package com.glemora.glemora.api.service.Impl;

import com.cloudinary.Cloudinary;
import com.glemora.glemora.api.model.Category;
import com.glemora.glemora.api.model.Product;
import com.glemora.glemora.api.repository.ProductRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
public class ProductService {

    private ProductRepository productRepository;
    private final Cloudinary cloudinary;


    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    public Product saveProduct(Product product, MultipartFile imageFile) throws IOException {

        if (imageFile.isEmpty())
            product.setImage("N/A");

        else {
            String productPic = cloudinary.uploader()
                    .upload(imageFile.getBytes(),
                            Map.of("public_id", UUID.randomUUID().toString()))
                    .get("url")
                    .toString();

            product.setImage(productPic);
        }

        return productRepository.save(product);
    }

    public Product updateProduct(Long id, String name, String description, Double price,
                                 Category category, Boolean sale, Boolean featured,
                                 MultipartFile imageFile, Integer stockQuantity) throws IOException {
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));

        // Update fields
        existingProduct.setName(name);
        existingProduct.setDescription(description);
        existingProduct.setPrice(price);
        existingProduct.setCategory(category);
        existingProduct.setSale(sale != null && sale);
        existingProduct.setFeatured(featured != null && featured);

        if (stockQuantity != null) {
            existingProduct.setStockQuantity(stockQuantity);
        }

        if (imageFile.isEmpty())
            existingProduct.setImage("N/A");

        else {
            String productPic = cloudinary.uploader()
                    .upload(imageFile.getBytes(),
                            Map.of("public_id", UUID.randomUUID().toString()))
                    .get("url")
                    .toString();

            existingProduct.setImage(productPic);
        }

        return productRepository.save(existingProduct);
    }

    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }
}