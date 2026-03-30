package vn.edu.ute.service;

import vn.edu.ute.strategy.otp.EmailOtpSender;
import vn.edu.ute.strategy.otp.OtpSenderStrategy;

import java.util.Random;

/**
 * Service quản lý gửi email và tạo OTP.
 * Áp dụng DI/Strategy Pattern để gửi OTP (mặc định cấu hình Email).
 */
public class EmailService {

    private OtpSenderStrategy otpSender;
    private final Random random = new Random();

    public EmailService() {
        // Mặc định sử dụng Email để gửi OTP
        this.otpSender = new EmailOtpSender();
    }

    // Dependency Injection thông qua Setter (đề phòng muốn đổi qua SMS)
    public void setOtpSenderStrategy(OtpSenderStrategy otpSender) {
        this.otpSender = otpSender;
    }

    /**
     * Gửi mã OTP
     * @param target (Email, Số điện thoại...)
     * @param otp Mã được sinh ra
     * @return true nếu gửi thành công
     */
    public boolean sendOtp(String target, String otp) {
        return otpSender.sendOtp(target, otp);
    }

    /**
     * Sinh mã OTP 6 chữ số ngẫu nhiên
     */
    public String generateOTP() {
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }
}
