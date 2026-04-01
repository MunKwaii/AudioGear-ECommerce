package vn.edu.ute.dao;

import vn.edu.ute.entity.Category;

import java.util.List;
import java.util.Optional;

public interface CategoryDao {
    Optional<Category> findById(Long id);
    List<Category> findAll();
    /**
     * Lấy danh sách tất cả danh mục (có thể giới hạn top danh mục)
     * @return List<Category>
     */
    List<Category> getAllCategories();
}
