package vn.edu.ute.service.impl;

import vn.edu.ute.entity.Order;
import vn.edu.ute.service.OrderNotificationService;

/**
 * Implementation Mock của OrderNotificationService.
 * Dùng System.out.println để mô phỏng gửi Email.
 * Về sau thay thân method bằng JavaMail / SendGrid mà không cần
 * chạm vào State layer hay ServiceImpl.
 */
public class OrderNotificationServiceImpl implements OrderNotificationService {

    @Override
    public void notifyProcessing(Order order) {
        System.out.printf("[EMAIL] Gửi tới <%s> - Đơn hàng #%s đã được XÁC NHẬN và đang được xử lý.%n",
                order.getEmail(), order.getOrderCode());
    }

    @Override
    public void notifyShipped(Order order) {
        System.out.printf("[EMAIL] Gửi tới <%s> - Đơn hàng #%s đã được XUẤT KHO và đang trên đường giao.%n",
                order.getEmail(), order.getOrderCode());
    }

    @Override
    public void notifyDelivered(Order order) {
        System.out.printf("[EMAIL] Gửi tới <%s> - Đơn hàng #%s đã GIAO THÀNH CÔNG. Cảm ơn bạn đã mua hàng!%n",
                order.getEmail(), order.getOrderCode());
    }

    @Override
    public void notifyCancelled(Order order, String reason) {
        System.out.printf("[EMAIL] Gửi tới <%s> - Đơn hàng #%s đã bị HUỶ. Lý do: %s%n",
                order.getEmail(), order.getOrderCode(), reason);
    }
}
