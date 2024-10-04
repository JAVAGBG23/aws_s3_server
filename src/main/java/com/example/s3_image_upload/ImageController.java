package com.example.s3_image_upload;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class ImageController {
    @Autowired
    private AmazonS3 amazonS3;

    @Value("${aws.s3.bucket}")
    private String bucketName;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadImage(@RequestBody Map<String, String> request) {
        try {
            String image = request.get("image");
            if(image == null || image.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No image");
            }

            // prepare image
            String base64Image = image.replaceFirst("^data:image/\\w+;base64,", "");
            byte[] imageBytes = Base64.getDecoder().decode(base64Image);


            // get the image type
            String imageType = image.substring(image.indexOf("/") + 1, image.indexOf(";"));

            // generate unique key
            String key = UUID.randomUUID().toString() + "." + imageType;

            // create metadata
            ObjectMetadata objectMetadata = new ObjectMetadata();
            objectMetadata.setContentLength(imageBytes.length);
            objectMetadata.setContentType("image/" + imageType);

            // create input stream
            ByteArrayInputStream inpputStream = new ByteArrayInputStream(imageBytes);

            // upload to s3
            amazonS3.putObject(bucketName, key, inpputStream, objectMetadata);

            // build the response data
            Map<String, String> responseData = new HashMap<>();
            responseData.put("Etag", "");
            responseData.put("Location", amazonS3.getUrl(bucketName, key).toString());
            responseData.put("key", key);
            responseData.put("bucket", bucketName);

            return ResponseEntity.ok(responseData);



        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Image upload failed.");
        }
    }
}























