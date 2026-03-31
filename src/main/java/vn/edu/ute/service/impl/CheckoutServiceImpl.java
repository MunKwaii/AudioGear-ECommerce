package vn.edu.ute.service.impl;

import jakarta.persistence.EntityManager;
import vn.edu.ute.config.DatabaseConfig;
import vn.edu.ute.dto.request.CheckoutItemRequest;
import vn.edu.ute.dto.request.CheckoutRequest;
import vn.edu.ute.dto.response.CheckoutResponse;
import vn.edu.ute.dto.response.VoucherValidationResult;
import vn.edu.ute.entity.Order;
import vn.edu.ute.entity.OrderItem;
import vn.edu.ute.entity.Product;
import vn.edu.ute.entity.User;
import vn.edu.ute.entity.Voucher;
import vn.edu.ute.entity.enums.OrderStatus;
import vn.edu.ute.exception.VoucherException;
import vn.edu.ute.order.payment.strategy.BankTransferStrategy;
import vn.edu.ute.order.payment.strategy.CODStrategy;
import vn.edu.ute.order.payment.strategy.MomoStrategy;
import vn.edu.ute.order.payment.strategy.PaymentStrategy;
import vn.edu.ute.order.payment.strategy.StorePickupStrategy;
import vn.edu.ute.service.CheckoutService;
import vn.edu.ute.service.VoucherService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CheckoutServiceImpl implements CheckoutService {

    private final VoucherService voucherService;

    public CheckoutServiceImpl() {
        this.voucherService = new VoucherServiceImpl();
    }

    @Override
    public CheckoutResponse checkout(Long userId, CheckoutRequest request) {
        validateCheckoutRequest(request);

        EntityManager em = DatabaseConfig.getEntityManager();

        try {
            DatabaseConfig.beginTransaction();

            User user = null;
            if (userId != null) {
                user = em.find(User.class, userId);
                if (user == null) {
                    throw new RuntimeException("Không tìm thấy user với id = " + userId);
                }
            }

            List<OrderItem> orderItems = new ArrayList<>();
            BigDecimal originalTotal = BigDecimal.ZERO;

            // 1. Load Product + validate stock + tạo OrderItem tạm
            for (CheckoutItemRequest itemRequest : request.getItems()) {
                Product product = em.find(Product.class, itemRequest.getProductId());

                if (product == null) {
                    throw new RuntimeException("Không tìm thấy sản phẩm với id = " + itemRequest.getProductId());
                }

                if (Boolean.FALSE.equals(product.getStatus())) {
                    throw new RuntimeException("Sản phẩm '" + product.getName() + "' hiện không khả dụng");
                }

                if (itemRequest.getQuantity() == null || itemRequest.getQuantity() <= 0) {
                    throw new RuntimeException("Số lượng sản phẩm không hợp lệ");
                }

                if (product.getStockQuantity() < itemRequest.getQuantity()) {
                    throw new RuntimeException(
                            "Sản phẩm '" + product.getName() + "' không đủ tồn kho. Còn lại: "
                                    + product.getStockQuantity()
                    );
                }

                BigDecimal itemTotal = product.getPrice()
                        .multiply(BigDecimal.valueOf(itemRequest.getQuantity()));

                originalTotal = originalTotal.add(itemTotal);

                OrderItem orderItem = new OrderItem();
                orderItem.setProduct(product);
                orderItem.setQuantity(itemRequest.getQuantity());
                orderItem.setPrice(product.getPrice());

                orderItems.add(orderItem);
            }

            // 2. Validate voucher + calculate discount
            Voucher appliedVoucher = null;
            BigDecimal discountAmount = BigDecimal.ZERO;

            if (request.getVoucherCode() != null && !request.getVoucherCode().isBlank()) {
                VoucherValidationResult validationResult = voucherService.validateVoucher(
                        request.getVoucherCode(),
                        originalTotal,
                        userId
                );

                if (!validationResult.isValid()) {
                    throw new VoucherException(validationResult.getMessage());
                }

                appliedVoucher = em.find(Voucher.class, validationResult.getVoucher().getId());
                discountAmount = voucherService.calculateDiscount(appliedVoucher, originalTotal);
            }

            BigDecimal finalTotal = originalTotal.subtract(discountAmount);

            // 3. Tạo Order
            Order order = new Order();
            order.setOrderCode(generateOrderCode());
            order.setUser(user);
            order.setEmail(request.getEmail());
            order.setRecipientName(request.getRecipientName());
            order.setPhoneNumber(request.getPhoneNumber());
            order.setStreetAddress(request.getStreetAddress());
            order.setCity(request.getCity());
            order.setStatus(OrderStatus.PENDING);
            order.setPaymentStrategy(resolvePaymentStrategy(request.getPaymentMethod()));
            order.setVoucher(appliedVoucher);
            order.setTotalAmount(finalTotal);
            order.setCreatedAt(LocalDateTime.now());
            order.setUpdatedAt(LocalDateTime.now());

            em.persist(order);

            // 4. Gắn OrderItem vào Order + trừ tồn kho
            for (OrderItem orderItem : orderItems) {
                orderItem.setOrder(order);

                Product product = orderItem.getProduct();
                product.setStockQuantity(product.getStockQuantity() - orderItem.getQuantity());

                em.persist(orderItem);
                em.merge(product);
            }

            DatabaseConfig.commitTransaction();

            return new CheckoutResponse(
                    order.getId(),
                    order.getOrderCode(),
                    originalTotal,
                    discountAmount,
                    finalTotal,
                    appliedVoucher != null ? appliedVoucher.getCode() : null,
                    order.getPaymentStrategy() != null ? order.getPaymentStrategy().getStrategyCode() : null,
                    "Đặt hàng thành công"
            );

        } catch (VoucherException e) {
            DatabaseConfig.rollbackTransaction();
            throw e;
        } catch (Exception e) {
            DatabaseConfig.rollbackTransaction();
            throw new RuntimeException("Checkout thất bại: " + e.getMessage(), e);
        } finally {
            DatabaseConfig.closeEntityManager();
        }
    }

    /**
     * Kiểm tra dữ liệu cơ bản trước khi checkout.
     */
    private void validateCheckoutRequest(CheckoutRequest request) {
        if (request == null) {
            throw new RuntimeException("Checkout request không được null");
        }

        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new RuntimeException("Email không được để trống");
        }

        if (request.getRecipientName() == null || request.getRecipientName().isBlank()) {
            throw new RuntimeException("Tên người nhận không được để trống");
        }

        if (request.getPhoneNumber() == null || request.getPhoneNumber().isBlank()) {
            throw new RuntimeException("Số điện thoại không được để trống");
        }

        if (request.getStreetAddress() == null || request.getStreetAddress().isBlank()) {
            throw new RuntimeException("Địa chỉ không được để trống");
        }

        if (request.getCity() == null || request.getCity().isBlank()) {
            throw new RuntimeException("Thành phố không được để trống");
        }

        if (request.getPaymentMethod() == null || request.getPaymentMethod().isBlank()) {
            throw new RuntimeException("Phương thức thanh toán không được để trống");
        }

        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new RuntimeException("Danh sách sản phẩm không được để trống");
        }
    }

    /**
     * Mapping paymentMethod string từ request sang PaymentStrategy hiện có trong project.
     */
    private PaymentStrategy resolvePaymentStrategy(String paymentMethod) {
        if (paymentMethod == null || paymentMethod.isBlank()) {
            return new CODStrategy();
        }

        return switch (paymentMethod.trim().toUpperCase()) {
            case "COD" -> new CODStrategy();
            case "MOMO" -> new MomoStrategy();
            case "BANK", "BANK_TRANSFER" -> new BankTransferStrategy();
            case "STORE_PICKUP" -> new StorePickupStrategy();
            default -> throw new RuntimeException("Phương thức thanh toán không hợp lệ: " + paymentMethod);
        };
    }

    /**
     * Sinh mã đơn hàng đơn giản.
     * Có thể thay bằng format đẹp hơn sau.
     */
    private String generateOrderCode() {
        return "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}