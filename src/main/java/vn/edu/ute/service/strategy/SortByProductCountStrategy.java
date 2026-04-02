package vn.edu.ute.service.strategy;

import vn.edu.ute.dto.CategoryDTO;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class SortByProductCountStrategy implements CategorySortStrategy {

    @Override
    public List<CategoryDTO> sort(List<CategoryDTO> categories) {
        return categories.stream()
                .sorted(Comparator.comparingLong(CategoryDTO::getProductCount).reversed())
                .collect(Collectors.toList());
    }
}
