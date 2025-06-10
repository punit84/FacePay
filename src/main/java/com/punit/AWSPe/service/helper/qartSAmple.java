package com.punit.AWSPe.service.helper;


import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;

public class qartSAmple {

	private static final S3Client s3Client = S3Client.builder().build();
	private static final SqsClient sqsClient = SqsClient.builder().build();
	private static final ObjectMapper objectMapper = new ObjectMapper();
	private static final RestTemplate restTemplate = new RestTemplate();
	private static final String bucketName = "qart-test-bucket";
	private static final String queueUrl = "https://sqs.ap-south-1.amazonaws.com/057641535369/qart";

	public static void main(String[] args) {
		//SpringApplication.run(qartSAmple.class, args);

		qartSAmple sample = new qartSAmple();
		try {
			sample.generateQRCode("upi://pay?pa=9911078929@paytm&pn=PaytmUser&cu=INR");
		} catch (IOException | WriterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}



//		Map<String, String> messageMap = objectMapper.readValue(message.body(), Map.class);
//		System.out.println("request for " + messageMap.get("upi_id"));
//
//		String upi_id = messageMap.get("upi_id");
//		String bucket = messageMap.get("bucket");
//		String person_s3_key = messageMap.get("person_s3_key");
//		String model_endpoint = messageMap.get("model_endpoint");
//		double reduction_ratio = Double.parseDouble(messageMap.get("reduction_ratio"));
//		String theme = messageMap.get("theme");
//		int seed = Integer.parseInt(messageMap.get("seed"));
//		int num_inference_steps = Integer.parseInt(messageMap.get("num_inference_steps"));
//		int num_images_per_prompt = Integer.parseInt(messageMap.get("num_images_per_prompt"));
//		double strength = Double.parseDouble(messageMap.get("strength"));
//		double guidance_scale = Double.parseDouble(messageMap.get("guidance_scale"));
//		double controlnet_1_conditioning_scale = Double.parseDouble(messageMap.get("controlnet_1_conditioning_scale"));
//		double controlnet_1_guidance_start = Double.parseDouble(messageMap.get("controlnet_1_guidance_start"));
//		double controlnet_1_guidance_end = Double.parseDouble(messageMap.get("controlnet_1_guidance_end"));
//		double controlnet_2_conditioning_scale = Double.parseDouble(messageMap.get("controlnet_2_conditioning_scale"));
//		double controlnet_2_guidance_start = Double.parseDouble(messageMap.get("controlnet_2_guidance_start"));
//		double controlnet_2_guidance_end = Double.parseDouble(messageMap.get("controlnet_2_guidance_end"));
//
//		BufferedImage qrImage = generateQRCode(upi_id);
//		BufferedImage combinedImage = mergeImages(qrImage, person_s3_key, bucket, reduction_ratio);
//
//		// Encode images to base64
//		String startingImageBase64 = encodeImageToBase64(qrImage);
//		String cnetImage1Base64 = encodeImageToBase64(combinedImage);
//
//		// Prepare request
//		MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
//		requestBody.add("prompt", getPrompt(theme_) );
//		requestBody.add("negative_prompt", "ugly, disfigured, low quality, blurry, NSFW");
//		requestBody.add("starting_image", startingImageBase64);
//		requestBody.add("controlnet_1_image", cnetImage1Base64);
//		requestBody.add("controlnet_2_image", "");
//		requestBody.add("seed", String.valueOf(seed));
//		requestBody.add("num_inference_steps", String.valueOf(num_inference_steps));
//		requestBody.add("num_images_per_prompt", String.valueOf(num_images_per_prompt));
//		requestBody.add("strength", String.valueOf(strength));
//		requestBody.add("guidance_scale", String.valueOf(guidance_scale));
//		requestBody.add("controlnet_1_conditioning_scale", String.valueOf(controlnet_1_conditioning_scale));
//		requestBody.add("controlnet_1_guidance_start", String.valueOf(controlnet_1_guidance_start));
//		requestBody.add("controlnet_1_guidance_end", String.valueOf(controlnet_1_guidance_end));
//		requestBody.add("controlnet_2_conditioning_scale", String.valueOf(controlnet_2_conditioning_scale));
//		requestBody.add("controlnet_2_guidance_start", String.valueOf(controlnet_2_guidance_start));
//		requestBody.add("controlnet_2_guidance_end", String.valueOf(controlnet_2_guidance_end));
//
//		// Send request to SageMaker
//		HttpHeaders headers = new HttpHeaders();
//		headers.setContentType(MediaType.APPLICATION_JSON);
//		HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(requestBody, headers);
//		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(model_endpoint);
//		ResponseEntity<String> responseEntity = restTemplate.postForEntity(builder.toUriString(), requestEntity, String.class);
//		Map<String, Object> responseMap = objectMapper.readValue(responseEntity.getBody(), Map.class);
//		List<String> outputImages = (List<String>) responseMap.get("output_images");
//
//		// Save images to S3
//		for (String image : outputImages) {
//			saveImageToS3(image, upi_id, theme);
//		}
//
//		System.out.println("Qart generated for " + upi_id + " & saved to S3!");
//		System.out.println("-----------------------------------------------");
//		//System.out.println("--- " + TimeUnit.SECONDS.convert(System.nanoTime() - start_time, TimeUnit.NANOSECONDS) + " seconds ---");
//		System.out.println("-----------------------------------------------");
//	}

	private BufferedImage generateQRCode(String upi_id) throws IOException, WriterException {
		Map<EncodeHintType, Object> hints = new java.util.EnumMap<>(EncodeHintType.class);
		hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
		hints.put(EncodeHintType.MARGIN, 1);
		QRCodeWriter qrCodeWriter = new QRCodeWriter();
		BitMatrix bitMatrix = qrCodeWriter.encode(upi_id, BarcodeFormat.QR_CODE, 200, 200, hints);
		return MatrixToImageWriter.toBufferedImage(bitMatrix);
	}

	private void saveQRCodeLocally(BufferedImage qrImage, String upi_id) throws IOException {
		File qrFile = new File(upi_id + "_qr.png");
		ImageIO.write(qrImage, "png", qrFile);
		System.out.println("QR Code saved locally at: " + qrFile.getAbsolutePath());
	}

	private BufferedImage mergeImages(BufferedImage qrImage, String person_s3_key, String bucket, double reduction_ratio) throws IOException {
		ResponseInputStream<GetObjectResponse> s3Object = s3Client.getObject(GetObjectRequest.builder().bucket(bucket).key(person_s3_key).build());
		BufferedImage personImage = ImageIO.read(s3Object);
		personImage = removeBackground(personImage); // Remove background here

		BufferedImage combinedImage = new BufferedImage(qrImage.getWidth(), qrImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d = combinedImage.createGraphics();
		g2d.drawImage(qrImage, 0, 0, null);
		int x = (int) ((combinedImage.getWidth() - (personImage.getWidth() * reduction_ratio)) / 2);
		int y = (int) ((combinedImage.getHeight() - (personImage.getHeight() * reduction_ratio)) / 2);
		g2d.drawImage(personImage.getScaledInstance((int) (personImage.getWidth() * reduction_ratio), -1, Image.SCALE_SMOOTH), x, y, null);
		g2d.dispose();
		return combinedImage;
	}

	private BufferedImage removeBackground(BufferedImage inputImage) {
		// Background removal logic
		// Placeholder implementation
		return inputImage;
	}

	private String encodeImageToBase64(BufferedImage image) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ImageIO.write(image, "png", baos);
		baos.flush();
		byte[] imageBytes = baos.toByteArray();
		baos.close();
		return Base64.getEncoder().encodeToString(imageBytes);
	}

	private void saveImageToS3(String image, String upi_id, String theme) throws IOException {
		byte[] imageBytes = Base64.getDecoder().decode(image);
		ByteArrayInputStream bais = new ByteArrayInputStream(imageBytes);
		s3Client.putObject(builder -> builder.bucket(bucketName).key("qart/" + upi_id.split("pa=")[1] + "/" + theme + "_qart.png"), RequestBody.fromInputStream(bais, imageBytes.length));
	}

	private String getPrompt(String theme) {
		int themeCode = Integer.parseInt(theme);
		switch (themeCode) {
		case 0:
			return "a cubism painting of a town with a lot of houses in the snow with a sky background, Andreas Rocha, matte painting concept art, a detailed matte painting";
		case 1:
			return "a cubism painting of a city with lots of small buildings and skyscrapers, bright daylight sky background, high quality, detailed";
		case 2:
			return "bamboo forest, bright daylight sky background, high quality, detailed";
		case 3:
			return "A photo-realistic rendering of a busy market, ((street vendors, fruits, vegetable, shops)), (Photorealistic:1.3), (Highly detailed:1.2), (Natural light:1.2), art inspired by Architectural Digest, Vogue Living, and Elle Decor";
		default:
			return "A photo-realistic rendering of a busy market, ((street vendors, fruits, vegetable, shops)), (Photorealistic:1.3), (Highly detailed:1.2), (Natural light:1.2), art inspired by Architectural Digest, Vogue Living, and Elle Decor";
		}
	}
}

