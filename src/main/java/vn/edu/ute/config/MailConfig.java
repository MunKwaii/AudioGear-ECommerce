package vn.edu.ute.config;

import jakarta.mail.Authenticator;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import java.util.Properties;

/**
 * Cấu hình SMTP Mail tập trung
 */
public class MailConfig {
    
    // Cấu hình SMTP mặc định (Sử dụng Gmail)
    public static final String SMTP_HOST = "smtp.gmail.com";
    public static final String SMTP_PORT = "587";
    
    // Sử dụng thông tin mail đã được cấu hình trong hệ thống
    public static final String SENDER_EMAIL = "trihieuvo4@gmail.com";
    public static final String SENDER_PASSWORD = "exeh vgvi mpwy wwoo";
    public static final String SYSTEM_NAME = "AudioGear System";

    /**
     * Tạo Mail Session với Authenticator
     */
    public static Session getSession() {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.starttls.required", "true");
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", SMTP_PORT);
        
        // Bổ sung cho tính ổn định với Gmail
        props.put("mail.smtp.ssl.protocols", "TLSv1.2 TLSv1.3");
        props.put("mail.smtp.ssl.trust", SMTP_HOST);
        
        // Bật debug để theo dõi log SMTP nếu cần
        props.put("mail.debug", "true");

        return Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(SENDER_EMAIL, SENDER_PASSWORD);
            }
        });
    }
}
