package vn.edu.ute.service;

public interface RedisService {
    void blacklistAccessToken(String token, long expirySeconds);
    boolean isTokenBlacklisted(String token);
    void saveRegisterOtp(String email, String otp);
    void saveForgotPasswordOtp(String email, String otp);
    boolean verifyRegisterOtp(String email, String otp);
    boolean verifyForgotPasswordOtp(String email, String otp);
    void deleteRegisterOtp(String email);
    void deleteForgotPasswordOtp(String email);
    boolean canSendOtp(String email);
    void setOtpCooldown(String email);
    boolean isOtpBlocked(String email);
    int incrementOtpAttempt(String email);
    void clearOtpAttempts(String email);
}
