package com.punit.AWSPe.service.helper;

import java.util.Base64;

public class ImageTypeDetector {

    public static String getFileType(String base64Data) {
        // Decode Base64 string to byte array
        byte[] fileBytes = Base64.getDecoder().decode(base64Data);

        // Determine the file type from the first few bytes
        String fileType = "unknown";

        if (isPNG(fileBytes)) {
            fileType = "image/png";
        } else if (isJPEG(fileBytes)) {
            fileType = "image/jpeg";
        } else if (isGIF(fileBytes)) {
            fileType = "image/gif";
        } else if (isBMP(fileBytes)) {
            fileType = "image/bmp";
        } else if (isWEBP(fileBytes)) {
            fileType = "image/webp";
        } else if (isPDF(fileBytes)) {
            fileType = "application/pdf";
        }
        System.out.println("image file type is " +  fileType);

        return fileType;
    }

    private static boolean isPNG(byte[] fileBytes) {
        return (fileBytes[0] == (byte) 0x89 && fileBytes[1] == (byte) 0x50 && fileBytes[2] == (byte) 0x4E && fileBytes[3] == (byte) 0x47);
    }

    private static boolean isJPEG(byte[] fileBytes) {
        return (fileBytes[0] == (byte) 0xFF && fileBytes[1] == (byte) 0xD8 && fileBytes[fileBytes.length - 2] == (byte) 0xFF && fileBytes[fileBytes.length - 1] == (byte) 0xD9);
    }

    private static boolean isGIF(byte[] fileBytes) {
        return (fileBytes[0] == (byte) 0x47 && fileBytes[1] == (byte) 0x49 && fileBytes[2] == (byte) 0x46);
    }

    private static boolean isBMP(byte[] fileBytes) {
        return (fileBytes[0] == (byte) 0x42 && fileBytes[1] == (byte) 0x4D);
    }

    private static boolean isWEBP(byte[] fileBytes) {
        return (fileBytes[0] == (byte) 0x52 && fileBytes[1] == (byte) 0x49 && fileBytes[2] == (byte) 0x46 && fileBytes[3] == (byte) 0x46 &&
                fileBytes[8] == (byte) 0x57 && fileBytes[9] == (byte) 0x45 && fileBytes[10] == (byte) 0x42 && fileBytes[11] == (byte) 0x50);
    }

    private static boolean isPDF(byte[] fileBytes) {
        return (fileBytes[0] == (byte) 0x25 && fileBytes[1] == (byte) 0x50 && fileBytes[2] == (byte) 0x44 && fileBytes[3] == (byte) 0x46);
    }

    public static void main(String[] args) {
        // Example Base64 data string (truncated for brevity)
        String base64Data = "iVBORw0KGgoAAAANSUhEUgAA..."; // Replace with actual Base64 string

        String fileType = getFileType(base64Data);
        System.out.println("The file type is: " + fileType);
    }
}
