// CartDTO.java
package com.glemora.glemora.api.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class CartDTO {
    private Long id;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<CartItemDTO> items;
    private double totalPrice;
}