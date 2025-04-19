package com.glemora.glemora.api.service.Impl;

import com.cloudinary.Cloudinary;
import com.glemora.glemora.api.model.Category;
import com.glemora.glemora.api.repository.CategoryRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
public class CategoryService {

    private CategoryRepository categoryRepository;
    private final Cloudinary cloudinary;

    private final String uploadDir = "uploads/categories/";

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    public Optional<Category> getCategoryById(Long id) {
        return categoryRepository.findById(id);
    }

    public Category saveCategory(Category category, MultipartFile imageFile) throws IOException {

        if (imageFile.isEmpty())
            category.setImage("N/A");

        else {
            String categoryPic = cloudinary.uploader()
                    .upload(imageFile.getBytes(),
                            Map.of("public_id", UUID.randomUUID().toString()))
                    .get("url")
                    .toString();

            category.setImage(categoryPic);
        }
        return categoryRepository.save(category);
    }

    public Category updateCategory(Category category, MultipartFile imageFile) throws IOException {
        Category existingCategory = categoryRepository.findById(category.getId())
                .orElseThrow(() -> new RuntimeException("Category not found"));

        // Update fields
        existingCategory.setName(category.getName());
        existingCategory.setDisplayName(category.getDisplayName());

        // Handle image update
        if (imageFile.isEmpty())
            existingCategory.setImage("N/A");

        else {
            String productPic = cloudinary.uploader()
                    .upload(imageFile.getBytes(),
                            Map.of("public_id", UUID.randomUUID().toString()))
                    .get("url")
                    .toString();

            existingCategory.setImage(productPic);
        }

        return categoryRepository.save(existingCategory);
    }

    public void deleteCategory(Long id) {
        categoryRepository.deleteById(id);
    }

}
