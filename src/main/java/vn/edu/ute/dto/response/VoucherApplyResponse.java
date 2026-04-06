package vn.edu.ute.dto.response;

import java.math.BigDecimal;

public class VoucherApplyResponse {
    private boolean success;
    private String message;
    private BigDecimal discountAmount;
    private BigDecimal newTotal;

    public VoucherApplyResponse() {}

    public VoucherApplyResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
        this.discountAmount = BigDecimal.ZERO;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }

    public BigDecimal getNewTotal() {
        return newTotal;
    }

    public void setNewTotal(BigDecimal newTotal) {
        this.newTotal = newTotal;
    }
}
