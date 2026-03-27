package vn.edu.ute.strategy.otp;

/**
 * Strategy Pattern Interface for sending OTP
 */
public interface OtpSenderStrategy {

    /**
     * Send OTP to the target destination
     * @param target The destination (email address, phone number, etc.)
     * @param otp The OTP string
     * @return true if successfully sent
     */
    boolean sendOtp(String target, String otp);
}
