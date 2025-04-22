package com.glemora.glemora.api.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderDTO {
    private Long id;
    private LocalDateTime orderDate;
    private String customerName;
    private String customerEmail;
    private String shippingAddress;
    private String shippingMethod;
    private String paymentMethod;
    private Double subtotal;
    private Double tax;
    private Double total;
    private String notes;
    private List<OrderItemDTO> items;
}
