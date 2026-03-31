package vn.edu.ute.service.impl;

import vn.edu.ute.auth.otp.EmailOtpSender;
import vn.edu.ute.auth.otp.OtpSenderStrategy;
import vn.edu.ute.service.EmailService;

import java.util.Random;

/**
 * Service quản lý gửi email và tạo OTP.
 * Áp dụng DI/Strategy Pattern để gửi OTP (mặc định cấu hình Email).
 */
public class EmailServiceImpl implements EmailService {

    private OtpSenderStrategy otpSender;
    private final Random random = new Random();

    public EmailServiceImpl() {
        // Mặc định sử dụng Email để gửi OTP
        this.otpSender = new EmailOtpSender();
    }

    // Dependency Injection thông qua Setter (đề phòng muốn đổi qua SMS)
    @Override
    public void setOtpSenderStrategy(OtpSenderStrategy otpSender) {
        this.otpSender = otpSender;
    }

    /**
     * Gửi mã OTP
     * @param target (Email, Số điện thoại...)
     * @param otp Mã được sinh ra
     * @return true nếu gửi thành công
     */
    @Override
    public boolean sendOtp(String target, String otp) {
        return otpSender.sendOtp(target, otp);
    }

    /**
     * Sinh mã OTP 6 chữ số ngẫu nhiên
     */
    @Override
    public String generateOTP() {
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }
}
