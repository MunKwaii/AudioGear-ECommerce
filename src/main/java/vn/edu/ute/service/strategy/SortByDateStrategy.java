package vn.edu.ute.service.strategy;

import vn.edu.ute.dto.CategoryDTO;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class SortByDateStrategy implements CategorySortStrategy {

    @Override
    public List<CategoryDTO> sort(List<CategoryDTO> categories) {
        // Sắp xếp theo createdAt giảm dần (mới nhất lên đầu)
        // createdAt là String dạng "dd/MM/yyyy HH:mm" nên so sánh ngược
        return categories.stream()
                .sorted(Comparator.comparing(
                        CategoryDTO::getCreatedAt,
                        Comparator.nullsLast(Comparator.reverseOrder())
                ))
                .collect(Collectors.toList());
    }
}
