package vn.edu.ute.dto.response;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;
import vn.edu.ute.entity.Inventory;
import vn.edu.ute.entity.Product;
import vn.edu.ute.entity.ProductImage;
import vn.edu.ute.homepage.factory.DaoFactory;
import vn.edu.ute.util.ImageUtil;

public class ProductDetailDTO {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private String thumbnailUrl;
    private String specifications;
    private Integer stockQuantity;
    private String categoryName;
    private Long categoryId;
    private String brandName;
    private List<String> images;

    public ProductDetailDTO() {}

    public static ProductDetailDTO fromEntity(Product product) {
        ProductDetailDTO dto = new ProductDetailDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setThumbnailUrl(ImageUtil.resolveImageUrl(product.getThumbnailUrl()));
        dto.setSpecifications(product.getSpecifications());
        dto.setStockQuantity(DaoFactory.getInventoryDao().findByProductId(product.getId()).map(Inventory::getStockQuantity).orElse(0));
        
        if (product.getCategory() != null) {
            dto.setCategoryId(product.getCategory().getId());
            dto.setCategoryName(product.getCategory().getName());
        }
        
        if (product.getBrand() != null) {
            dto.setBrandName(product.getBrand().getName());
        }
        
        // Sử dụng Java Stream để chuyển đổi danh sách ảnh
        if (product.getImages() != null && !product.getImages().isEmpty()) {
            dto.setImages(product.getImages().stream()
                    .map(img -> ImageUtil.resolveImageUrl(img.getImageUrl()))
                    .collect(Collectors.toList()));
        }
        
        return dto;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public String getThumbnailUrl() { return thumbnailUrl; }
    public void setThumbnailUrl(String thumbnailUrl) { this.thumbnailUrl = thumbnailUrl; }
    public String getSpecifications() { return specifications; }
    public void setSpecifications(String specifications) { this.specifications = specifications; }
    public Integer getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(Integer stockQuantity) { this.stockQuantity = stockQuantity; }
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    public String getBrandName() { return brandName; }
    public void setBrandName(String brandName) { this.brandName = brandName; }
    public List<String> getImages() { return images; }
    public void setImages(List<String> images) { this.images = images; }
}
