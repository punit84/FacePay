package com.punit.AWSPe.service.helper;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Map;

import javax.imageio.ImageIO;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

public class QRGenerator {
	
	public static void main(String[] args) {
		QRGenerator sample = new QRGenerator();
		try {
			String upi= "upi://pay?pa=9911078929@paytm&pn=PaytmUser&cu=INR";
			BufferedImage im=sample.generateQRCode(upi);
			sample.saveQRCodeLocally(im, upi);
			
		} catch (IOException | WriterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private BufferedImage generateQRCode(String upi_id) throws IOException, WriterException {
		Map<EncodeHintType, Object> hints = new java.util.EnumMap<>(EncodeHintType.class);
		hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
		hints.put(EncodeHintType.MARGIN, 1);
		QRCodeWriter qrCodeWriter = new QRCodeWriter();
		BitMatrix bitMatrix = qrCodeWriter.encode(upi_id, BarcodeFormat.QR_CODE, 200, 200, hints);
		return MatrixToImageWriter.toBufferedImage(bitMatrix);
	}

	private void saveQRCodeLocally(BufferedImage qrImage, String upi_id) throws IOException {
		File qrFile = new File("./"+ "_qr.png");
		ImageIO.write(qrImage, "png", qrFile);
		System.out.println("QR Code saved locally at: " + qrFile.getAbsolutePath());
	}

}


