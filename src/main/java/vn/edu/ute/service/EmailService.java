package vn.edu.ute.service;

import vn.edu.ute.auth.otp.OtpSenderStrategy;

public interface EmailService {
    void setOtpSenderStrategy(OtpSenderStrategy otpSender);
    boolean sendOtp(String target, String otp);
    String generateOTP();
}
