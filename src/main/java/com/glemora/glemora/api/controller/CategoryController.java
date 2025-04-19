package com.glemora.glemora.api.controller;

import com.glemora.glemora.api.model.Category;
import com.glemora.glemora.api.service.Impl.CategoryService;
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
    private CategoryService categoryService;

    @RolesAllowed({"ADMIN", "USER"})
    @GetMapping(headers = "X-Api-Version=v1")
    public List<Category> getAllCategories() {
        return categoryService.getAllCategories();
    }

    @RolesAllowed({"ADMIN", "USER"})
    @GetMapping(value = "/{id}", headers = "X-Api-Version=v1")
    public ResponseEntity<Category> getCategoryById(@PathVariable Long id) {
        return categoryService.getCategoryById(id)
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

        return categoryService.saveCategory(category, image);
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

        return ResponseEntity.ok(categoryService.updateCategory(category, image));
    }

    @RolesAllowed({"ADMIN"})
    @DeleteMapping(value = "/{id}", headers = "X-Api-Version=v1")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.ok().build();
    }
}
