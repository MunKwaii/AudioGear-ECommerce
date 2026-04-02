package vn.edu.ute.dao;

import vn.edu.ute.entity.Category;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface CategoryDao {
    Optional<Category> findById(Long id);
    Optional<Category> findByIdWithChildren(Long id);
    List<Category> findAll();
    List<Category> getAllCategories();
    List<Category> findRootCategories();
    Category save(Category category);
    Category update(Category category);
    void detachChildren(Long parentId);
    void delete(Long id);
    long countProductsByCategory(Long categoryId);
    Map<Long, Long> getProductCountsGroupedByCategory();
}
