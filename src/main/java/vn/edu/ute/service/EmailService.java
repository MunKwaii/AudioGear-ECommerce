package vn.edu.ute.service;

import vn.edu.ute.auth.otp.OtpSenderStrategy;

public interface EmailService {
    void setOtpSenderStrategy(OtpSenderStrategy otpSender);
    boolean sendOtp(String target, String otp);
    String generateOTP();

    /**
     * Gửi Email định dạng HTML
     * @param to Email người nhận
     * @param subject Tiêu đề email
     * @param content Nội dung HTML
     * @return true nếu gửi thành công
     */
    boolean sendHtmlEmail(String to, String subject, String content);
}
