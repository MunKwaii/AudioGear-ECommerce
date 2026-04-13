package vn.edu.ute.service;

import vn.edu.ute.dto.request.CheckoutRequest;
import vn.edu.ute.dto.response.CheckoutResponse;

public interface CheckoutService {

    /**
     * Thực hiện checkout:
     * - Validate dữ liệu đầu vào
     * - Tính tổng tiền
     * - Áp voucher nếu có
     * - Tạo Order và OrderItem
     * - Trừ tồn kho sản phẩm
     * - Lưu dữ liệu trong cùng một transaction
     */
    CheckoutResponse checkout(Long userId, CheckoutRequest request);
    
}