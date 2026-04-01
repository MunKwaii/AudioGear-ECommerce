package vn.edu.ute.dao;

import vn.edu.ute.entity.Product;

import java.util.List;
import java.util.Optional;

public interface ProductDao {
    Product save(Product product);
    Optional<Product> findById(Long id);
    
    /**
     * Lấy danh sách sản phẩm liên quan (cùng danh mục, loại trừ sản phẩm hiện tại)
     * @param categoryId ID danh mục
     * @param excludeProductId ID sản phẩm loại trừ
     * @param limit số lượng tối đa
     * @return List<Product>
     */
    List<Product> findRelatedProducts(Long categoryId, Long excludeProductId, int limit);
    
     /**
     * Lấy danh sách sản phẩm nổi bật (Ví dụ: sắp xếp theo id hoặc random)
     * @param limit số lượng tối đa
     * @return List<Product>
     */
    List<Product> getFeaturedProducts(int limit);

    /**
     * Lấy danh sách sản phẩm mới nhất
     * @param limit số lượng tối đa
     * @return List<Product>
     */
    List<Product> getNewestProducts(int limit);
    /**
     * Truy vấn danh sách sản phẩm theo từ khóa (LIKE %keyword%) và/hoặc danh mục, có phân trang.
     */
    List<Product> searchProducts(String keyword, Long categoryId, int offset, int limit);

    /**
     * Lấy tổng số lượng sản phẩm từ query search đề dùng cho thuật toán đếm trang `(total / limit)`.
     */
    long countSearchProducts(String keyword, Long categoryId);
}
