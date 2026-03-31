package vn.edu.ute.auth.strategy;
import vn.edu.ute.dto.request.AuthRequest;

import vn.edu.ute.entity.User;

/**
 * Strategy Interface cho việc Đăng nhập
 */
public interface LoginStrategy {
    /**
     * Xác thực người dùng dựa trên AuthRequest
     * @param request Chứa dữ liệu chứng thực (credentials / tokens)
     * @return Đối tượng User nếu đăng nhập thành công
     * @throws RuntimeException (hoặc exception cụ thể) nếu thất bại
     */
    User authenticate(AuthRequest request);
}
