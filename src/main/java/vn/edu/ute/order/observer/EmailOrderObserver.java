package vn.edu.ute.order.observer;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import vn.edu.ute.entity.Order;

import java.util.Properties;

/**
 * Concrete Observer (Observer Pattern).
 *
 * EmailOrderObserver lắng nghe sự kiện từ OrderService (Subject).
 * Khi nhận được thông báo, nó tự động gửi email HTML cho khách hàng
 * mà không làm gián đoạn luồng cập nhật trạng thái chính.
 */
public class EmailOrderObserver implements OrderObserver {

    private static final Logger logger = LogManager.getLogger(EmailOrderObserver.class);

    // ── SMTP Config (tái sử dụng từ EmailOtpSender) ──────────────────
    private static final String SMTP_HOST     = "smtp.gmail.com";
    private static final String SMTP_PORT     = "587";
    private static final String SENDER_EMAIL  = "trihieuvo4@gmail.com";
    private static final String SENDER_PASSWORD = "exeh vgvi mpwy wwoo";

    // ── Observer Entry Point ─────────────────────────────────────────
    @Override
    public void onOrderEvent(Order order, OrderEventType event, String reason) {
        String toEmail = order.getEmail();
        if (toEmail == null || toEmail.isBlank()) {
            logger.warn("[EmailObserver] Đơn hàng {} không có email, bỏ qua gửi mail.", order.getOrderCode());
            return;
        }

        // Chạy gửi email trong một Thread riêng để không block luồng chính
        Thread emailThread = new Thread(() -> {
            switch (event) {
                case ORDER_PROCESSING -> sendMail(toEmail,
                        "⚙️ Đơn hàng " + order.getOrderCode() + " đang được xử lý",
                        buildProcessingTemplate(order));

                case ORDER_SHIPPED -> sendMail(toEmail,
                        "🚚 Đơn hàng " + order.getOrderCode() + " đang trên đường giao",
                        buildShippedTemplate(order));

                case ORDER_DELIVERED -> sendMail(toEmail,
                        "✅ Đơn hàng " + order.getOrderCode() + " đã giao thành công!",
                        buildDeliveredTemplate(order));

                case ORDER_CANCELLED -> sendMail(toEmail,
                        "❌ Đơn hàng " + order.getOrderCode() + " đã bị huỷ",
                        buildCancelledTemplate(order, reason));
            }
        });
        emailThread.setDaemon(true);
        emailThread.start();
    }

    // ── Core Send Method ─────────────────────────────────────────────
    private void sendMail(String toEmail, String subject, String html) {
        Properties props = new Properties();
        props.put("mail.smtp.auth",             "true");
        props.put("mail.smtp.starttls.enable",  "true");
        props.put("mail.smtp.host",             SMTP_HOST);
        props.put("mail.smtp.port",             SMTP_PORT);

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(SENDER_EMAIL, SENDER_PASSWORD);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(SENDER_EMAIL, "AudioGear Shop"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject(subject);
            message.setContent(html, "text/html; charset=utf-8");
            Transport.send(message);
            logger.info("[EmailObserver] Gửi mail thành công → {}: {}", toEmail, subject);
        } catch (Exception e) {
            logger.error("[EmailObserver] Lỗi gửi mail → {}: {}", toEmail, e.getMessage());
        }
    }

    // ── HTML Email Templates ─────────────────────────────────────────
    private String buildProcessingTemplate(Order order) {
        return wrap(
            "<h2 style='color:#3b82f6'>⚙️ Đơn hàng đang được xử lý</h2>" +
            "<p>Xin chào <strong>" + order.getRecipientName() + "</strong>,</p>" +
            "<p>Đơn hàng <strong>" + order.getOrderCode() + "</strong> của bạn đã được AdminOrder <strong>duyệt</strong> và đang trong giai đoạn chuẩn bị hàng.</p>" +
            orderInfoBlock(order) +
            "<p style='color:#555'>Chúng tôi sẽ thông báo ngay khi hàng được xuất kho. Cảm ơn bạn đã mua sắm tại AudioGear!</p>"
        );
    }

    private String buildShippedTemplate(Order order) {
        return wrap(
            "<h2 style='color:#f97316'>🚚 Đơn hàng đang trên đường giao</h2>" +
            "<p>Xin chào <strong>" + order.getRecipientName() + "</strong>,</p>" +
            "<p>Tuyệt vời! Đơn hàng <strong>" + order.getOrderCode() + "</strong> đã được xuất kho và hiện đang trên đường đến tay bạn.</p>" +
            orderInfoBlock(order) +
            "<p style='color:#555'>Vui lòng để ý điện thoại để nhận hàng từ nhân viên giao hàng. Cảm ơn bạn!</p>"
        );
    }

