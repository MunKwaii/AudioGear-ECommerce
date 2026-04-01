package vn.edu.ute.dto;

import java.math.BigDecimal;

public class CartItemDTO {
    private Long id; // ID của dòng CartItem
    private Long productId;
    private String productName;
    private String productThumbnail;
    private BigDecimal price;
    private int quantity;
    private int stockQuantity; // Tồn kho thực tế
    private BigDecimal totalPrice; // price * quantity

    public CartItemDTO() {}

    public CartItemDTO(Long id, Long productId, String productName, String productThumbnail, BigDecimal price, int quantity, BigDecimal totalPrice, int stockQuantity) {
        this.id = id;
        this.productId = productId;
        this.productName = productName;
        this.productThumbnail = productThumbnail;
        this.price = price;
        this.quantity = quantity;
        this.totalPrice = totalPrice;
        this.stockQuantity = stockQuantity;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductThumbnail() {
        return productThumbnail;
    }

    public void setProductThumbnail(String productThumbnail) {
        this.productThumbnail = productThumbnail;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public int getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(int stockQuantity) {
        this.stockQuantity = stockQuantity;
    }
}
