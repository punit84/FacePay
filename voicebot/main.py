from PIL import Image
import os
import asyncio
import base64
import json
import uuid
import pyaudio
import boto3
import time
import streamlit as st
import streamlit.components.v1 as components
import wave
import io
from botocore.config import Config
import traceback
from aws_sdk_bedrock_runtime.client import BedrockRuntimeClient, InvokeModelWithBidirectionalStreamOperationInput
from aws_sdk_bedrock_runtime.models import InvokeModelWithBidirectionalStreamInputChunk, BidirectionalInputPayloadPart
from aws_sdk_bedrock_runtime.config import Config, HTTPAuthSchemeResolver, SigV4AuthScheme
from smithy_aws_core.credentials_resolvers.environment import EnvironmentCredentialsResolver
import Configs

import Events
import ToolProcessor

import AcronymPhonemeConverter

first_run = True
streamlit_session_active = False

class SimpleNovaSonic:

    def _handle_tool_task_completion(self, task, content_name):
        """Handle the completion of a tool task"""
        # Remove task from pending tasks
        if content_name in self.pending_tool_tasks:
            del self.pending_tool_tasks[content_name]

        # Handle any exceptions
        if task.done() and not task.cancelled():
            exception = task.exception()
            if exception:
                print(f"Tool task failed: {str(exception)}")

    def handle_tool_request(self, tool_name, tool_content, tool_use_id):
        """Handle a tool request asynchronously"""
        # Create a unique content name for this tool response
        tool_content_name = str(uuid.uuid4())

        # Create an asynchronous task for the tool execution
        task = asyncio.create_task(self._execute_tool_and_send_result(tool_name, tool_content, tool_use_id, tool_content_name))

        # Store the task
        self.pending_tool_tasks[tool_content_name] = task

        # Add error handling
        task.add_done_callback(lambda t: self._handle_tool_task_completion(t, tool_content_name))

    async def send_tool_start_event(self, content_name, tool_use_id):
        """Send a tool content start event to the Bedrock stream."""
        content_start_event = Events.TOOL_CONTENT_START_EVENT % (self.prompt_name, content_name, tool_use_id)
        await self.send_event(content_start_event)

    def tool_result_event(self, content_name, content, role):
        """Create a tool result event"""
        if isinstance(content, dict):
            content_json_string = json.dumps(content)
        else:
            content_json_string = content

        tool_result_event = {
            "event": {
                "toolResult": {
                    "promptName": self.prompt_name,
                    "contentName": content_name,
                    "content": content_json_string
                }
            }
        }
        return json.dumps(tool_result_event)

    async def send_tool_result_event(self, content_name, tool_result):
        """Send a tool content event to the Bedrock stream."""
        # Use the actual tool result from processToolUse
        tool_result_event = self.tool_result_event(content_name=content_name, content=tool_result, role="TOOL")
        print(f"== Sending tool result event: {tool_result_event}")
        await self.send_event(tool_result_event)

    async def send_tool_content_end_event(self, content_name):
        """Send a tool content end event to the Bedrock stream."""
        tool_content_end_event = Events.CONTENT_END_EVENT % (self.prompt_name, content_name)
        await self.send_event(tool_content_end_event)

    async def _execute_tool_and_send_result(self, tool_name, tool_content, tool_use_id, content_name):
        """Execute a tool and send the result"""
        try:
            print(f"== Starting tool execution: {tool_name}")

            # Process the tool - this doesn't block the event loop
            tool_result = await self.tool_processor.process_tool_async(tool_name, tool_content)

            # Send the result sequence
            await self.send_tool_start_event(content_name, tool_use_id)
            await self.send_tool_result_event(content_name, tool_result)
            await self.send_tool_content_end_event(content_name)

            print(f"== Tool execution complete: {tool_name}\n")
        except Exception as e:
            print(f"Error executing tool {tool_name}: {str(e)}")
            # Try to send an error response if possible
            try:
                error_result = {"error": f"Tool execution failed: {str(e)}"}
                await self.send_tool_start_event(content_name, tool_use_id)
                await self.send_tool_result_event(content_name, error_result)
                await self.send_tool_content_end_event(content_name)
            except Exception as send_error:
                print(f"Failed to send error response: {str(send_error)}")

    def pcm_to_b64_wav(self, pcm_data: bytes, sample_rate=Configs.CONFIG['audio']['input_sample_rate'], channels=Configs.CONFIG['audio']['channels'], sample_width=2) -> str:
        buffer = io.BytesIO()
        with wave.open(buffer, 'wb') as wav_file:
            wav_file.setnchannels(channels)
            wav_file.setsampwidth(sample_width)
            wav_file.setframerate(sample_rate)
            wav_file.writeframes(pcm_data)

        wav_bytes = buffer.getvalue()
        return base64.b64encode(wav_bytes).decode('utf-8')

    def __init__(self, model_id='amazon.nova-sonic-v1:0', region='us-east-1'):
        self.model_id = model_id
        self.region = region
        self.client = None
        self.stream = None
        self.response = None
        self.is_active = False
        self.barge_in = False
        self.prompt_name = str(uuid.uuid4())
        self.content_name = str(uuid.uuid4())
        self.audio_content_name = str(uuid.uuid4())
        self.audio_queue = asyncio.Queue()
        self.display_assistant_text = False

        self.tool_processor = ToolProcessor.ToolProcessor()
        self.ssml_converter = AcronymPhonemeConverter.AcronymPhonemeConverter()

        # Add tracking for in-progress tool calls
        self.pending_tool_tasks = {}

        self.bot_speaking = False

    def _initialize_client(self):
        """Initialize the Bedrock client."""
        config = Config(
            endpoint_uri=f"https://bedrock-runtime.{self.region}.amazonaws.com",
            region=self.region,
            aws_credentials_identity_resolver=EnvironmentCredentialsResolver(),
            http_auth_scheme_resolver=HTTPAuthSchemeResolver(),
            http_auth_schemes={"aws.auth#sigv4": SigV4AuthScheme()}
        )
        self.client = BedrockRuntimeClient(config=config)

        self.polly_client = boto3.client("polly", region_name=Configs.CONFIG["polly"]["region"])

    async def send_event(self, event_json):
        """Send an event to the stream."""
        event = InvokeModelWithBidirectionalStreamInputChunk(value=BidirectionalInputPayloadPart(bytes_=event_json.encode('utf-8')))
        await self.stream.input_stream.send(event)

    async def start_session(self):
        """Start a new session with Nova Sonic."""
        if not self.client:
            self._initialize_client()

        # Initialize the stream
        self.stream = await self.client.invoke_model_with_bidirectional_stream(
            InvokeModelWithBidirectionalStreamOperationInput(model_id=self.model_id)
        )
        self.is_active = True


        prompt_event = Events.start_prompt(self)
        text_content_start = Events.TEXT_CONTENT_START_EVENT % (self.prompt_name, self.content_name, "SYSTEM")
        text_content = Events.TEXT_INPUT_EVENT % (self.prompt_name, self.content_name, Configs.system_prompt_sonic)
        text_content_end = Events.CONTENT_END_EVENT % (self.prompt_name, self.content_name)

        init_events = [Events.START_SESSION_EVENT, prompt_event, text_content_start, text_content, text_content_end]

        for event in init_events:
            await self.send_event(event)
            await asyncio.sleep(0.1)

        # Start processing responses
        self.response = asyncio.create_task(self._process_responses())

    async def start_audio_input(self):
        """Start audio input stream."""
        audio_content_start = f'''
        {{
            "event": {{
                "contentStart": {{
                    "promptName": "{self.prompt_name}",
                    "contentName": "{self.audio_content_name}",
                    "type": "AUDIO",
                    "interactive": true,
                    "role": "USER",
                    "audioInputConfiguration": {{
                        "mediaType": "audio/lpcm",
                        "sampleRateHertz": 16000,
                        "sampleSizeBits": 16,
                        "channelCount": 1,
                        "audioType": "SPEECH",
                        "encoding": "base64"
                    }}
                }}
            }}
        }}
        '''
        await self.send_event(audio_content_start)

    async def send_audio_chunk(self, audio_bytes):
        """Send an audio chunk to the stream."""
        if not self.is_active:
            return

        blob = base64.b64encode(audio_bytes)
        audio_event = f'''
        {{
            "event": {{
                "audioInput": {{
                    "promptName": "{self.prompt_name}",
                    "contentName": "{self.audio_content_name}",
                    "content": "{blob.decode('utf-8')}"
                }}
            }}
        }}
        '''
        await self.send_event(audio_event)

    async def end_audio_input(self):
        """End audio input stream."""
        audio_content_end = f'''
        {{
            "event": {{
                "contentEnd": {{
                    "promptName": "{self.prompt_name}",
                    "contentName": "{self.audio_content_name}"
                }}
            }}
        }}
        '''
        await self.send_event(audio_content_end)

    async def end_session(self):
        """End the session."""
        if not self.is_active:
            return

        prompt_end = f'''
        {{
            "event": {{
                "promptEnd": {{
                    "promptName": "{self.prompt_name}"
                }}
            }}
        }}
        '''
        await self.send_event(prompt_end)

        session_end = '''
        {
            "event": {
                "sessionEnd": {}
            }
        }
        '''
        await self.send_event(session_end)
        # close the stream
        await self.stream.input_stream.close()

    async def _process_responses(self):
        """Process responses from the stream."""
        try:
            while self.is_active:
                try:
                    output = await self.stream.await_output()
                    result = await output[1].receive()
                    if result is not None:
                        if result.value and result.value.bytes_:
                            response_data = result.value.bytes_.decode('utf-8')
                            json_data = json.loads(response_data)

                            if 'event' in json_data:
                                # Handle content start event
                                if 'contentStart' in json_data['event']:
                                            content_start = json_data['event']['contentStart']
                                            # set role
                                            self.role = content_start['role']
                                            # Check for speculative content
                                            if 'additionalModelFields' in content_start:
                                                additional_fields = json.loads(content_start['additionalModelFields'])
                                                if additional_fields.get('generationStage') == 'SPECULATIVE':
                                                    self.display_assistant_text = True
                                                else:
                                                    self.display_assistant_text = False

                                # Handle text output event
                                elif 'textOutput' in json_data['event']:
                                            text = json_data['event']['textOutput']['content']

                                            if '{ "interrupted" : true }' in text:
                                                print("Barge-in detected.")
                                                self.barge_in = True

                                            if (self.role == "ASSISTANT" and self.display_assistant_text):

                                                print()

                                                # extra
                                                response = self.polly_client.synthesize_speech(
                                                    Text=self.ssml_converter.convert_to_ssml(text),
                                                    VoiceId=Configs.CONFIG["polly"]["voice_id"],
                                                    Engine=Configs.CONFIG["polly"]["engine"],
                                                    LanguageCode=Configs.CONFIG['polly']['language_code'],
                                                    OutputFormat=Configs.CONFIG['polly']['output_format'],
                                                    SampleRate=str(Configs.CONFIG["polly"]["sample_rate"]),
                                                    TextType='ssml'
                                                )
                                                audio_bytes = response["AudioStream"].read()
                                                if not native_voice_mode:
                                                    #print("inside polly_mode")
                                                    await self.audio_queue.put(audio_bytes)
                                                # extra
                                                print(f"Assistant      : {text}")
                                                print(f"Assistant-ssml : {self.ssml_converter.convert_to_ssml(text)}")

                                                placeholder = st.empty()
                                                words = text.split()
                                                streamed_text = ""
                                                for word in words:
                                                    streamed_text += word + " "
                                                    placeholder.markdown(
                                                        f"""<div class="bot-message"><div class="message">{streamed_text}</div><div class="avatar">🤖</div></div>""",
                                                        unsafe_allow_html=True)
                                                    time.sleep(0.02)  # Adjust speed as needed

                                                st.session_state.messages.append({"role": "assistant", "content": text})


                                            elif self.role == "USER":
                                                print(f"User           : {text}")
                                                placeholder = st.empty()
                                                words = text.split()
                                                streamed_text = ""
                                                for word in words:
                                                    streamed_text += word + " "
                                                    placeholder.markdown(
                                                        f"""<div class="user-message"><div class="avatar">🧑‍</div><div class="message">{streamed_text}</div></div>""",
                                                        unsafe_allow_html=True)
                                                    time.sleep(0.02)  # Adjust speed as needed
                                                st.session_state.messages.append({"role": "user", "content": text})

                                # Handle audio output
                                elif 'audioOutput' in json_data['event']:
                                            audio_content = json_data['event']['audioOutput']['content']
                                            audio_bytes = base64.b64decode(audio_content)
                                            # discard sonic english voice - sounds like "doguna lagan lega" for hindi
                                            if native_voice_mode:
                                                #print("inside native_voice_mode")
                                                await self.audio_queue.put(audio_bytes)

                                elif 'toolUse' in json_data['event']:
                                            self.toolUseContent = json_data['event']['toolUse']
                                            self.toolName = json_data['event']['toolUse']['toolName']
                                            self.toolUseId = json_data['event']['toolUse']['toolUseId']
                                            print(f"\nTool use detected: {self.toolName}, ID: {self.toolUseId}")

                                elif 'contentEnd' in json_data['event'] and json_data['event'].get('contentEnd', {}).get('type') == 'TOOL':
                                            print("Processing tool use and sending result")
                                            # Start asynchronous tool processing - non-blocking
                                            self.handle_tool_request(self.toolName, self.toolUseContent, self.toolUseId)
                                            print("Processing tool use asynchronously")

                                #elif 'contentEnd' in json_data['event']:
                                #    print("Content end")

                                elif 'completionEnd' in json_data['event']:
                                            print("End of response sequence")

                                #elif 'usageEvent' in json_data['event']:
                                #    print(f"UsageEvent: {json_data['event']}")
                    else:
                        break

                except Exception as e:
                    print(f"Found Error processing responses, skipping & proceeding to next event: {e}")
                    # Invalid event bytes.
                    # The system encountered an unexpected error during processing. Try your request again.
                    # Timed out waiting for input events
                    # Checksum mismatch: expected 0x4d6d466d, calculated 0xa3a39e81
                    continue

        except Exception as e:
            print(f"Error processing responses: {e}")
            traceback.print_exc()

    def render_waveform(self, b64_audio):
        html_string = f"""      
        <html>
        <head>
        <style>
          body {{
            margin: 0;
            padding: 0;
            background: transparent;
            overflow: hidden;
          }}
          canvas {{
            display: block;
            margin: auto;
            background-color: transparent;
          }}
          audio {{
            display: none;
          }}
        </style>
        </head>
        <body>
        <canvas id="canvas"></canvas>
        <audio id="audio" src="data:audio/wav;base64,{b64_audio}"></audio>
        
        <script>
        const canvas = document.getElementById("canvas");
        const ctx = canvas.getContext("2d");
        canvas.width = window.innerWidth;
        canvas.height = 200;
        
        const audio = document.getElementById("audio");
        const audioCtx = new (window.AudioContext || window.webkitAudioContext)();
        const analyser = audioCtx.createAnalyser();
        analyser.fftSize = 256;
        
        const source = audioCtx.createMediaElementSource(audio);
        source.connect(analyser); // Only connect to analyser
        // analyser.connect(audioCtx.destination); ❌ Skip this to avoid actual playback
        
        const bufferLength = analyser.frequencyBinCount;
        const dataArray = new Uint8Array(bufferLength);
        
        function draw() {{
          requestAnimationFrame(draw);
          analyser.getByteFrequencyData(dataArray);
          ctx.clearRect(0, 0, canvas.width, canvas.height);
          const barCount = 48;
          const spacing = canvas.width / barCount;
          for (let i = 0; i < barCount; i++) {{
            const val = dataArray[i] / 255.0;
            const barHeight = val * canvas.height * 0.8;
            const x = canvas.width - (i + 1) * spacing;
            ctx.fillStyle = '#F87171';
            ctx.fillRect(x, canvas.height/2 - barHeight/2, spacing * 0.3, barHeight);
          }}
        }}
        
        audio.play().then(() => {{
          audioCtx.resume().then(() => {{
            draw();
          }});
        }});
        
        audio.onplay = () => {{
          if (audioCtx.state === "suspended") {{
            audioCtx.resume();
          }}
          draw();
        }};
        </script>
        </body>
        </html>
        """
        components.html(html_string, height=200)

    async def play_audio(self):
        p = pyaudio.PyAudio()
        if native_voice_mode:
            stream = p.open(
                format=Configs.CONFIG['audio']['format'],
                channels=Configs.CONFIG['audio']['channels'],
                rate=24000,
                output=True
            )
            try:
                while self.is_active:
                    audio_data = await self.audio_queue.get()
                    self.bot_speaking =  True
                    stream.write(audio_data)
                await asyncio.sleep(0.1)
                self.bot_speaking = False
            except Exception as e:
                print(f"Error playing audio: {e}")
            finally:
                stream.stop_stream()
                stream.close()
                p.terminate()
        else:
            stream = p.open(
                format=Configs.CONFIG['audio']['format'],
                channels=Configs.CONFIG['audio']['channels'],
                rate=Configs.CONFIG['audio']['output_sample_rate'],
                output=True
            )
            with col3:
                placeholder = st.empty()
                try:
                    while self.is_active:
                        audio_data = await self.audio_queue.get()
                        b64_audio = self.pcm_to_b64_wav(audio_data,
                                                        sample_rate=Configs.CONFIG['audio']['output_sample_rate'],
                                                        channels=Configs.CONFIG['audio']['channels'],
                                                        sample_width=2)  # Usually 2 for 16-bit audio
                        # stream.write(audio_data)
                        self.bot_speaking = True
                        playback_task = asyncio.to_thread(stream.write, audio_data)

                        with placeholder:
                            self.render_waveform(b64_audio)
                        await playback_task
                        await asyncio.sleep(0.1)  # Slight delay to allow render
                        self.bot_speaking = False
                except Exception as e:
                    print(f"Error playing audio: {e}")
                finally:
                    stream.stop_stream()
                    stream.close()
                    p.terminate()


    async def capture_audio(self):
        """Capture audio from microphone and send to Nova Sonic."""
        p = pyaudio.PyAudio()
        stream = p.open(
            format=p.get_format_from_width(2),
            channels=Configs.CONFIG['audio']['channels'],
            rate=Configs.CONFIG['audio']['input_sample_rate'],
            input=True,
            frames_per_buffer=Configs.CONFIG['audio']['chunk_size']
        )

        print("\nStarting audio capture. Speak into your microphone...\n")

        # Render JS waveform once at the beginning
        with col1:
            components.html(f"""
            <html>
            <head>
            <style>
              body {{
                margin: 0;
                background: transparent;
                overflow: hidden;
              }}
              canvas {{
                display: block;
                margin: auto;
                background-color: transparent;
              }}
            </style>
            </head>
            <body>
            <canvas id="canvas"></canvas>

            <script>
            const canvas = document.getElementById("canvas");
            const ctx = canvas.getContext("2d");
            canvas.width = window.innerWidth;
            canvas.height = 200;

            navigator.mediaDevices.getUserMedia({{ audio: true }})
              .then(stream => {{
                const audioCtx = new (window.AudioContext || window.webkitAudioContext)();
                const source = audioCtx.createMediaStreamSource(stream);
                const analyser = audioCtx.createAnalyser();
                analyser.fftSize = 256;
                source.connect(analyser);

                const bufferLength = analyser.frequencyBinCount;
                const dataArray = new Uint8Array(bufferLength);

                function draw() {{
                  requestAnimationFrame(draw);
                  analyser.getByteFrequencyData(dataArray);
                  ctx.clearRect(0, 0, canvas.width, canvas.height);

                  const barCount = 48;
                  const spacing = canvas.width / barCount;

                  for (let i = 0; i < barCount; i++) {{
                    const val = dataArray[i] / 255.0;
                    const alpha = 0.3 + (i / barCount) * 0.7;
                    const barHeight = val * canvas.height * 0.8;
                    const x = i * spacing;
                    //reverse
                    //const x = canvas.width - (i + 1) * spacing;

                    //ctx.fillStyle = `rgba(255,255,255,${{alpha.toFixed(2)}})`;
                    ctx.fillStyle =  '#FDFD96';
                    ctx.fillRect(x, canvas.height/2 - barHeight/2, spacing * 0.3, barHeight);
                  }}
                }}
                draw();
              }})
              .catch(e => {{
                console.error("Mic access failed:", e);
              }});
            </script>
            </body>
            </html>
            """, height=200)

        await self.start_audio_input()

        try:
            while self.is_active:
                if self.bot_speaking:
                    await asyncio.sleep(1)
                    continue
                audio_data = stream.read(Configs.CONFIG['audio']['chunk_size'], exception_on_overflow=False)
                await self.send_audio_chunk(audio_data)
                await asyncio.sleep(0.01)
        except Exception as e:
            print(f"Error capturing audio: {e}")
        finally:
            stream.stop_stream()
            stream.close()
            p.terminate()
            print("Audio capture stopped.")
            await self.end_audio_input()

