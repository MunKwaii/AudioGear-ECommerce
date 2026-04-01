package vn.edu.ute.dao;

import vn.edu.ute.entity.Category;

import java.util.List;
import java.util.Optional;

public interface CategoryDao {
    Optional<Category> findById(Long id);
    List<Category> findAll();
}
