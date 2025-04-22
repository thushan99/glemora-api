// Order.java
package com.glemora.glemora.api.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
@Data
@EqualsAndHashCode(exclude = {"user", "shippingAddress"})
@ToString(exclude = {"user", "shippingAddress"})
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "order_date")
    private LocalDateTime orderDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shipping_address_id", nullable = false)
    private UserAddress shippingAddress;

    @Column(name = "shipping_method")
    private String shippingMethod;

    @Column(name = "payment_method")
    private String paymentMethod;

    @Column(nullable = false)
    private Double subtotal;

    @Column(nullable = false)
    private Double tax;

    @Column(nullable = false)
    private Double total;

    private String notes;

    @PrePersist
    protected void onCreate() {
        this.orderDate = LocalDateTime.now();
    }
}
