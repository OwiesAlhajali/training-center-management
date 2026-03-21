package com.trainingcenter.management.controller;

import com.trainingcenter.management.dto.CategoryResponseDTO;
import com.trainingcenter.management.dto.CategoryRequestDTO;
import com.trainingcenter.management.entity.Category;
import com.trainingcenter.management.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/categories")

public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    @PostMapping
    public CategoryResponseDTO createCategory(@RequestBody CategoryRequestDTO request){

        Category category = new Category();
        category.setName(request.getName());

        Category savedCategory = categoryService.createCategory(category);

        CategoryResponseDTO response = new CategoryResponseDTO();
        response.setId(savedCategory.getId());
        response.setName(savedCategory.getName());

        return response;
    }

    @GetMapping
    public List<CategoryResponseDTO> getAllCategories(){

        List<Category> categories = categoryService.getAllCategories();

        return categories.stream().map(category -> {
            CategoryResponseDTO response = new CategoryResponseDTO();
            response.setId(category.getId());
            response.setName(category.getName());
            return response;
        }).collect(Collectors.toList());
    }
    @DeleteMapping("/{id}")
    public void deleteCategory(@PathVariable Long id){
        categoryService.deleteCategory(id);
    }
}
