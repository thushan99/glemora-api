package com.glemora.glemora.api.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    private Double price;

    private String image;

    @Column(name = "stock_quantity", nullable = false)
    private Integer stockQuantity = 0;

    @Column(columnDefinition = "boolean default false")
    private Boolean sale = false;

    @Column(columnDefinition = "boolean default false")
    private Boolean featured = false;

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;
}
