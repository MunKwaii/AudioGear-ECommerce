package vn.edu.ute.service;

import vn.edu.ute.dto.request.CreateProductRequest;
import vn.edu.ute.entity.Brand;
import vn.edu.ute.entity.Category;
import vn.edu.ute.entity.Product;

import java.util.List;
import java.util.Optional;

public interface ProductService {
    Product createProduct(CreateProductRequest request);
    List<Category> getAllCategories();
    List<Brand> getAllBrands();
    Optional<Product> getProductById(Long id);
    List<Product> getRelatedProducts(Long categoryId, Long excludeProductId, int limit);
}
