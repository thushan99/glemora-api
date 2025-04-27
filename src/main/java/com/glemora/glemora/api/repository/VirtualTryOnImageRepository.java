package com.glemora.glemora.api.repository;

import com.glemora.glemora.api.model.Product;
import com.glemora.glemora.api.model.User;
import com.glemora.glemora.api.model.VirtualTryOnImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VirtualTryOnImageRepository extends JpaRepository<VirtualTryOnImage, Long> {

    List<VirtualTryOnImage> findByUser(User user);
    List<VirtualTryOnImage> findByProduct(Product product);
    List<VirtualTryOnImage> findByUserAndProduct(User user, Product product);
}
