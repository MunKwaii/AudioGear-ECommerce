package vn.edu.ute.service.strategy;

import vn.edu.ute.dto.CategoryDTO;

import java.util.List;

@FunctionalInterface
public interface CategorySortStrategy {
    List<CategoryDTO> sort(List<CategoryDTO> categories);
}
