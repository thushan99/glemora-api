package com.glemora.glemora.api.controller.response;

import lombok.Data;

@Data
public class TryOnUserProductResponse {
    private String generatedImageUrl;
    private Long productId;
    private Long userId;
    private String uploadedImagePath;
}
