package com.glemora.glemora.api.repository;

import com.glemora.glemora.api.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}
