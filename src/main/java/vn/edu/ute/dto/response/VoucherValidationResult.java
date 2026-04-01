package vn.edu.ute.dto.response;

import vn.edu.ute.entity.Voucher;

public class VoucherValidationResult {

    private final boolean valid;
    private final String message;
    private final Voucher voucher;

    private VoucherValidationResult(boolean valid, String message, Voucher voucher) {
        this.valid = valid;
        this.message = message;
        this.voucher = voucher;
    }

    public static VoucherValidationResult valid(Voucher voucher) {
        return new VoucherValidationResult(true, "Voucher hợp lệ", voucher);
    }

    public static VoucherValidationResult invalid(String message) {
        return new VoucherValidationResult(false, message, null);
    }

    public boolean isValid() {
        return valid;
    }

    public String getMessage() {
        return message;
    }

    public Voucher getVoucher() {
        return voucher;
    }
}