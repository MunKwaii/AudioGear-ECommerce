package vn.edu.ute.service.impl;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
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
import vn.edu.ute.entity.ProductImage;
import vn.edu.ute.product.builder.DefaultProductBuilder;
import vn.edu.ute.product.builder.ProductBuilder;
import vn.edu.ute.service.ProductService;
import vn.edu.ute.util.JsonUtil;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.Optional;

public class ProductServiceImpl implements ProductService {
    private final ProductDao productDao = ProductDaoImpl.getInstance();
    private final CategoryDao categoryDao = CategoryDaoImpl.getInstance();
    private final BrandDao brandDao = BrandDaoImpl.getInstance();

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

        String specifications = safeTrim(request.getSpecifications());
        if (!specifications.isEmpty() && !JsonUtil.isValidJsonObject(specifications)) {
            errors.add("Thông số kỹ thuật phải là JSON object hợp lệ");
        }

        List<String> imageUrls = parseImageUrls(request.getImageUrls(), errors);
        String thumbnailUrl = normalizeImageUrl(request.getThumbnailUrl());
        if (thumbnailUrl.isEmpty() && !imageUrls.isEmpty()) {
            thumbnailUrl = imageUrls.get(0);
        }
        if (!thumbnailUrl.isEmpty() && !imageUrls.isEmpty() && !imageUrls.contains(thumbnailUrl)) {
            errors.add("Ảnh đại diện phải nằm trong danh sách ảnh");
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
                .thumbnailUrl(thumbnailUrl)
                .specifications(specifications)
                .status(parseStatus(request.getStatus()))
                .stockQuantity(stockQuantity)
                .category(category)
                .brand(brand);

        Product product = builder.build();
        if (!imageUrls.isEmpty()) {
            Set<ProductImage> images = new LinkedHashSet<>();
            for (String url : imageUrls) {
                ProductImage image = new ProductImage();
                image.setProduct(product);
                image.setImageUrl(url);
                image.setAltText(name);
                image.setIsPrimary(url.equals(thumbnailUrl));
                images.add(image);
            }
            product.setImages(images);
        }
        return productDao.save(product);
    }

    @Override
    public Product updateProduct(Long id, CreateProductRequest request) {
        List<String> errors = new ArrayList<>();
        Product product = productDao.findById(id).orElse(null);
        if (product == null) {
            throw new IllegalArgumentException("Không tìm thấy sản phẩm");
        }

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

        String specifications = safeTrim(request.getSpecifications());
        if (!specifications.isEmpty() && !JsonUtil.isValidJsonObject(specifications)) {
            errors.add("Thông số kỹ thuật phải là JSON object hợp lệ");
        }

        String rawImageUrls = request.getImageUrls();
        boolean hasImagePayload = rawImageUrls != null && !rawImageUrls.trim().isEmpty();
        List<String> imageUrls = parseImageUrls(rawImageUrls, errors);
        String thumbnailUrl = normalizeImageUrl(request.getThumbnailUrl());
        if (thumbnailUrl.isEmpty() && hasImagePayload && !imageUrls.isEmpty()) {
            thumbnailUrl = imageUrls.get(0);
        }
        if (hasImagePayload && !thumbnailUrl.isEmpty() && !imageUrls.isEmpty() && !imageUrls.contains(thumbnailUrl)) {
            errors.add("Ảnh đại diện phải nằm trong danh sách ảnh");
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

        product.setName(name);
        product.setDescription(safeTrim(request.getDescription()));
        product.setPrice(price);
        product.setSpecifications(specifications);
        product.setStatus(parseStatus(request.getStatus()));
        product.setStockQuantity(stockQuantity);
        product.setCategory(category);
        product.setBrand(brand);

        if (hasImagePayload) {
            product.setThumbnailUrl(thumbnailUrl);
            product.getImages().clear();
            if (!imageUrls.isEmpty()) {
                for (String url : imageUrls) {
                    ProductImage image = new ProductImage();
                    image.setProduct(product);
                    image.setImageUrl(url);
                    image.setAltText(name);
                    image.setIsPrimary(url.equals(thumbnailUrl));
                    product.getImages().add(image);
                }
            }
        }

        return productDao.save(product);
    }

    @Override
    public void deleteProduct(Long id) {
        productDao.deleteById(id);
    }

    @Override
    public List<Category> getAllCategories() {
        return categoryDao.findAll();
    }

    @Override
    public List<Brand> getAllBrands() {
        return brandDao.findAll();
    }

    @Override
    public Optional<Product> getProductById(Long id) {
        return productDao.findById(id);
    }

    @Override
    public List<Product> getRelatedProducts(Long categoryId, Long excludeProductId, int limit) {
        return productDao.findRelatedProducts(categoryId, excludeProductId, limit);
    }

    @Override
    public List<Product> searchProductsForAdmin(String keyword, Long categoryId, Boolean status, int offset, int limit) {
        return productDao.searchProductsForAdmin(keyword, categoryId, status, offset, limit);
    }

    @Override
    public long countSearchProductsForAdmin(String keyword, Long categoryId, Boolean status) {
        return productDao.countSearchProductsForAdmin(keyword, categoryId, status);
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

    private static List<String> parseImageUrls(String json, List<String> errors) {
        String trimmed = safeTrim(json);
        if (trimmed.isEmpty()) {
            return new ArrayList<>();
        }
        if (!JsonUtil.isValidJson(trimmed)) {
            errors.add("Danh sách ảnh phải là JSON hợp lệ");
            return new ArrayList<>();
        }
        try {
            Type type = new TypeToken<List<String>>() {}.getType();
            List<String> raw = new Gson().fromJson(trimmed, type);
            if (raw == null) {
                return new ArrayList<>();
            }
            List<String> cleaned = new ArrayList<>();
            Set<String> seen = new LinkedHashSet<>();
            for (String url : raw) {
                String value = normalizeImageUrl(url);
                if (!value.isEmpty() && seen.add(value)) {
                    cleaned.add(value);
                }
            }
            return cleaned;
        } catch (Exception ex) {
            errors.add("Danh sách ảnh không đúng định dạng");
            return new ArrayList<>();
        }
    }

    private static String normalizeImageUrl(String url) {
        String trimmed = safeTrim(url);
        if (trimmed.isEmpty()) {
            return "";
        }
        // Nếu là ảnh local path mà thiếu /static thì thêm vào
        if (trimmed.startsWith("/images/products/") && !trimmed.startsWith("/static/")) {
            return "/static" + trimmed;
        }
        return trimmed;
    }
}
