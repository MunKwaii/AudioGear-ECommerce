package vn.edu.ute.auth;

import org.mindrot.jbcrypt.BCrypt;
import vn.edu.ute.dao.UserDAO;
import vn.edu.ute.dto.RegisterRequest;
import vn.edu.ute.entity.User;

import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Service xử lý toàn bộ luồng Đăng ký tài khoản:
 * - Kiểm tra dữ liệu đầu vào
 * - Kiểm tra trùng username/email
 * - Mã hóa mật khẩu
 * - Lưu tài khoản mới vào hệ thống
 */
public class RegisterService {

    private final UserDAO userDAO;

    public RegisterService() {
        this.userDAO = new UserDAO();
    }

    /**
     * Entry-point cho luồng Đăng ký tài khoản
     */
    public User register(RegisterRequest request) {
        validateRequest(request);

        Optional<User> userByUsername = userDAO.findByUsername(request.getUsername().trim());
        if (userByUsername.isPresent()) {
            throw new RuntimeException("Tên đăng nhập đã tồn tại");
        }

        Optional<User> userByEmail = userDAO.findByEmail(request.getEmail().trim());
        if (userByEmail.isPresent()) {
            throw new RuntimeException("Email đã được sử dụng");
        }

        String hashedPassword = BCrypt.hashpw(request.getPassword(), BCrypt.gensalt());

        User user = new User(
                request.getUsername().trim(),
                request.getFullName().trim(),
                request.getEmail().trim(),
                hashedPassword
        );

        return userDAO.save(user);
    }

    /**
     * Kiểm tra dữ liệu nhập vào trước khi tạo tài khoản
     */
    private void validateRequest(RegisterRequest request) {
        String username = safeTrim(request.getUsername());
        String fullName = safeTrim(request.getFullName());
        String email = safeTrim(request.getEmail());
        String password = safeTrim(request.getPassword());
        String confirmPassword = safeTrim(request.getConfirmPassword());

        if (username.isEmpty() || fullName.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            throw new IllegalArgumentException("Vui lòng nhập đầy đủ thông tin");
        }

        if (username.length() < 4) {
            throw new IllegalArgumentException("Tên đăng nhập phải có ít nhất 4 ký tự");
        }

        if (fullName.length() < 2) {
            throw new IllegalArgumentException("Họ tên không hợp lệ");
        }

        if (!isValidEmail(email)) {
            throw new IllegalArgumentException("Email không hợp lệ");
        }

        if (password.length() < 6) {
            throw new IllegalArgumentException("Mật khẩu phải có ít nhất 6 ký tự");
        }

        if (!password.equals(confirmPassword)) {
            throw new IllegalArgumentException("Mật khẩu xác nhận không khớp");
        }
    }

    /**
     * Hàm tiện ích loại bỏ khoảng trắng thừa và chống null
     */
    private String safeTrim(String value) {
        return value == null ? "" : value.trim();
    }

    /**
     * Kiểm tra định dạng email cơ bản
     */
    private boolean isValidEmail(String email) {
        return Pattern.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$", email);
    }
}