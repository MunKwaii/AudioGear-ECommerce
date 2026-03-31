package vn.edu.ute.auth.facade;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import vn.edu.ute.dto.request.RegisterRequest;
import vn.edu.ute.service.EmailService;
import vn.edu.ute.service.RedisService;
import vn.edu.ute.service.UserService;
import vn.edu.ute.service.impl.EmailServiceImpl;
import vn.edu.ute.service.impl.RedisServiceImpl;
import vn.edu.ute.service.impl.UserServiceImpl;

/**
 * AuthFacade: Áp dụng Facade Pattern.
 * Đóng gói toàn bộ luồng xử lý phức tạp của Auth lại, giúp Controller gọn gàng.
 */
public class AuthFacade {

    private static final Logger logger = LogManager.getLogger(AuthFacade.class);
    private final UserService userService;
    private final RedisService redisService;
    private final EmailService emailService;

    public AuthFacade() {
        this.userService = new UserServiceImpl();
        this.redisService = new RedisServiceImpl();
        this.emailService = new EmailServiceImpl();
    }

    // ==========================================
    // LUỒNG 1: ĐĂNG KÝ (REGISTRATION) VỚI OTP
    // ==========================================

    /**
     * B1: Yêu cầu đăng ký. Tạo User trạng thái PENDING và gửi OTP.
     */
    public void requestRegister(RegisterRequest request) {
        String email = request.getEmail();
        
        // Anti-spam check
        if (!redisService.canSendOtp(email)) {
            throw new RuntimeException("Vui lòng đợi 1 phút trước khi yêu cầu gửi lại mã OTP.");
        }

        // Tạo user PENDING (ném Exception nếu trùng email/username)
        userService.registerPendingUser(request);

        // Sinh OTP
        String otp = emailService.generateOTP();

        // Lưu vào Redis Cache
        redisService.saveRegisterOtp(email, otp);
        redisService.setOtpCooldown(email);
        redisService.clearOtpAttempts(email);

        // Phát tín hiệu gửi Email cho Observer thi hành (Ở đây làm trực tiếp cho đồng bộ)
        boolean isSent = emailService.sendOtp(email, otp);
        if (!isSent) {
            throw new RuntimeException("Rất tiếc, lỗi server trong lúc gửi Email. Vui lòng thử lại!");
        }
    }

    /**
     * B2: Xác thực mã OTP và kích hoạt tài khoản.
     */
    public void verifyRegister(String email, String inputOtp) {
        checkAntiSpam(email);

        boolean isValid = redisService.verifyRegisterOtp(email, inputOtp);
        if (!isValid) {
            handleFailedAttempt(email);
        }

        // Active tài khoản trong DB
        userService.activateUser(email);

        // Dọn dẹp RAM
        redisService.deleteRegisterOtp(email);
        redisService.clearOtpAttempts(email);
    }

    // ==========================================
    // LUỒNG 2: QUÊN MẬT KHẨU (FORGOT PASSWORD)
    // ==========================================

    /**
     * B1: Yêu cầu mã OTP qua Email
     */
    public void requestForgotPassword(String email) {
        if (!userService.isEmailExists(email)) {
            throw new RuntimeException("Email này chưa được đăng ký trong hệ thống.");
        }

        if (!redisService.canSendOtp(email)) {
            throw new RuntimeException("Vui lòng đợi 1 phút trước khi gửi yêu cầu mới.");
        }

        String otp = emailService.generateOTP();

        redisService.saveForgotPasswordOtp(email, otp);
        redisService.setOtpCooldown(email);
        redisService.clearOtpAttempts(email);

        boolean isSent = emailService.sendOtp(email, otp);
        if (!isSent) {
            throw new RuntimeException("Server đang quá tải, không thể gửi mail lúc này.");
        }
    }

    /**
     * B2: Client gửi mã để check hợp lệ không (Chưa đổi mật khẩu vội)
     */
    public void verifyForgotPassword(String email, String inputOtp) {
        checkAntiSpam(email);

        boolean isValid = redisService.verifyForgotPasswordOtp(email, inputOtp);
        if (!isValid) {
            handleFailedAttempt(email);
        }
    }

    /**
     * B3: Set mật khẩu mới
     */
    public void resetPassword(String email, String otp, String newPassword) {
        // Double check mã OTP trước khi cho phép Reset
        verifyForgotPassword(email, otp);

        // Ghi đè Pass Hash mới
        userService.resetPassword(email, newPassword);

        // Dọn rác
        redisService.deleteForgotPasswordOtp(email);
        redisService.clearOtpAttempts(email);
    }

    // ==========================================
    // UTILS
    // ==========================================

    private void checkAntiSpam(String email) {
        if (redisService.isOtpBlocked(email)) {
            throw new RuntimeException("Bạn đã nhập sai mã OTP quá 5 lần. Vui lòng quay lại sau 30 phút.");
        }
    }

    private void handleFailedAttempt(String email) {
        int attempt = redisService.incrementOtpAttempt(email);
        int remaining = 5 - attempt;
        if (remaining > 0) {
            throw new RuntimeException("Mã OTP không đúng hoặc đã hết hạn. Bạn còn " + remaining + " lần thử.");
        } else {
            throw new RuntimeException("Trượt quá 5 lần. Tính năng bị khóa trong 30 phút.");
        }
    }
}
