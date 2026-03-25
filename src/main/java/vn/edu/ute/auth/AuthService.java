package vn.edu.ute.auth;

import vn.edu.ute.auth.strategy.AuthRequest;
import vn.edu.ute.auth.strategy.GoogleLoginStrategy;
import vn.edu.ute.auth.strategy.LocalLoginStrategy;
import vn.edu.ute.auth.strategy.LoginStrategy;
import vn.edu.ute.entity.User;
import vn.edu.ute.entity.enums.UserStatus;

import java.util.EnumMap;
import java.util.Map;

/**
 * Context class cho luồng Đăng nhập, quản lý và vận hành LoginStrategy
 */
public class AuthService {

    private final Map<LoginType, LoginStrategy> strategies;

    public AuthService() {
        strategies = new EnumMap<>(LoginType.class);
        strategies.put(LoginType.LOCAL, new LocalLoginStrategy());
        strategies.put(LoginType.GOOGLE, new GoogleLoginStrategy());
    }

    /**
     * Entry-point luồng Đăng nhập (Strategy Pattern + UML workflow)
     */
    public User login(LoginType loginType, AuthRequest request) {
        // 1. Phân phối cho Strategy tương ứng để lấy thông tin người dùng / xác thực
        LoginStrategy strategy = strategies.get(loginType);
        if (strategy == null) {
            throw new IllegalArgumentException("Không hỗ trợ phương thức đăng nhập này");
        }

        User user = strategy.authenticate(request);

        // 2. Chế độ kiểm tra chung (UML: Kiểm tra trạng thái Lock, Pending...)
        if (user.getStatus() == UserStatus.locked) {
            throw new RuntimeException("Tài khoản bị khóa");
        }
        
        if (user.getStatus() == UserStatus.pending) {
            throw new RuntimeException("Tài khoản đang chờ xác thực Email/OTP");
        }

        // 3. Nếu mọi thông tin đúng (Active), thực hiện trả về để Controller tạo Session/JWT Token
        return user;
    }
}
