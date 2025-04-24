package com.glemora.glemora.api.controller.request;

import lombok.Data;

@Data
public class OrderRequest {
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String state;
    private String postalCode;
    private String country;
    private Boolean isDefaultAddress;
    private String shippingMethod;
    private Double shippingCost;
    private String paymentMethod;
    private String notes;
}
