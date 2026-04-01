package vn.edu.ute.exception;

public class InsufficientStockException extends RuntimeException {
    private final String productName;
    private final int availableStock;

    public InsufficientStockException(String productName, int availableStock) {
        super(String.format("Sản phẩm '%s' chỉ còn %d sản phẩm trong kho.", productName, availableStock));
        this.productName = productName;
        this.availableStock = availableStock;
    }

    public String getProductName() {
        return productName;
    }

    public int getAvailableStock() {
        return availableStock;
    }
}
