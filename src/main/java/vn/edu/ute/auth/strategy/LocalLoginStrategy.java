package vn.edu.ute.auth.strategy;

import org.mindrot.jbcrypt.BCrypt;
import vn.edu.ute.dao.UserDAO;
import vn.edu.ute.entity.User;

import java.util.Optional;

/**
 * Strategy xử lý đăng nhập truyền thống bằng Username/Email và Password
 */
public class LocalLoginStrategy implements LoginStrategy {

    private final UserDAO userDAO;

    public LocalLoginStrategy() {
        this.userDAO = new UserDAO();
    }

    @Override
    public User authenticate(AuthRequest request) {
        if (request.getUsernameOrEmail() == null || request.getPassword() == null) {
            throw new IllegalArgumentException("Thiếu thông tin đăng nhập.");
        }

        // Tìm user bằng username hoặc email
        Optional<User> userOpt = userDAO.findByUsernameOrEmail(request.getUsernameOrEmail());
        
        if (userOpt.isEmpty()) {
            throw new RuntimeException("Sai thông tin: Không tìm thấy tài khoản.");
        }

        User user = userOpt.get();

        // Kiểm tra mật khẩu bằng BCrypt
        if (!BCrypt.checkpw(request.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Sai thông tin: Mật khẩu không chính xác.");
        }

        return user;
    }
}
