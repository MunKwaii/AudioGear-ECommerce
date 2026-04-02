package vn.edu.ute.service;

import vn.edu.ute.dto.CategoryDTO;

import java.util.List;

public interface CategoryService {
    List<CategoryDTO> getAllCategoriesAsTree();
    List<CategoryDTO> getAllCategoriesFlat();
    CategoryDTO getCategoryById(Long id);
    CategoryDTO createCategory(String name, String description, Long parentId);
    CategoryDTO updateCategory(Long id, String name, String description, Long parentId);
    void deleteCategory(Long id);
    List<CategoryDTO> getRootCategories();
}
