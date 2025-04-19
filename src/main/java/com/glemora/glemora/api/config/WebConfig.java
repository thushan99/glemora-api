package com.glemora.glemora.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class WebConfig {

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();

        // Allow all origins for development (restrict this in production)
        config.addAllowedOrigin("http://localhost:3000");

        // Allow all HTTP methods (GET, POST, PUT, DELETE, etc.)
        config.addAllowedMethod("*");

        // Allow all headers
        config.addAllowedHeader("*");

        // Allow credentials like cookies, authorization headers
        config.setAllowCredentials(true);

        // Add exposed headers if needed
        config.addExposedHeader("Access-Control-Allow-Origin");

        // Increase max age for preflight requests to reduce options requests
        config.setMaxAge(3600L);

        source.registerCorsConfiguration("/api/**", config);
        return new CorsFilter(source);
    }
}
