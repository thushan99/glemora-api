package com.glemora.glemora.api.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

@Component
@ConfigurationProperties(prefix = "pixelcut")
@Data
public class PixelcutConfig {
    private String apiKey;
    private String apiBaseUrl = "https://api.developer.pixelcut.ai";
}




