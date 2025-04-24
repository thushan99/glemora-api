package com.glemora.glemora.api.controller.request;

import lombok.Data;

@Data
public class TryOnRequest {
    private String personImageUrl;
    private String garmentImageUrl;
}
