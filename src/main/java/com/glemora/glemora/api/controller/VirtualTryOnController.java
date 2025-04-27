package com.glemora.glemora.api.controller;

import java.io.IOException;
import java.util.Map;

import com.glemora.glemora.api.controller.response.TryOnUserProductResponse;
import com.glemora.glemora.api.service.Impl.VirtualTryOnServiceImpl;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.glemora.glemora.api.controller.request.TryOnRequest;
import com.glemora.glemora.api.controller.response.TryOnResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/tryon")
@RequiredArgsConstructor
@Slf4j
public class VirtualTryOnController {

    private final VirtualTryOnServiceImpl virtualTryOnService;

    @PostMapping
    @RolesAllowed({"USER", "ADMIN"})
    public ResponseEntity<TryOnResponse> tryOnGarment(@RequestBody TryOnRequest request) {
        TryOnResponse response = virtualTryOnService.tryOnGarment(request.getPersonImageUrl(), request.getGarmentImageUrl());
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @RolesAllowed({"USER", "ADMIN"})
    public ResponseEntity<TryOnResponse> tryOnGarmentWithUpload(@RequestParam("personImage") MultipartFile personImage, @RequestParam("garmentImage") MultipartFile garmentImage) throws IOException {

        log.info("Received file uploads for try-on: person image size: {}, garment image size: {}", personImage.getSize(), garmentImage.getSize());

        Map<String, String> uploadedImages = virtualTryOnService.uploadAndResizeImages(personImage, garmentImage);
        TryOnResponse response = virtualTryOnService.tryOnGarment(uploadedImages.get("personImageUrl"), uploadedImages.get("garmentImageUrl"));
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/user-product", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @RolesAllowed({"USER", "ADMIN"})
    public ResponseEntity<TryOnUserProductResponse> tryOnProductWithUserImage(@RequestParam("userImage") MultipartFile userImage, @RequestParam("productId") Long productId, @AuthenticationPrincipal UserDetails userDetails) throws IOException {

        log.info("Received user image upload for product try-on: user image size: {}, product id: {}", userImage.getSize(), productId);

        TryOnUserProductResponse response = virtualTryOnService.tryOnProductWithUserImage(userImage, productId, userDetails.getUsername());
        return ResponseEntity.ok(response);
    }
}