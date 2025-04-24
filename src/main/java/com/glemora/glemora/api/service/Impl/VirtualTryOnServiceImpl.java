package com.glemora.glemora.api.service.Impl;

import com.glemora.glemora.api.config.PixelcutConfig;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.glemora.glemora.api.controller.response.TryOnResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class VirtualTryOnServiceImpl {

    private final PixelcutConfig properties;
    private final RestTemplate restTemplate;
    private final Cloudinary cloudinary;

    // Maximum image dimensions allowed
    private static final int MAX_IMAGE_DIMENSION = 6000;

    public TryOnResponse tryOnGarment(String personImageUrl, String garmentImageUrl) {
        String url = properties.getApiBaseUrl() + "/v1/try-on";

        log.info("Making try-on API call with person image: {} and garment image: {}", personImageUrl, garmentImageUrl);

        // Create headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.set("X-API-KEY", properties.getApiKey());

        // Create request body as a Map
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("person_image_url", personImageUrl);
        requestBody.put("garment_image_url", garmentImageUrl);

        // Create HTTP entity with headers and body
        HttpEntity<Map<String, String>> entity = new HttpEntity<>(requestBody, headers);

        try {
            // Make the request and get the full response with headers and body
            ResponseEntity<Map> responseEntity = restTemplate.postForEntity(url, entity, Map.class);

            // Log the entire response for debugging
            log.info("Response status: {}", responseEntity.getStatusCode());
            log.info("Response body: {}", responseEntity.getBody());

            if (responseEntity.getBody() != null && responseEntity.getBody().containsKey("result_url")) {
                TryOnResponse response = new TryOnResponse();
                response.setResultUrl((String) responseEntity.getBody().get("result_url"));
                log.info("Extracted result URL: {}", response.getResultUrl());
                return response;
            } else {
                log.warn("Response body doesn't contain result_url field: {}", responseEntity.getBody());
                TryOnResponse errorResponse = new TryOnResponse();
                // Set a default error message if the API didn't provide a proper response
                errorResponse.setResultUrl("No result URL provided in API response");
                return errorResponse;
            }
        } catch (Exception e) {
            log.error("Error calling try-on API: {}", e.getMessage(), e);
            throw e;
        }
    }

    public Map<String, String> uploadAndResizeImages(MultipartFile personImage, MultipartFile garmentImage) throws IOException {
        // Resize images if necessary
        byte[] resizedPersonImage = resizeImageIfNeeded(personImage.getBytes(), getFileExtension(personImage.getOriginalFilename()));
        byte[] resizedGarmentImage = resizeImageIfNeeded(garmentImage.getBytes(), getFileExtension(garmentImage.getOriginalFilename()));

        // Upload resized images to Cloudinary
        Map<?, ?> personUploadResult = cloudinary.uploader().upload(resizedPersonImage, ObjectUtils.asMap("folder", "glemora/tryon/person"));

        Map<?, ?> garmentUploadResult = cloudinary.uploader().upload(resizedGarmentImage, ObjectUtils.asMap("folder", "glemora/tryon/garment"));

        String personImageUrl = (String) personUploadResult.get("secure_url");
        String garmentImageUrl = (String) garmentUploadResult.get("secure_url");

        log.info("Uploaded resized images to Cloudinary: person: {}, garment: {}", personImageUrl, garmentImageUrl);

        Map<String, String> result = new HashMap<>();
        result.put("personImageUrl", personImageUrl);
        result.put("garmentImageUrl", garmentImageUrl);
        return result;
    }

    private byte[] resizeImageIfNeeded(byte[] imageData, String formatName) throws IOException {
        BufferedImage originalImage = ImageIO.read(new ByteArrayInputStream(imageData));

        // Check if resizing is needed
        if (originalImage.getWidth() <= MAX_IMAGE_DIMENSION && originalImage.getHeight() <= MAX_IMAGE_DIMENSION) {
            return imageData; // No resizing needed
        }

        // Calculate new dimensions while maintaining aspect ratio
        int newWidth = originalImage.getWidth();
        int newHeight = originalImage.getHeight();

        if (newWidth > MAX_IMAGE_DIMENSION) {
            float aspectRatio = (float) originalImage.getHeight() / originalImage.getWidth();
            newWidth = MAX_IMAGE_DIMENSION;
            newHeight = Math.round(MAX_IMAGE_DIMENSION * aspectRatio);
        }

        if (newHeight > MAX_IMAGE_DIMENSION) {
            float aspectRatio = (float) originalImage.getWidth() / originalImage.getHeight();
            newHeight = MAX_IMAGE_DIMENSION;
            newWidth = Math.round(MAX_IMAGE_DIMENSION * aspectRatio);
        }

        // Create resized image
        BufferedImage resizedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = resizedImage.createGraphics();
        g.drawImage(originalImage, 0, 0, newWidth, newHeight, null);
        g.dispose();

        // Convert resized image to byte array
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(resizedImage, formatName, outputStream);

        log.info("Resized image from {}x{} to {}x{}", originalImage.getWidth(), originalImage.getHeight(), newWidth, newHeight);

        return outputStream.toByteArray();
    }

    private String getFileExtension(String filename) {
        if (filename == null) return "jpg";
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex > 0) {
            return filename.substring(lastDotIndex + 1).toLowerCase();
        }
        return "jpg"; // Default to jpg if no extension found
    }
}