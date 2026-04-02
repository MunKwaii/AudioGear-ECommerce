package vn.edu.ute.service.impl;

import vn.edu.ute.dao.CategoryDao;
import vn.edu.ute.dao.impl.CategoryDaoImpl;
import vn.edu.ute.dto.CategoryDTO;
import vn.edu.ute.entity.Category;
import vn.edu.ute.service.CategoryService;
import vn.edu.ute.service.strategy.CategorySortStrategy;
import vn.edu.ute.service.strategy.SortByNameStrategy;

import java.util.*;
import java.util.stream.Collectors;

public class CategoryServiceImpl implements CategoryService {

    private final CategoryDao categoryDao = CategoryDaoImpl.getInstance();
    private CategorySortStrategy sortStrategy = new SortByNameStrategy();

    public void setSortStrategy(CategorySortStrategy strategy) {
        this.sortStrategy = strategy;
    }

    @Override
    public List<CategoryDTO> getAllCategoriesAsTree() {
        List<Category> allCategories = categoryDao.findAll();
        Map<Long, Long> countsMap = categoryDao.getProductCountsGroupedByCategory();
        
        List<CategoryDTO> flatDtos = allCategories.stream()
                .map(c -> toDTO(c, countsMap))
                .collect(Collectors.toList());

        return buildTree(flatDtos);
    }

    @Override
    public List<CategoryDTO> getAllCategoriesFlat() {
        List<Category> allCategories = categoryDao.findAll();
        Map<Long, Long> countsMap = categoryDao.getProductCountsGroupedByCategory();
        
        return allCategories.stream()
                .map(c -> toDTO(c, countsMap))
                .collect(Collectors.toList());
    }

    @Override
    public CategoryDTO getCategoryById(Long id) {
        return categoryDao.findById(id)
                .map(c -> toDTO(c, categoryDao.countProductsByCategory(id)))
                .orElse(null);
    }

    @Override
    public CategoryDTO createCategory(String name, String description, Long parentId) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Tên danh mục không được để trống");
        }

        Category category = new Category();
        category.setName(name.trim());
        category.setDescription(description);

        if (parentId != null) {
            Category parent = categoryDao.findById(parentId)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy danh mục cha"));
            category.setParent(parent);
        }

        Category saved = categoryDao.save(category);
        return toDTO(saved, 0L);
    }

    @Override
    public CategoryDTO updateCategory(Long id, String name, String description, Long parentId) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Tên danh mục không được để trống");
        }

        Category category = categoryDao.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy danh mục"));

        if (parentId != null) {
            if (parentId.equals(id)) {
                throw new IllegalArgumentException("Danh mục không thể là cha của chính nó");
            }
            Category parent = categoryDao.findById(parentId)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy danh mục cha"));
            category.setParent(parent);
        } else {
            category.setParent(null);
        }

        category.setName(name.trim());
        category.setDescription(description);

        // Kiểm tra vòng lặp nếu có đổi parent
        if (parentId != null) {
            Map<Long, CategoryDTO> dtoMap = categoryDao.findAll().stream()
                    .collect(Collectors.toMap(Category::getId, c -> toDTO(c, 0L)));
            if (isAncestor(id, parentId, dtoMap)) {
                throw new IllegalArgumentException("Không thể chọn danh mục con làm danh mục cha");
            }
        }

        Category updated = categoryDao.update(category);
        return toDTO(updated, categoryDao.countProductsByCategory(id));
    }

    private boolean isAncestor(Long potentialAncestorId, Long descendantId, Map<Long, CategoryDTO> dtoMap) {
        if (potentialAncestorId.equals(descendantId)) return true;
        
        CategoryDTO descendant = dtoMap.get(descendantId);
        if (descendant == null || descendant.getParentId() == null) return false;
        
        // Theo dõi set để tránh lặp vô tận chính nó trong database
        Set<Long> visited = new HashSet<>();
        Long currentParentId = descendant.getParentId();
        while (currentParentId != null) {
            if (!visited.add(currentParentId)) break; // Phát hiện loop trong DB
            if (currentParentId.equals(potentialAncestorId)) return true;
            
            CategoryDTO parent = dtoMap.get(currentParentId);
            currentParentId = (parent != null) ? parent.getParentId() : null;
        }
        return false;
    }

    @Override
    public void deleteCategory(Long id) {
        Category category = categoryDao.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy danh mục"));

        long productCount = categoryDao.countProductsByCategory(id);
        if (productCount > 0) {
            throw new IllegalStateException("Không thể xóa danh mục đang có " + productCount + " sản phẩm");
        }

        categoryDao.detachChildren(id);
        categoryDao.delete(id);
    }

    @Override
    public List<CategoryDTO> getRootCategories() {
        List<Category> roots = categoryDao.findRootCategories();
        Map<Long, Long> countsMap = categoryDao.getProductCountsGroupedByCategory();
        return roots.stream()
                .map(c -> toDTO(c, countsMap))
                .collect(Collectors.toList());
    }

    private CategoryDTO toDTO(Category entity, Map<Long, Long> countsMap) {
        long count = countsMap.getOrDefault(entity.getId(), 0L);
        return toDTO(entity, count);
    }

    private CategoryDTO toDTO(Category entity, long productCount) {
        return new CategoryDTO.Builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .parentId(entity.getParent() != null ? entity.getParent().getId() : null)
                .parentName(entity.getParent() != null ? entity.getParent().getName() : null)
                .productCount(productCount)
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    private List<CategoryDTO> buildTree(List<CategoryDTO> flatList) {
        Map<Long, CategoryDTO> dtoMap = flatList.stream()
                .collect(Collectors.toMap(CategoryDTO::getId, dto -> dto));

        List<CategoryDTO> roots = flatList.stream()
                .filter(dto -> dto.getParentId() == null)
                .collect(Collectors.toList());

        flatList.stream()
                .filter(dto -> dto.getParentId() != null)
                .forEach(dto -> {
                    CategoryDTO parent = dtoMap.get(dto.getParentId());
                    if (parent != null) {
                        // Ngăn chặn vòng lặp trực tiếp và gián tiếp (A -> B -> A)
                        if (!isAncestor(dto.getId(), parent.getId(), dtoMap)) {
                            parent.getChildren().add(dto);
                        }
                    }
                });

        Set<Long> visited = new HashSet<>();
        assignLevels(roots, 0, visited);
        
        visited.clear();
        sortChildren(roots, visited);
        
        return sortStrategy.sort(roots);
    }

    private void assignLevels(List<CategoryDTO> categories, int level, Set<Long> visited) {
        categories.forEach(dto -> {
            if (visited.add(dto.getId())) {
                dto.setLevel(level);
                if (!dto.getChildren().isEmpty()) {
                    assignLevels(dto.getChildren(), level + 1, visited);
                }
            }
        });
    }

    private void sortChildren(List<CategoryDTO> categories, Set<Long> visited) {
        categories.forEach(dto -> {
            if (visited.add(dto.getId())) {
                if (!dto.getChildren().isEmpty()) {
                    dto.setChildren(sortStrategy.sort(dto.getChildren()));
                    sortChildren(dto.getChildren(), visited);
                }
            }
        });
    }
}