async def main():
    nova_client = SimpleNovaSonic()
    await nova_client.start_session()
    playback_task = asyncio.create_task(nova_client.play_audio())
    capture_task = asyncio.create_task(nova_client.capture_audio())

    # Keep running while streamlit_session_active is True
    try:
        while st.session_state.streamlit_session_active:
            await asyncio.sleep(0.1)

    except asyncio.CancelledError:
        print("got CancelledError")

    finally:
        nova_client.is_active = False
        # First cancel the tasks
        tasks = []
        if not playback_task.done():
            tasks.append(playback_task)
        if not capture_task.done():
            tasks.append(capture_task)
        for task in tasks:
            task.cancel()
        if tasks:
            await asyncio.gather(*tasks, return_exceptions=True)
        # cancel the response task
        if nova_client.response and not nova_client.response.done():
            nova_client.response.cancel()

        await nova_client.end_session()
        print("== Session ended cleanly")

if __name__ == "__main__":

    #os.environ['AWS_ACCESS_KEY_ID'] = Configs.CONFIG['aws']['access_key_id']
    #s.environ['AWS_SECRET_ACCESS_KEY'] = Configs.CONFIG['aws']['secret_access_key']
    #os.environ['AWS_DEFAULT_REGION'] = "us-east-1"
    os.environ['KB_ID'] = Configs.CONFIG['sonic']['kb_id']

    # Streamlit stuff
    st.set_page_config(initial_sidebar_state="collapsed")
    with st.sidebar:
        st.write("Model : amazon.nova-sonic-v1:0")
        "[Model architecture](https://docs.aws.amazon.com/nova/latest/userguide/speech.html#speech-architecture)"
        "[Sample code](https://github.com/aws-samples/amazon-nova-samples/tree/main/speech-to-speech)"
        st.write("FAQs:")
        st.code("haan ji aap meree keisee saahataa kar sakte ho", language=None)
        st.code("devnagri script mein hello likhe dikhaao", language=None)
        st.code("mujhe upi ke baare mein bataao", language=None)
        st.code("kyaa upi reliable hei", language=None)
        st.code("upi ke top apps kaun hai", language=None)
        st.code("meraa payment fail ho gayaa, kya karun", language=None)
        st.code("abhi time kya ho raha hai", language=None)
        st.code("mere rupay card se kare last 5 transactions ki detail do", language=None)
        st.code("mobikwik ki financial metrics batao", language=None)
        st.code("paytm ke revenue metrics ke baare men bataao", language=None)
        st.code("paytm ke mtu kitne hai", language=None)
        st.code("paytm ke paas monetization ke kya vikalp hei", language=None)
        st.code("paytm ke context mein payment aggregator license samajhao", language=None)

        native_voice_mode = st.checkbox("Native voice Mode")

    st.title("_:orange[FINGenie] - voicebot for Fintech_")
    #st.markdown("<h6 style='color: gray;'>Powered by Amazon Nova Sonic & Amazon Polly</h6>", unsafe_allow_html=True)
    st.markdown("""
        <style>
            .user-message {
                display: flex;
                justify-content: flex-start;
                align-items: flex-start;
                padding: 5px 0;
            }

            .user-message .avatar {
                width: 32px;
                height: 32px;
                background-color: #FDFD96;
                border-radius: 8px;
                display: flex;
                align-items: center;
                justify-content: center;
                font-size: 20px;
                color: white;
            }
            .user-message .message {
                background-color: #1A1C23; 
                color: #FAFAFA;
                padding: 10px 14px;
                border-radius: 12px;
                max-width: 60%;
                word-wrap: break-word;
                box-shadow: 0px 2px 4px rgba(0, 0, 0, 0.1);
                margin-left: 8px;
            }


            .bot-message {
                display: flex;
                justify-content: flex-end;
                align-items: flex-end;
                padding: 5px 0;
            }

            .bot-message .message {
                background-color: #1A1C23;
                color: #FAFAFA;
                padding: 10px 14px;
                border-radius: 12px;
                max-width: 60%;
                word-wrap: break-word;
                box-shadow: 0px 2px 4px rgba(0, 0, 0, 0.1);
                margin-right: 8px;
            }

            .bot-message .avatar {
                width: 32px;
                height: 32px;
                background-color: #F87171;
                border-radius: 8px;
                display: flex;
                align-items: center;
                justify-content: center;
                font-size: 20px;
                color: white;
            }
        </style>
    """, unsafe_allow_html=True)

    col1, col2, col3 = st.columns(3)

    with col2:
        img = Image.open("nova-sonic.png")
        st.image(img, width=200)

    chat_box = st.container(height=750)  # fixed height, scrollable

    if "input_waveform" not in st.session_state:
        st.session_state.input_waveform = st.empty()
    if "output_waveform" not in st.session_state:
        st.session_state.output_waveform = st.empty()

    if "first_run" not in st.session_state:
        st.session_state.first_run = True
    if "streamlit_session_active" not in st.session_state:
        st.session_state.streamlit_session_active = False

    if st.button("Reset Session"):
        if st.session_state.first_run:
            print("first run")

            with chat_box:
                st.session_state["messages"] = [{"role": "assistant", "content": "Help chahiye?"}]


                for msg in st.session_state.messages:
                    if msg["role"] == "user":
                        st.markdown(
                            f"""<div class="user-message"><div class="avatar">🧑‍</div><div class="message">{msg["content"]}</div></div>""",
                            unsafe_allow_html=True)
                    else:
                        st.markdown(
                            f"""<div class="bot-message"><div class="message">{msg["content"]}</div><div class="avatar">🤖</div></div>""",
                            unsafe_allow_html=True)
                st.session_state.streamlit_session_active = True
                st.session_state.first_run = False
                asyncio.run(main())

                st.markdown("""
                    <script>
                    const chatBox = parent.document.querySelector('section.main div[data-testid="stVerticalBlock"]');
                    if (chatBox) chatBox.scrollTop = chatBox.scrollHeight;
                    </script>
                """, unsafe_allow_html=True)
        else:
            with chat_box:

                print("\nnot first run")
                st.session_state.streamlit_session_active = False
                time.sleep(2)
                print("== Restarting session")
                st.session_state.streamlit_session_active = True
                st.session_state.messages = []
                st.session_state["messages"] = [{"role": "assistant", "content": "Help chahiye?"}]
                for msg in st.session_state.messages:
                    if msg["role"] == "user":
                        st.markdown(
                            f"""<div class="user-message"><div class="avatar">🧑‍</div><div class="message">{msg["content"]}</div></div>""",
                            unsafe_allow_html=True)
                    else:
                        st.markdown(
                            f"""<div class="bot-message"><div class="message">{msg["content"]}</div><div class="avatar">🤖</div></div>""",
                            unsafe_allow_html=True)
                asyncio.run(main())

                st.markdown("""
                    <script>
                    const chatBox = parent.document.querySelector('section.main div[data-testid="stVerticalBlock"]');
                    if (chatBox) chatBox.scrollTop = chatBox.scrollHeight;
                    </script>
                """, unsafe_allow_html=True)