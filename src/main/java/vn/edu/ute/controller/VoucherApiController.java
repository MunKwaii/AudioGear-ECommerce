package vn.edu.ute.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import vn.edu.ute.dto.request.VoucherApplyRequest;
import vn.edu.ute.dto.response.VoucherApplyResponse;
import vn.edu.ute.dto.response.VoucherValidationResult;
import vn.edu.ute.service.VoucherService;
import vn.edu.ute.service.impl.VoucherServiceImpl;

import java.io.IOException;
import java.math.BigDecimal;

@WebServlet("/api/v1/vouchers/apply")
public class VoucherApiController extends BaseApiController {

    private final VoucherService voucherService = new VoucherServiceImpl();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        processRequest(req, resp, () -> {
            VoucherApplyRequest request = gson.fromJson(req.getReader(), VoucherApplyRequest.class);

            if (request == null || request.getCode() == null || request.getCode().isBlank()) {
                sendError(resp, "Mã giảm giá không hợp lệ", HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            if (request.getSubtotal() == null || request.getSubtotal().compareTo(BigDecimal.ZERO) <= 0) {
                sendError(resp, "Giá trị đơn hàng không hợp lệ", HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            Long userId = (Long) req.getAttribute("currentUserId");
            
            VoucherValidationResult validationResult = voucherService.validateVoucher(
                    request.getCode(), 
                    request.getSubtotal(), 
                    userId
            );

            VoucherApplyResponse response = new VoucherApplyResponse();
            if (validationResult.isValid()) {
                BigDecimal discountAmount = voucherService.calculateDiscount(
                        validationResult.getVoucher(), 
                        request.getSubtotal()
                );
                BigDecimal newTotal = request.getSubtotal().subtract(discountAmount);

                response.setSuccess(true);
                response.setMessage("Áp dụng mã giảm giá thành công");
                response.setDiscountAmount(discountAmount);
                response.setNewTotal(newTotal);
                
                sendSuccess(resp, response.getMessage(), response);
            } else {
                sendError(resp, validationResult.getMessage(), HttpServletResponse.SC_BAD_REQUEST);
            }
        });
    }
}
