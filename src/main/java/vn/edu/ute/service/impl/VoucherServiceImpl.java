package vn.edu.ute.service.impl;

import vn.edu.ute.dao.VoucherDao;
import vn.edu.ute.dao.impl.VoucherDaoImpl;
import vn.edu.ute.dto.response.VoucherValidationResult;
import vn.edu.ute.entity.Voucher;
import vn.edu.ute.entity.enums.VoucherStatus;
import vn.edu.ute.exception.VoucherException;
import vn.edu.ute.order.voucher.strategy.VoucherDiscountStrategy;
import vn.edu.ute.order.voucher.strategy.VoucherDiscountStrategyFactory;
import vn.edu.ute.service.VoucherService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

public class VoucherServiceImpl implements VoucherService {

    private final VoucherDao voucherDao;
    private final VoucherDiscountStrategyFactory strategyFactory;

    public VoucherServiceImpl() {
        this.voucherDao = new VoucherDaoImpl();
        this.strategyFactory = new VoucherDiscountStrategyFactory();
    }

    @Override
    public VoucherValidationResult validateVoucher(String voucherCode, BigDecimal orderTotal, Long userId) {
        if (voucherCode == null || voucherCode.isBlank()) {
            return VoucherValidationResult.invalid("Mã voucher không được để trống");
        }

        if (orderTotal == null || orderTotal.compareTo(BigDecimal.ZERO) <= 0) {
            return VoucherValidationResult.invalid("Tổng tiền đơn hàng không hợp lệ");
        }

        Optional<Voucher> optionalVoucher = voucherDao.findByCode(voucherCode.trim());
        if (optionalVoucher.isEmpty()) {
            return VoucherValidationResult.invalid("Voucher không tồn tại");
        }

        Voucher voucher = optionalVoucher.get();

        if (voucher.getStatus() != VoucherStatus.ACTIVE) {
            return VoucherValidationResult.invalid("Voucher không còn hoạt động");
        }

        if (voucher.getExpiryDate() != null && voucher.getExpiryDate().isBefore(LocalDateTime.now())) {
            return VoucherValidationResult.invalid("Voucher đã hết hạn");
        }

        if (voucher.getMinOrderValue() != null
                && orderTotal.compareTo(voucher.getMinOrderValue()) < 0) {
            return VoucherValidationResult.invalid(
                    "Đơn hàng chưa đạt giá trị tối thiểu để áp voucher"
            );
        }

        if (voucher.getMaxUsage() != null) {
            long usedCount = voucherDao.countOrdersUsingVoucher(voucher.getId());
            if (usedCount >= voucher.getMaxUsage()) {
                return VoucherValidationResult.invalid("Voucher đã hết lượt sử dụng");
            }
        }

        return VoucherValidationResult.valid(voucher);
    }

    @Override
    public BigDecimal calculateDiscount(Voucher voucher, BigDecimal orderTotal) {
        if (voucher == null) {
            throw new VoucherException("Voucher không hợp lệ");
        }

        VoucherDiscountStrategy strategy = strategyFactory.getStrategy(voucher.getDiscountType());
        return strategy.calculateDiscount(voucher, orderTotal);
    }

    @Override
    public BigDecimal applyVoucher(String voucherCode, BigDecimal orderTotal, Long userId) {
        VoucherValidationResult validationResult = validateVoucher(voucherCode, orderTotal, userId);

        if (!validationResult.isValid()) {
            throw new VoucherException(validationResult.getMessage());
        }

        Voucher voucher = validationResult.getVoucher();
        BigDecimal discount = calculateDiscount(voucher, orderTotal);

        return orderTotal.subtract(discount);
    }
}