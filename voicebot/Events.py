import json

# Event templates
START_SESSION_EVENT = '''{
        "event": {
            "sessionStart": {
            "inferenceConfiguration": {
                "maxTokens": 4096,
                "topP": 0.9,
                "temperature": 0.7
                }
            }
        }
    }'''

CONTENT_START_EVENT = '''{
        "event": {
            "contentStart": {
            "promptName": "%s",
            "contentName": "%s",
            "type": "AUDIO",
            "interactive": true,
            "role": "USER",
            "audioInputConfiguration": {
                "mediaType": "audio/lpcm",
                "sampleRateHertz": 16000,
                "sampleSizeBits": 16,
                "channelCount": 1,
                "audioType": "SPEECH",
                "encoding": "base64"
                }
            }
        }
    }'''

AUDIO_EVENT_TEMPLATE = '''{
        "event": {
            "audioInput": {
            "promptName": "%s",
            "contentName": "%s",
            "content": "%s"
            }
        }
    }'''

TEXT_CONTENT_START_EVENT = '''{
        "event": {
            "contentStart": {
            "promptName": "%s",
            "contentName": "%s",
            "type": "TEXT",
            "role": "%s",
            "interactive": true,
                "textInputConfiguration": {
                    "mediaType": "text/plain"
                }
            }
        }
    }'''

TEXT_INPUT_EVENT = '''{
        "event": {
            "textInput": {
            "promptName": "%s",
            "contentName": "%s",
            "content": "%s"
            }
        }
    }'''

TOOL_CONTENT_START_EVENT = '''{
        "event": {
            "contentStart": {
                "promptName": "%s",
                "contentName": "%s",
                "interactive": false,
                "type": "TOOL",
                "role": "TOOL",
                "toolResultInputConfiguration": {
                    "toolUseId": "%s",
                    "type": "TEXT",
                    "textInputConfiguration": {
                        "mediaType": "text/plain"
                    }
                }
            }
        }
    }'''

CONTENT_END_EVENT = '''{
        "event": {
            "contentEnd": {
            "promptName": "%s",
            "contentName": "%s"
            }
        }
    }'''

PROMPT_END_EVENT = '''{
        "event": {
            "promptEnd": {
            "promptName": "%s"
            }
        }
    }'''

SESSION_END_EVENT = '''{
        "event": {
            "sessionEnd": {}
        }
    }'''


def start_prompt(self):
    """Create a promptStart event"""
    get_default_tool_schema = json.dumps({
        "type": "object",
        "properties": {},
        "required": []
    })

    get_payment_tracking_schema = json.dumps({
        "type": "object",
        "properties": {
            "paymentId": {
                "type": "string",
                "description": "The UPI payment number or ID to track"
            },
            "requestNotifications": {
                "type": "boolean",
                "description": "Whether to set up notifications for this payment",
                "default": False
            }
        },
        "required": ["paymentId"]
    })

    get_stock_value_schema = json.dumps({
        "type": "object",
        "properties": {
            "companyName": {
                "type": "string",
                "description": "The name of company to find stock value for"
            },
            "requestNotifications": {
                "type": "boolean",
                "description": "Whether to set up notifications for this payment",
                "default": False
            }
        },
        "required": ["companyName"]
    })

    get_kb_schema = json.dumps({
        "type": "object",
        "properties": {
            "query": {
                "type": "string",
                "description": "Search the company knowledge base for past transaction history or information on key financial metrics of Paytm and Mobikwik"
            }
        },
        "required": ["query"]
    })

    prompt_start_event = {
        "event": {
            "promptStart": {
                "promptName": self.prompt_name,
                "textOutputConfiguration": {
                    "mediaType": "text/plain"
                },
                "audioOutputConfiguration": {
                    "mediaType": "audio/lpcm",
                    "sampleRateHertz": 24000,
                    "sampleSizeBits": 16,
                    "channelCount": 1,
                    "voiceId": "amy",
                    "encoding": "base64",
                    "audioType": "SPEECH"
                },
                "toolUseOutputConfiguration": {
                    "mediaType": "application/json"
                },
                "toolConfiguration": {
                    "toolChoice": {
                        "auto": {}
                    },
                    "tools": [
                        {
                            "toolSpec": {
                                "name": "knowledgeBase",
                                "description": "Search the company knowledge base for past transaction history or information on key financial metrics of Paytm and Mobikwik",
                                "inputSchema": {
                                    "json": get_kb_schema
                                }
                            }
                        },
                        {
                            "toolSpec": {
                                "name": "getDateAndTimeTool",
                                "description": "get information about the current date and time",
                                "inputSchema": {
                                    "json": get_default_tool_schema
                                }
                            }
                        },
                        {
                            "toolSpec": {
                                "name": "getstockvaluetool",
                                "description": "get information about the current stock value of company",
                                "inputSchema": {
                                    "json": get_stock_value_schema
                                }
                            }
                        },
                        {
                            "toolSpec": {
                                "name": "trackPaymentTool",
                                "description": "Retrieves real-time information and detailed status updates for customer UPI payment by UPI ID. Provides estimated completion dates. Use this tool when customers ask about their UPI payment status or completion timeline.",
                                "inputSchema": {
                                    "json": get_payment_tracking_schema
                                }
                            }
                        }
                    ]
                }
            }
        }
    }

    return json.dumps(prompt_start_event)

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