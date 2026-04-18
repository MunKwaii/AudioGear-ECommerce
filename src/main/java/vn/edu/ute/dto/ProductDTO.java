package vn.edu.ute.dto;

import java.math.BigDecimal;
import vn.edu.ute.util.ImageUtil;

public class ProductDTO {
    private Long id;
    private String name;
    private BigDecimal price;
    private String thumbnailUrl;
    private String categoryName;
    private String brandName;
    private Integer stockQuantity;
    private Boolean status;

    public ProductDTO() {}

    public ProductDTO(Long id, String name, BigDecimal price, String thumbnailUrl, String categoryName, String brandName, Integer stockQuantity, Boolean status) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.thumbnailUrl = ImageUtil.resolveImageUrl(thumbnailUrl);
        this.categoryName = categoryName;
        this.brandName = brandName;
        this.stockQuantity = stockQuantity;
        this.status = status;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    
    public String getThumbnailUrl() { return thumbnailUrl; }
    public void setThumbnailUrl(String thumbnailUrl) { this.thumbnailUrl = ImageUtil.resolveImageUrl(thumbnailUrl); }
    
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    
    public String getBrandName() { return brandName; }
    public void setBrandName(String brandName) { this.brandName = brandName; }
    
    public Integer getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(Integer stockQuantity) { this.stockQuantity = stockQuantity; }
    
    public Boolean getStatus() { return status; }
    public void setStatus(Boolean status) { this.status = status; }
}
