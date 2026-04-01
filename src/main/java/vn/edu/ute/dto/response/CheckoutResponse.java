package vn.edu.ute.dto.response;

import java.math.BigDecimal;

public class CheckoutResponse {

    private Long orderId;
    private String orderCode;
    private BigDecimal originalTotal;
    private BigDecimal discountAmount;
    private BigDecimal finalTotal;
    private String voucherCode;
    private String paymentMethod;
    private String message;

    public CheckoutResponse() {
    }

    public CheckoutResponse(Long orderId,
                            String orderCode,
                            BigDecimal originalTotal,
                            BigDecimal discountAmount,
                            BigDecimal finalTotal,
                            String voucherCode,
                            String paymentMethod,
                            String message) {
        this.orderId = orderId;
        this.orderCode = orderCode;
        this.originalTotal = originalTotal;
        this.discountAmount = discountAmount;
        this.finalTotal = finalTotal;
        this.voucherCode = voucherCode;
        this.paymentMethod = paymentMethod;
        this.message = message;
    }

    public Long getOrderId() {
        return orderId;
    }

    public String getOrderCode() {
        return orderCode;
    }

    public BigDecimal getOriginalTotal() {
        return originalTotal;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public BigDecimal getFinalTotal() {
        return finalTotal;
    }

    public String getVoucherCode() {
        return voucherCode;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public String getMessage() {
        return message;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public void setOrderCode(String orderCode) {
        this.orderCode = orderCode;
    }

    public void setOriginalTotal(BigDecimal originalTotal) {
        this.originalTotal = originalTotal;
    }

    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }

    public void setFinalTotal(BigDecimal finalTotal) {
        this.finalTotal = finalTotal;
    }

    public void setVoucherCode(String voucherCode) {
        this.voucherCode = voucherCode;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}