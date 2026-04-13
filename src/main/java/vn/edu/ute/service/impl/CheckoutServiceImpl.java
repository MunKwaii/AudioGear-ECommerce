package vn.edu.ute.service.impl;

import jakarta.persistence.EntityManager;
import vn.edu.ute.config.DatabaseConfig;
import vn.edu.ute.dao.UserDAO;
import vn.edu.ute.dao.impl.UserDAOImpl;
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
import vn.edu.ute.order.payment.strategy.PaymentResult;
import vn.edu.ute.order.payment.strategy.PaymentStrategy;
import vn.edu.ute.order.payment.strategy.PaymentStrategyFactory;
import vn.edu.ute.service.CheckoutService;
import vn.edu.ute.service.OrderNotificationService;
import vn.edu.ute.service.VoucherService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

public class CheckoutServiceImpl implements CheckoutService {

    private final VoucherService voucherService;
    private final UserDAO userDAO;
    private final OrderNotificationService orderNotificationService;

    public CheckoutServiceImpl() {
        this.voucherService = new VoucherServiceImpl();
        this.userDAO = UserDAOImpl.getInstance();
        this.orderNotificationService = new OrderNotificationServiceImpl();
    }

    @Override
    public CheckoutResponse checkout(Long userId, CheckoutRequest request) {
        validateCheckoutRequest(request);

        if (userId == null) {
            validateGuestContactInfo(request);
        }

        EntityManager em = DatabaseConfig.getInstance().getEntityManager();

        try {
            DatabaseConfig.getInstance().beginTransaction();

            User user = null;
            if (userId != null) {
                user = em.find(User.class, userId);
                if (user == null) {
                    throw new RuntimeException("Không tìm thấy user với id = " + userId);
                }
            }

            List<OrderItem> orderItems = new ArrayList<>();
            List<Long> checkedOutProductIds = new ArrayList<>();
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

                int availableStock = product.getInventory() != null ? product.getInventory().getStockQuantity() : 0;
                if (availableStock < itemRequest.getQuantity()) {
                    throw new RuntimeException(
                            "Sản phẩm '" + product.getName() + "' không đủ tồn kho. Còn lại: "
                                    + availableStock
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
                checkedOutProductIds.add(product.getId());
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

            // 3. Xử lý thanh toán qua PaymentStrategy
            PaymentStrategy strategy = PaymentStrategyFactory.fromCode(request.getPaymentMethod());
            PaymentResult paymentResult = strategy.pay(finalTotal);

            if (!paymentResult.success()) {
                throw new RuntimeException("Thanh toán thất bại: " + paymentResult.message());
            }

            // 4. Tạo Order
            Order order = new Order();
            order.setOrderCode(generateOrderCode());
            order.setUser(user);
            order.setEmail(request.getEmail());
            order.setRecipientName(request.getRecipientName());
            order.setPhoneNumber(request.getPhoneNumber());
            order.setStreetAddress(request.getStreetAddress());
            order.setCity(request.getCity());
            order.setStatus(OrderStatus.PENDING);
            order.setPaymentStrategy(strategy);
            order.setVoucher(appliedVoucher);
            order.setTotalAmount(finalTotal);
            order.setCreatedAt(LocalDateTime.now());
            order.setUpdatedAt(LocalDateTime.now());

            em.persist(order);

            // 5. Gắn OrderItem vào Order + trừ tồn kho
            for (OrderItem orderItem : orderItems) {
                orderItem.setOrder(order);

                Product product = orderItem.getProduct();
                if (product.getInventory() != null) {
                    product.getInventory().setStockQuantity(product.getInventory().getStockQuantity() - orderItem.getQuantity());
                }
                em.persist(orderItem);
                em.merge(product);
            }
            order.setItems(orderItems);

            // 6. Xóa các sản phẩm đã checkout khỏi giỏ hàng của user
            if (userId != null && !checkedOutProductIds.isEmpty()) {
                em.createQuery(
                                "DELETE FROM CartItem ci " +
                                "WHERE ci.cart.user.id = :userId " +
                                "AND ci.product.id IN :productIds")
                        .setParameter("userId", userId)
                        .setParameter("productIds", checkedOutProductIds)
                        .executeUpdate();
            }

            DatabaseConfig.getInstance().commitTransaction();

            // 7. Gửi email thông báo (Sau khi DB commit thành công)
            // Chỉ gửi email ngay nếu không phải là thanh toán QR (VD: COD)
            // Nếu là SEPAY_QR, email sẽ được gửi sau khi nhận được callback thanh toán thành công
            if (!"SEPAY_QR".equalsIgnoreCase(request.getPaymentMethod())) {
                try {
                    orderNotificationService.notifyProcessing(order);
                } catch (Exception e) {
                    // Log lỗi gửi mail nhưng không làm hỏng tiến trình checkout
                    e.printStackTrace();
                }
            }

            return new CheckoutResponse(
                    order.getId(),
                    order.getOrderCode(),
                    originalTotal,
                    discountAmount,
                    finalTotal,
                    appliedVoucher != null ? appliedVoucher.getCode() : null,
                    order.getPaymentStrategy() != null ? order.getPaymentStrategy().getStrategyCode() : null,
                    "Đặt hàng thành công. " + paymentResult.message()
            );

        } catch (VoucherException e) {
            DatabaseConfig.getInstance().rollbackTransaction();
            throw e;
        } catch (Exception e) {
            DatabaseConfig.getInstance().rollbackTransaction();
            throw new RuntimeException("Checkout thất bại: " + e.getMessage(), e);
        } finally {
            DatabaseConfig.getInstance().closeEntityManager();
        }
    }

    /**
     * Validate guest contact info - email and phone must not belong to any registered user.
     * Uses lambda + stream for clean validation.
     */
    private void validateGuestContactInfo(CheckoutRequest request) {
        Map<String, java.util.function.Function<String, Optional<User>>> contactChecks = new LinkedHashMap<>();
        contactChecks.put("Email", userDAO::findByEmail);
        contactChecks.put("Số điện thoại", userDAO::findByPhoneNumber);

        Map<String, String> contactValues = new LinkedHashMap<>();
        contactValues.put("Email", request.getEmail());
        contactValues.put("Số điện thoại", request.getPhoneNumber());

        contactChecks.entrySet().stream()
                .filter(entry -> {
                    String value = contactValues.get(entry.getKey());
                    return value != null && !value.isBlank() && entry.getValue().apply(value).isPresent();
                })
                .findFirst()
                .ifPresent(entry -> {
                    throw new RuntimeException(entry.getKey() + " '" + contactValues.get(entry.getKey())
                            + "' đã được sử dụng bởi một tài khoản đăng ký. Vui lòng đăng nhập để đặt hàng.");
                });
    }

    /**
     * Kiểm tra dữ liệu cơ bản trước khi checkout.
     * Sử dụng Map + Stream thay vì nhiều if-block.
     */
    private void validateCheckoutRequest(CheckoutRequest request) {
        if (request == null) {
            throw new RuntimeException("Checkout request không được null");
        }

        // Map<String, Supplier<String>>: fieldName → getter
        // Stream.filter() tìm field đầu tiên rỗng → ném exception
        Map<String, Supplier<String>> requiredFields = new LinkedHashMap<>();
        requiredFields.put("Email", request::getEmail);
        requiredFields.put("Tên người nhận", request::getRecipientName);
        requiredFields.put("Số điện thoại", request::getPhoneNumber);
        requiredFields.put("Địa chỉ", request::getStreetAddress);
        requiredFields.put("Thành phố", request::getCity);
        requiredFields.put("Phương thức thanh toán", request::getPaymentMethod);

        requiredFields.entrySet().stream()
                .filter(entry -> {
                    String value = entry.getValue().get();
                    return value == null || value.isBlank();
                })
                .findFirst()
                .ifPresent(entry -> {
                    throw new RuntimeException(entry.getKey() + " không được để trống");
                });

        // Kiểm tra danh sách sản phẩm
        Optional.ofNullable(request.getItems())
                .filter(items -> !items.isEmpty())
                .orElseThrow(() -> new RuntimeException("Danh sách sản phẩm không được để trống"));
    }

    /**
     * Sinh mã đơn hàng đơn giản.
     * Có thể thay bằng format đẹp hơn sau.
     */
    private String generateOrderCode() {
        return "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}