package com.glemora.glemora.api.repository;

import com.glemora.glemora.api.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}
