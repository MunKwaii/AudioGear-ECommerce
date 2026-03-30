package vn.edu.ute.service;

import vn.edu.ute.dao.UserDAO;
import vn.edu.ute.dao.impl.UserDAOImpl;
import vn.edu.ute.dto.request.RegisterRequest;
import vn.edu.ute.entity.User;
import vn.edu.ute.entity.enums.UserStatus;
import vn.edu.ute.util.PasswordUtil;

import java.util.Optional;

/**
 * Service xử lý nghiệp vụ chung cho User
 */
public class UserService {

    private final UserDAO userDAO;

    public UserService() {
        this.userDAO = new UserDAOImpl();
    }

    public boolean isUsernameExists(String username) {
        return userDAO.findByUsername(username).isPresent();
    }

    public boolean isEmailExists(String email) {
        return userDAO.findByEmail(email).isPresent();
    }

    public User registerPendingUser(RegisterRequest request) {
        // 1. Kiểm tra Email
        Optional<User> existingByEmail = userDAO.findByEmail(request.getEmail());
        if (existingByEmail.isPresent()) {
            User existingUser = existingByEmail.get();
            if (existingUser.getStatus() == UserStatus.active) {
                throw new RuntimeException("Email đã được sử dụng!");
            } else {
                // Email tồn tại nhưng PENDING -> Cập nhật thông tin mới nhất và gửi lại OTP
                existingUser.setUsername(request.getUsername());
                existingUser.setFullName(request.getFullName());
                existingUser.setPasswordHash(PasswordUtil.hashPassword(request.getPassword()));
                return userDAO.save(existingUser);
            }
        }

        // 2. Kiểm tra Username
        Optional<User> existingByUsername = userDAO.findByUsername(request.getUsername());
        if (existingByUsername.isPresent()) {
            User existingUser = existingByUsername.get();
            if (existingUser.getStatus() == UserStatus.active) {
                throw new RuntimeException("Tên đăng nhập đã tồn tại!");
            } else {
                // Username tồn tại nhưng PENDING -> Cập nhật thông tin
                existingUser.setEmail(request.getEmail());
                existingUser.setFullName(request.getFullName());
                existingUser.setPasswordHash(PasswordUtil.hashPassword(request.getPassword()));
                return userDAO.save(existingUser);
            }
        }

        // 3. Nếu chưa tồn tại -> Tạo mới hoàn toàn
        User user = new User();
        user.setUsername(request.getUsername());
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPasswordHash(PasswordUtil.hashPassword(request.getPassword()));
        user.setStatus(UserStatus.pending); // Bắt buộc chờ OTP

        return userDAO.save(user);
    }

    /**
     * Kích hoạt tài khoản
     */
    public User activateUser(String email) {
        Optional<User> userOpt = userDAO.findByEmail(email);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("Không tìm thấy người dùng!");
        }

        User user = userOpt.get();
        if (user.getStatus() == UserStatus.active) {
            throw new RuntimeException("Tài khoản đã được kích hoạt trước đó!");
        }
        
        user.setStatus(UserStatus.active);
        return userDAO.save(user);
    }

    /**
     * Đặt lại mật khẩu (dùng cho luồng Forgot Password)
     */
    public void resetPassword(String email, String newPassword) {
        Optional<User> userOpt = userDAO.findByEmail(email);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("Xin lỗi, tài khoản không tồn tại!");
        }

        User user = userOpt.get();
        user.setPasswordHash(PasswordUtil.hashPassword(newPassword));
        userDAO.save(user);
    }
}
