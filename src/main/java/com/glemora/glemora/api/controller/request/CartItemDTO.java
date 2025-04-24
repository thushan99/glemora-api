package com.glemora.glemora.api.controller.request;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CartItemDTO {
    private Long id;
    private Long productId;
    private String productName;
    private String productImage;
    private Double price;
    private Integer quantity;
    private String size;
    private LocalDateTime addedAt;
}
