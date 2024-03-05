package com.example.productservice.controller;

import com.example.productservice.entity.Category;
import com.example.productservice.entity.Product;
import com.example.productservice.service.CategoryService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/category")
public class CategoryController {
    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping("/add-category")
    public ResponseEntity<String> addCategory(@Valid @RequestBody Category category) {
        Category addedCategory = categoryService.addCategory(category);
        return ResponseEntity.ok("Added category successfully! Id is: " + addedCategory.getId());
    }

    @GetMapping("/all")
    public ResponseEntity<List<Category>> getAllProducts() {
        List<Category> allCategories = categoryService.getAllProducts();
        return ResponseEntity.ok(allCategories);
    }

//    @GetMapping("/{categoryName}")
//    public ResponseEntity<Category> getCategory(@PathVariable @NotBlank String categoryName) {
//        return categoryService.getCategoryDetails(categoryName);
//    }
}
