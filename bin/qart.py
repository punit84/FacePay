import base64
import io
from io import BytesIO
from rembg import remove
from PIL import Image
import qrcode
import json
from sagemaker.serializers import JSONSerializer
from sagemaker.deserializers import BytesDeserializer
from sagemaker.predictor import Predictor
from sagemaker.predictor_async import AsyncPredictor
import boto3
from sqs_polling import sqs_polling
import time

def your_callback(message, greeting):
    print(message)
    message = json.loads(message)
    print("request for "+str(message["upi_id"]))
    upi_id = message["upi_id"]
    bucket = message["bucket"]
    person_s3_key = message["person_s3_key"]
    model_endpoint = message["model_endpoint"]
    reduction_ratio = float(message["reduction_ratio"])
    theme = message["theme"]
    seed = int(message["seed"])
    num_inference_steps = int(message["num_inference_steps"])
    num_images_per_prompt = int(message["num_images_per_prompt"])
    strength = float(message["strength"])
    guidance_scale = float(message["guidance_scale"])
    controlnet_1_conditioning_scale = float(message["controlnet_1_conditioning_scale"])
    controlnet_1_guidance_start = float(message["controlnet_1_guidance_start"])
    controlnet_1_guidance_end = float(message["controlnet_1_guidance_end"])
    controlnet_2_conditioning_scale = float(message["controlnet_2_conditioning_scale"])
    controlnet_2_guidance_start = float(message["controlnet_2_guidance_start"])
    controlnet_2_guidance_end = float(message["controlnet_2_guidance_end"])

    person_obj = s3.get_object(Bucket=bucket, Key=person_s3_key)
    person_dl = person_obj['Body'].read()
    person_raw_image = Image.open(BytesIO(person_dl))

    #generate qr
    qr = qrcode.QRCode(
        version=10,
        error_correction=qrcode.constants.ERROR_CORRECT_H,
        box_size=15,
        border=2,
    )
    qr.add_data(upi_id)
    qr.make(fit=True)
    upi_qr = qr.make_image(fill_color="black", back_color="white")
    width, height = upi_qr.size
    print("w: "+str(width)+" h: "+str(height))

    #generate qr with person overlapped
    input = person_raw_image
    person_raw_image.thumbnail((width*reduction_ratio,height*reduction_ratio), Image.Resampling.LANCZOS)
    person = remove(input)
 
    qrc = upi_qr.convert("RGB")
    qrc_merged = Image.new("RGBA", qrc.size)
    qrc_merged.paste(qrc, (0, 0))
    _, _, _, mask = person.split()
    x = int((width/2) - ((width*reduction_ratio)/2.5))
    y = int((height - (height*reduction_ratio)))
    qrc.paste(person, (x,y), mask)

    qrc1 = qrc
    qrc_person = remove(qrc)

    image1 = qrc1
    image2 = qrc_person

    buffered1 = BytesIO()
    image1.save(buffered1, format="PNG")
    starting_image = base64.b64encode(buffered1.getvalue())
    cnet_image_1 = starting_image

    buffered2 = BytesIO()
    image2.save(buffered2, format="PNG")
    cnet_image_2 = base64.b64encode(buffered2.getvalue())

    predictor = Predictor(model_endpoint)
    predictor.serializer=JSONSerializer()
    predictor.deserializer=BytesDeserializer()
    async_predictor = AsyncPredictor(predictor)

    theme_code = int(theme)
    if theme_code == 0:
        p_prompt="a cubism painting of a town with a lot of houses in the snow with a sky background, Andreas Rocha, matte painting concept art, a detailed matte painting"
        n_prompt="ugly, disfigured, low quality, blurry, NSFW"
    elif theme_code == 1:
        p_prompt="a cubism painting of a city with lots of small buildings and skyscrapers, bright daylight sky background, high quality, detailed"
        n_prompt="ugly, disfigured, low quality, blurry, NSFW"
    elif theme_code == 2:
        p_prompt="bamboo forest, bright daylight sky background, high quality, detailed"
        n_prompt="ugly, disfigured, low quality, blurry, NSFW"
    elif theme_code == 3:
        p_prompt="A photo-realistic rendering of a busy market, ((street vendors, fruits, vegetable, shops)), (Photorealistic:1.3), (Highly detailed:1.2), (Natural light:1.2), art inspired by Architectural Digest, Vogue Living, and Elle Decor"
        n_prompt="ugly, disfigured, low quality, blurry, NSFW"
    else:
        p_prompt="A photo-realistic rendering of a busy market, ((street vendors, fruits, vegetable, shops)), (Photorealistic:1.3), (Highly detailed:1.2), (Natural light:1.2), art inspired by Architectural Digest, Vogue Living, and Elle Decor"
        n_prompt="ugly, disfigured, low quality, blurry, NSFW"
        
    request={
        "prompt":p_prompt,
        "negative_prompt":n_prompt,
        "starting_image":starting_image.decode(),
        "controlnet_1_image":cnet_image_1.decode(),
        "controlnet_2_image":cnet_image_2.decode(),
        "seed": seed,
        "num_inference_steps": num_inference_steps,
        "num_images_per_prompt": num_images_per_prompt,
        "strength": strength,
        "guidance_scale": guidance_scale,
        "controlnet_1_conditioning_scale": controlnet_1_conditioning_scale,
        "controlnet_1_guidance_start": controlnet_1_guidance_start,
        "controlnet_1_guidance_end": controlnet_1_guidance_end,
        "controlnet_2_conditioning_scale": controlnet_2_conditioning_scale,
        "controlnet_2_guidance_start": controlnet_2_guidance_start,
        "controlnet_2_guidance_end": controlnet_2_guidance_end
    }

    response=async_predictor.predict(data=request, input_path="s3://"+bucket+"/qart/"+upi_id.split("pa=")[1]+"/"+theme+"_input.json")
    
    output=json.loads(response.decode())
    for image in output["output_images"]:
        person_raw_image=base64.b64decode(image)
        person_raw_image=Image.open(io.BytesIO(person_raw_image))
        in_mem_file = io.BytesIO()
        person_raw_image.save(in_mem_file, format=person_raw_image.format)
        in_mem_file.seek(0)
        s3.upload_fileobj(
            in_mem_file,
            bucket,
            "qart/"+upi_id.split("pa=")[1]+"/"+theme+"_qart.png"
        )

    print("Qart generated for " +str(message["upi_id"])+ " & saved to S3!")
    print("-----------------------------------------------")
    print("--- %s seconds ---" % (time.time() - start_time))
    print("-----------------------------------------------")

    return True

start_time = time.time()
s3 = boto3.client('s3')
your_queue_url="https://sqs.ap-south-1.amazonaws.com/093847107635/qart"

sqs_polling(queue_url=your_queue_url, callback=your_callback, callback_args={"greeting": "Hello, "})
