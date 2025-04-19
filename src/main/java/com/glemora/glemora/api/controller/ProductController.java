package com.glemora.glemora.api.controller;

import com.glemora.glemora.api.model.Category;
import com.glemora.glemora.api.model.Product;
import com.glemora.glemora.api.repository.CategoryRepository;
import com.glemora.glemora.api.service.Impl.ProductService;
import jakarta.annotation.security.RolesAllowed;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/products")
@AllArgsConstructor
public class ProductController {

    private ProductService productService;
    private CategoryRepository categoryRepository;

    @RolesAllowed({"ADMIN", "USER"})
    @GetMapping(headers = "X-Api-Version=v1")
    public List<Product> getAllProducts() {
        return productService.getAllProducts();
    }

    @RolesAllowed({"ADMIN", "USER"})
    @GetMapping(value = "/{id}", headers = "X-Api-Version=v1")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        return productService.getProductById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping(headers = "X-Api-Version=v1")
    @RolesAllowed({"ADMIN"})
    public Product createProduct(
            @RequestParam("name") String name,
            @RequestParam("description") String description,
            @RequestParam("price") Double price,
            @RequestParam("category") Long categoryId,
            @RequestParam(value = "sale", required = false) Boolean sale,
            @RequestParam(value = "featured", required = false) Boolean featured,
            @RequestParam(value = "image", required = false) MultipartFile image,
            @RequestParam(value = "stockQuantity", required = false, defaultValue = "0") Integer stockQuantity
    ) throws IOException {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid category ID: " + categoryId));

        Product product = new Product();
        product.setName(name);
        product.setDescription(description);
        product.setPrice(price);
        product.setCategory(category);
        product.setSale(sale != null && sale);
        product.setFeatured(featured != null && featured);
        product.setStockQuantity(stockQuantity);

        return productService.saveProduct(product, image);
    }

    @RolesAllowed({"ADMIN"})
    @PutMapping(value = "/{id}", headers = "X-Api-Version=v1")
    public ResponseEntity<Product> updateProduct(
            @PathVariable Long id,
            @RequestParam("name") String name,
            @RequestParam("description") String description,
            @RequestParam("price") Double price,
            @RequestParam("category") Long categoryId,
            @RequestParam(value = "sale", required = false) Boolean sale,
            @RequestParam(value = "featured", required = false) Boolean featured,
            @RequestParam(value = "image", required = false) MultipartFile image,
            @RequestParam(value = "stockQuantity", required = false) Integer stockQuantity
    ) throws IOException {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid category ID: " + categoryId));

        return ResponseEntity.ok(productService.updateProduct(id, name, description, price,
                category, sale, featured, image, stockQuantity));
    }

    @RolesAllowed({"ADMIN"})
    @DeleteMapping(value = "/{id}", headers = "X-Api-Version=v1")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok().build();
    }
}
