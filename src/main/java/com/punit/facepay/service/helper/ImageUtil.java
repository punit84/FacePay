package com.punit.facepay.service.helper;

import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.rekognition.model.Image;

import java.io.IOException;

@Component
public class ImageUtil {

    public static Image getImage(byte[] imageToCheck) throws IOException {

        Image souImage = Image.builder()
                .bytes(SdkBytes.fromByteArray(imageToCheck))
                .build();
        return souImage;
    }

}
