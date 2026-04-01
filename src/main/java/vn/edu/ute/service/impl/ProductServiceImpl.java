package vn.edu.ute.service.impl;

import vn.edu.ute.dao.BrandDao;
import vn.edu.ute.dao.CategoryDao;
import vn.edu.ute.dao.ProductDao;
import vn.edu.ute.dao.impl.BrandDaoImpl;
import vn.edu.ute.dao.impl.CategoryDaoImpl;
import vn.edu.ute.dao.impl.ProductDaoImpl;
import vn.edu.ute.dto.request.CreateProductRequest;
import vn.edu.ute.entity.Brand;
import vn.edu.ute.entity.Category;
import vn.edu.ute.entity.Product;
import vn.edu.ute.product.builder.DefaultProductBuilder;
import vn.edu.ute.product.builder.ProductBuilder;
import vn.edu.ute.service.ProductService;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProductServiceImpl implements ProductService {
    private final ProductDao productDao = ProductDaoImpl.getInstance();
    private final CategoryDao categoryDao = CategoryDaoImpl.getInstance();
    private final BrandDao brandDao = new BrandDaoImpl();

    @Override
    public Product createProduct(CreateProductRequest request) {
        List<String> errors = new ArrayList<>();

        String name = safeTrim(request.getName());
        if (name.isEmpty()) {
            errors.add("Tên sản phẩm không được để trống");
        }

        BigDecimal price = parseBigDecimal(request.getPrice(), "Giá sản phẩm", errors);
        if (price != null && price.compareTo(BigDecimal.ZERO) <= 0) {
            errors.add("Giá sản phẩm phải lớn hơn 0");
        }

        Integer stockQuantity = parseInteger(request.getStockQuantity(), "Số lượng tồn kho", errors);
        if (stockQuantity != null && stockQuantity < 0) {
            errors.add("Số lượng tồn kho không được nhỏ hơn 0");
        }

        Long categoryId = parseLong(request.getCategoryId(), "Danh mục", errors);
        Long brandId = parseLong(request.getBrandId(), "Thương hiệu", errors);

        Category category = null;
        if (categoryId != null) {
            category = categoryDao.findById(categoryId).orElse(null);
            if (category == null) {
                errors.add("Danh mục không tồn tại");
            }
        }

        Brand brand = null;
        if (brandId != null) {
            brand = brandDao.findById(brandId).orElse(null);
            if (brand == null) {
                errors.add("Thương hiệu không tồn tại");
            }
        }

        if (!errors.isEmpty()) {
            throw new IllegalArgumentException(String.join("; ", errors));
        }

        ProductBuilder builder = new DefaultProductBuilder()
                .name(name)
                .description(safeTrim(request.getDescription()))
                .price(price)
                .thumbnailUrl(safeTrim(request.getThumbnailUrl()))
                .specifications(safeTrim(request.getSpecifications()))
                .status(parseStatus(request.getStatus()))
                .stockQuantity(stockQuantity)
                .category(category)
                .brand(brand);

        Product product = builder.build();
        return productDao.save(product);
    }

    @Override
    public List<Category> getAllCategories() {
        return categoryDao.findAll();
    }

    @Override
    public List<Brand> getAllBrands() {
        return brandDao.findAll();
    }

    private static String safeTrim(String value) {
        return value == null ? "" : value.trim();
    }

    private static BigDecimal parseBigDecimal(String value, String fieldLabel, List<String> errors) {
        String trimmed = safeTrim(value);
        if (trimmed.isEmpty()) {
            errors.add(fieldLabel + " không được để trống");
            return null;
        }
        try {
            return new BigDecimal(trimmed);
        } catch (NumberFormatException e) {
            errors.add(fieldLabel + " không hợp lệ");
            return null;
        }
    }

    private static Integer parseInteger(String value, String fieldLabel, List<String> errors) {
        String trimmed = safeTrim(value);
        if (trimmed.isEmpty()) {
            errors.add(fieldLabel + " không được để trống");
            return null;
        }
        try {
            return Integer.parseInt(trimmed);
        } catch (NumberFormatException e) {
            errors.add(fieldLabel + " không hợp lệ");
            return null;
        }
    }

    private static Long parseLong(String value, String fieldLabel, List<String> errors) {
        String trimmed = safeTrim(value);
        if (trimmed.isEmpty()) {
            errors.add(fieldLabel + " không được để trống");
            return null;
        }
        try {
            return Long.parseLong(trimmed);
        } catch (NumberFormatException e) {
            errors.add(fieldLabel + " không hợp lệ");
            return null;
        }
    }

    private static Boolean parseStatus(String status) {
        if (status == null) {
            return Boolean.TRUE;
        }
        String normalized = status.trim().toLowerCase();
        if ("false".equals(normalized) || "0".equals(normalized) || "off".equals(normalized)) {
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }
}
