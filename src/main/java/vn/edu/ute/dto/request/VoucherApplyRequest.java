package vn.edu.ute.dto.request;

import java.math.BigDecimal;

public class VoucherApplyRequest {
    private String code;
    private BigDecimal subtotal;

    public VoucherApplyRequest() {}

    public VoucherApplyRequest(String code, BigDecimal subtotal) {
        this.code = code;
        this.subtotal = subtotal;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }
}
