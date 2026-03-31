package vn.edu.ute.dao;

import vn.edu.ute.entity.Category;
import java.util.List;

public interface CategoryDao {
    /**
     * Lấy danh sách tất cả danh mục (có thể giới hạn top danh mục)
     * @return List<Category>
     */
    List<Category> getAllCategories();
}
