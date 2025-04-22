package com.glemora.glemora.api.controller;

import com.glemora.glemora.api.model.Category;
import com.glemora.glemora.api.service.Impl.CategoryServiceImpl;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    @Autowired
    private CategoryServiceImpl categoryServiceImpl;

    @RolesAllowed({"ADMIN", "USER"})
    @GetMapping(headers = "X-Api-Version=v1")
    public List<Category> getAllCategories() {
        return categoryServiceImpl.getAllCategories();
    }

    @RolesAllowed({"ADMIN", "USER"})
    @GetMapping(value = "/{id}", headers = "X-Api-Version=v1")
    public ResponseEntity<Category> getCategoryById(@PathVariable Long id) {
        return categoryServiceImpl.getCategoryById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @RolesAllowed({"ADMIN"})
    @PostMapping(headers = "X-Api-Version=v1")
    public Category createCategory(
            @RequestParam("name") String name,
            @RequestParam("displayName") String displayName,
            @RequestParam(value = "image", required = false) MultipartFile image
    ) throws IOException {
        Category category = new Category();
        category.setName(name);
        category.setDisplayName(displayName);

        return categoryServiceImpl.saveCategory(category, image);
    }

    @RolesAllowed({"ADMIN"})
    @PutMapping(value = "/{id}", headers = "X-Api-Version=v1")
    public ResponseEntity<Category> updateCategory(
            @PathVariable Long id,
            @RequestParam("name") String name,
            @RequestParam("displayName") String displayName,
            @RequestParam(value = "image", required = false) MultipartFile image
    ) throws IOException {
        Category category = new Category();
        category.setId(id);
        category.setName(name);
        category.setDisplayName(displayName);

        return ResponseEntity.ok(categoryServiceImpl.updateCategory(category, image));
    }

    @RolesAllowed({"ADMIN"})
    @DeleteMapping(value = "/{id}", headers = "X-Api-Version=v1")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        categoryServiceImpl.deleteCategory(id);
        return ResponseEntity.ok().build();
    }
}
