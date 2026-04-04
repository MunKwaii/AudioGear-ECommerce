package vn.edu.ute.auth.otp;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Concrete Strategy for sending OTP via Email (JavaMail)
 */
public class EmailOtpSender implements OtpSenderStrategy {

    private static final Logger logger = LogManager.getLogger(EmailOtpSender.class);

    @Override
    public boolean sendOtp(String targetEmail, String otp) {
        // Sử dụng MailConfig tập trung
        Session session = vn.edu.ute.config.MailConfig.getSession();

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(vn.edu.ute.config.MailConfig.SENDER_EMAIL, vn.edu.ute.config.MailConfig.SYSTEM_NAME));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(targetEmail));
            message.setSubject("Mã xác minh OTP của bạn");

            // Xây dựng template HTML
            String htmlContent = buildEmailTemplate(otp);

            message.setContent(htmlContent, "text/html; charset=utf-8");

            Transport.send(message);
            logger.info("Email OTP đã được gửi thành công đến {}", targetEmail);
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Lỗi khi gửi thư OTP đến {}: {}", targetEmail, e.getMessage());
            return false;
        }
    }

    private String buildEmailTemplate(String otp) {
        return "<div style='font-family: Arial, sans-serif; padding: 20px; background-color: #f4f7f6;'>" +
                "<div style='max-width: 600px; margin: 0 auto; background: white; padding: 30px; border-radius: 8px; box-shadow: 0 4px 6px rgba(0,0,0,0.1);'>"
                +
                "<h2 style='color: #333; text-align: center;'>Mã xác minh OTP</h2>" +
                "<p style='color: #555; font-size: 16px; line-height: 1.5;'>Bạn vừa yêu cầu mã OTP từ hệ thống AudioGear E-commerce. Dưới đây là mã của bạn:</p>"
                +
                "<div style='background: white; border: 2px dashed #667eea; padding: 20px; margin: 20px 0; text-align: center; border-radius: 8px;'>"
                +
                "<span style='font-size: 32px; font-weight: bold; color: #667eea; letter-spacing: 5px;'>" + otp
                + "</span>" +
                "</div>" +
                "<p style='color: #777; font-size: 14px;'>Mã này có hiệu lực trong 5 phút. Vui lòng không chia sẻ mã này cho bất kỳ ai.</p>"
                +
                "<p style='color: #aaa; font-size: 12px; margin-top: 30px; text-align: center;'>&copy; 2026 AudioGear E-commerce</p>"
                +
                "</div></div>";
    }
}
