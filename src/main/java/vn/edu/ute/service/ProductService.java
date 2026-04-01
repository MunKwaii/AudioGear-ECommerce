package vn.edu.ute.service;

import vn.edu.ute.dto.request.CreateProductRequest;
import vn.edu.ute.entity.Brand;
import vn.edu.ute.entity.Category;
import vn.edu.ute.entity.Product;

import java.util.List;
import java.util.Optional;

public interface ProductService {
    Product createProduct(CreateProductRequest request);
    Product updateProduct(Long id, CreateProductRequest request);
    void deleteProduct(Long id);
    List<Category> getAllCategories();
    List<Brand> getAllBrands();
    Optional<Product> getProductById(Long id);
    List<Product> getRelatedProducts(Long categoryId, Long excludeProductId, int limit);
    List<Product> searchProductsForAdmin(String keyword, Long categoryId, Boolean status, int offset, int limit);
    long countSearchProductsForAdmin(String keyword, Long categoryId, Boolean status);
}