    private String buildDeliveredTemplate(Order order) {
        return wrap(
            "<h2 style='color:#22c55e'>✅ Giao hàng thành công!</h2>" +
            "<p>Xin chào <strong>" + order.getRecipientName() + "</strong>,</p>" +
            "<p>Đơn hàng <strong>" + order.getOrderCode() + "</strong> đã đến tay bạn thành công. Chúc bạn có trải nghiệm tuyệt vời với sản phẩm AudioGear!</p>" +
            orderInfoBlock(order) +
            "<p style='color:#555'>Nếu có bất kỳ vấn đề gì, vui lòng liên hệ đội hỗ trợ của chúng tôi. Cảm ơn bạn đã tin tưởng AudioGear!</p>"
        );
    }

    private String buildCancelledTemplate(Order order, String reason) {
        String displayReason = (reason != null && !reason.isBlank()) ? reason : "Không có lý do cụ thể";
        return wrap(
            "<h2 style='color:#ef4444'>❌ Đơn hàng đã bị huỷ</h2>" +
            "<p>Xin chào <strong>" + order.getRecipientName() + "</strong>,</p>" +
            "<p>Rất tiếc, đơn hàng <strong>" + order.getOrderCode() + "</strong> của bạn đã bị huỷ.</p>" +
            "<div style='background:#fee2e2;border-left:4px solid #ef4444;padding:12px 16px;margin:16px 0;border-radius:4px'>" +
                "<strong>Lý do huỷ:</strong> " + displayReason +
            "</div>" +
            orderInfoBlock(order) +
            "<p style='color:#555'>Nếu bạn không yêu cầu huỷ đơn hoặc có thắc mắc, vui lòng liên hệ đội hỗ trợ của chúng tôi ngay. Xin lỗi vì sự bất tiện này.</p>"
        );
    }

    /** Khối hiển thị thông tin đơn hàng dùng chung cho các template */
    private String orderInfoBlock(Order order) {
        String amount = String.format("%,.0f VND", order.getTotalAmount().doubleValue());
        return "<div style='background:#f8fafc;border:1px solid #e2e8f0;padding:16px;margin:16px 0;border-radius:8px'>" +
                   "<table style='width:100%;border-collapse:collapse'>" +
                       "<tr><td style='padding:4px 0;color:#64748b'>Mã đơn hàng:</td><td style='font-weight:600'>" + order.getOrderCode() + "</td></tr>" +
                       "<tr><td style='padding:4px 0;color:#64748b'>Người nhận:</td><td>" + order.getRecipientName() + "</td></tr>" +
                       "<tr><td style='padding:4px 0;color:#64748b'>Địa chỉ:</td><td>" + order.getStreetAddress() + ", " + order.getCity() + "</td></tr>" +
                       "<tr><td style='padding:4px 0;color:#64748b'>Tổng tiền:</td><td style='font-weight:700;color:#6c63ff'>" + amount + "</td></tr>" +
                   "</table>" +
               "</div>";
    }

    /** Bao toàn bộ nội dung trong layout email chuẩn */
    private String wrap(String content) {
        return "<div style='font-family:Inter,Arial,sans-serif;background:#f1f5f9;padding:32px'>" +
                   "<div style='max-width:600px;margin:0 auto;background:#fff;border-radius:12px;overflow:hidden;box-shadow:0 4px 24px rgba(0,0,0,.08)'>" +
                       "<div style='background:linear-gradient(135deg,#6c63ff,#a78bfa);padding:24px 32px'>" +
                           "<h1 style='color:#fff;margin:0;font-size:1.4rem;letter-spacing:-.5px'>⚡ AudioGear</h1>" +
                           "<p style='color:rgba(255,255,255,.8);margin:4px 0 0;font-size:.875rem'>Thông báo đơn hàng</p>" +
                       "</div>" +
                       "<div style='padding:32px'>" + content + "</div>" +
                       "<div style='background:#f8fafc;padding:16px 32px;text-align:center;color:#94a3b8;font-size:.75rem'>" +
                           "&copy; 2026 AudioGear E-commerce. Tất cả quyền được bảo lưu." +
                       "</div>" +
                   "</div>" +
               "</div>";
    }
}
