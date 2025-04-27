package com.glemora.glemora.api.service.Impl;

import com.glemora.glemora.api.config.PixelcutConfig;
import com.glemora.glemora.api.controller.response.TryOnUserProductResponse;
import com.glemora.glemora.api.model.Product;
import com.glemora.glemora.api.model.User;
import com.glemora.glemora.api.model.VirtualTryOnImage;
import com.glemora.glemora.api.repository.ProductRepository;
import com.glemora.glemora.api.repository.UserRepository;
import com.glemora.glemora.api.repository.VirtualTryOnImageRepository;
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
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final VirtualTryOnImageRepository virtualTryOnImageRepository;

    private static final int MAX_IMAGE_DIMENSION = 5900;

    public TryOnResponse tryOnGarment(String personImageUrl, String garmentImageUrl) {
        String url = properties.getApiBaseUrl() + "/v1/try-on";

        log.info("Making try-on API call with person image: {} and garment image: {}", personImageUrl, garmentImageUrl);

        try {
            personImageUrl = checkAndResizeRemoteImageIfNeeded(personImageUrl);
            garmentImageUrl = checkAndResizeRemoteImageIfNeeded(garmentImageUrl);

            log.info("After resizing check: person image: {}, garment image: {}", personImageUrl, garmentImageUrl);
        } catch (IOException e) {
            log.error("Error resizing remote images: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to process images for try-on", e);
        }

        // Create headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.set("X-API-KEY", properties.getApiKey());

        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("person_image_url", personImageUrl);
        requestBody.put("garment_image_url", garmentImageUrl);

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(requestBody, headers);

        try {

            ResponseEntity<Map> responseEntity = restTemplate.postForEntity(url, entity, Map.class);

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
                errorResponse.setResultUrl("No result URL provided in API response");
                return errorResponse;
            }
        } catch (Exception e) {
            log.error("Error calling try-on API: {}", e.getMessage(), e);
            throw e;
        }
    }

    private String checkAndResizeRemoteImageIfNeeded(String imageUrl) throws IOException {

        log.info("Checking dimensions of remote image: {}", imageUrl);

        try {

            java.net.URL url = new java.net.URL(imageUrl);
            BufferedImage originalImage = ImageIO.read(url);

            if (originalImage == null) {
                log.warn("Could not read image from URL: {}", imageUrl);
                return imageUrl;
            }

            if (originalImage.getWidth() <= MAX_IMAGE_DIMENSION && originalImage.getHeight() <= MAX_IMAGE_DIMENSION) {
                log.info("Image dimensions are within limits: {}x{}", originalImage.getWidth(), originalImage.getHeight());
                return imageUrl;
            }

            log.info("Image needs resizing, current dimensions: {}x{}", originalImage.getWidth(), originalImage.getHeight());

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

            BufferedImage resizedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = resizedImage.createGraphics();
            g.drawImage(originalImage, 0, 0, newWidth, newHeight, null);
            g.dispose();

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(resizedImage, "jpg", outputStream);

            Map<?, ?> uploadResult = cloudinary.uploader().upload(
                    outputStream.toByteArray(),
                    ObjectUtils.asMap("folder", "glemora/tryon/resized")
            );

            String resizedImageUrl = (String) uploadResult.get("secure_url");
            log.info("Uploaded resized image to Cloudinary: {} ({}x{})", resizedImageUrl, newWidth, newHeight);

            return resizedImageUrl;

        } catch (IOException e) {
            log.error("Error processing remote image: {}", e.getMessage(), e);
            throw e;
        }
    }

    public Map<String, String> uploadAndResizeImages(MultipartFile personImage, MultipartFile garmentImage) throws IOException {

        byte[] resizedPersonImage = resizeImageIfNeeded(personImage.getBytes(), getFileExtension(personImage.getOriginalFilename()));
        byte[] resizedGarmentImage = resizeImageIfNeeded(garmentImage.getBytes(), getFileExtension(garmentImage.getOriginalFilename()));

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

        if (originalImage.getWidth() <= MAX_IMAGE_DIMENSION && originalImage.getHeight() <= MAX_IMAGE_DIMENSION) {
            return imageData;
        }

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

        BufferedImage resizedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = resizedImage.createGraphics();
        g.drawImage(originalImage, 0, 0, newWidth, newHeight, null);
        g.dispose();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(resizedImage, formatName, outputStream);

        log.info("Resized image from {}x{} to {}x{}", originalImage.getWidth(), originalImage.getHeight(), newWidth, newHeight);

        return outputStream.toByteArray();
    }

    public TryOnUserProductResponse tryOnProductWithUserImage(MultipartFile userImage, Long productId, String username) throws IOException {

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));

        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new RuntimeException("User not found with username: " + username);
        }

        log.info("Processing try-on for user ID: {} and product ID: {}", user.getId(), productId);

        byte[] resizedUserImage = resizeImageIfNeeded(userImage.getBytes(), getFileExtension(userImage.getOriginalFilename()));

        Map<?, ?> userImageUploadResult = cloudinary.uploader().upload(resizedUserImage,
                ObjectUtils.asMap("folder", "glemora/tryon/users/" + user.getId()));

        String userImageUrl = (String) userImageUploadResult.get("secure_url");
        log.info("Uploaded user image to Cloudinary: {}", userImageUrl);

        String productImageUrl = product.getPngTryOnImage();
        if (productImageUrl == null || productImageUrl.isEmpty()) {
            throw new RuntimeException("Product does not have a try-on image");
        }

        TryOnResponse tryOnResponse = tryOnGarment(userImageUrl, productImageUrl);

        VirtualTryOnImage virtualTryOnImage = new VirtualTryOnImage();
        virtualTryOnImage.setProduct(product);
        virtualTryOnImage.setUser(user);
        virtualTryOnImage.setUploadImagePath(userImageUrl);
        virtualTryOnImage.setGeneratedImagePath(tryOnResponse.getResultUrl());

        virtualTryOnImageRepository.save(virtualTryOnImage);

        TryOnUserProductResponse response = new TryOnUserProductResponse();
        response.setGeneratedImageUrl(tryOnResponse.getResultUrl());
        response.setProductId(product.getId());
        response.setUserId(user.getId());
        response.setUploadedImagePath(userImageUrl);

        return response;
    }

    private String getFileExtension(String filename) {
        if (filename == null) return "jpg";
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex > 0) {
            return filename.substring(lastDotIndex + 1).toLowerCase();
        }
        return "jpg";
    }
}