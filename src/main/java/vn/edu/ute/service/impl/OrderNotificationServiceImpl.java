package vn.edu.ute.service.impl;

import org.thymeleaf.context.Context;
import vn.edu.ute.config.ThymeleafConfig;
import vn.edu.ute.entity.Order;
import vn.edu.ute.service.EmailService;
import vn.edu.ute.service.OrderNotificationService;

/**
 * Implementation thực tế của OrderNotificationService.
 * Sử dụng Thymeleaf để render HTML và EmailService để gửi.
 */
public class OrderNotificationServiceImpl implements OrderNotificationService {

    private final EmailService emailService;

    public OrderNotificationServiceImpl() {
        this.emailService = new EmailServiceImpl();
    }

    @Override
    public void notifyProcessing(Order order) {
        try {
            Context context = new Context();
            context.setVariable("order", order);
            context.setVariable("discountAmount", java.math.BigDecimal.ZERO); // Mặc định 0 nếu không có voucher
            context.setVariable("orderUrl", "https://audiogear.vn/account/orders/" + order.getOrderCode());

            String htmlContent = ThymeleafConfig.getTemplateEngine().process("mail/order-confirmation", context);
            
            String subject = "Xác nhận đơn hàng #" + order.getOrderCode() + " - AudioGear";
            emailService.sendHtmlEmail(order.getEmail(), subject, htmlContent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void notifyShipped(Order order) {
        // Tương tự cho các trạng thái khác nếu muốn bổ sung sau
        System.out.printf("[MOCK EMAIL] Đơn hàng #%s đã được giao đi.%n", order.getOrderCode());
    }

    @Override
    public void notifyDelivered(Order order) {
        System.out.printf("[MOCK EMAIL] Đơn hàng #%s đã giao thành công.%n", order.getOrderCode());
    }

    @Override
    public void notifyCancelled(Order order, String reason) {
        System.out.printf("[MOCK EMAIL] Đơn hàng #%s đã bị huỷ.%n", order.getOrderCode());
    }
}

