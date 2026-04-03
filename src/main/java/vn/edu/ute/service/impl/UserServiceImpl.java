package vn.edu.ute.service.impl;

import vn.edu.ute.dao.UserDAO;
import vn.edu.ute.dao.impl.UserDAOImpl;
import vn.edu.ute.dto.request.RegisterRequest;
import vn.edu.ute.entity.User;
import vn.edu.ute.entity.enums.UserRole;
import vn.edu.ute.entity.enums.UserStatus;
import vn.edu.ute.service.UserService;
import vn.edu.ute.util.PasswordUtil;

import java.util.Optional;

/**
 * Service xử lý nghiệp vụ chung cho User
 */
public class UserServiceImpl implements UserService {

    private final UserDAO userDAO;

    public UserServiceImpl() {
        this.userDAO = new UserDAOImpl();
    }

    @Override
    public boolean isUsernameExists(String username) {
        return userDAO.findByUsername(username).isPresent();
    }

    @Override
    public boolean isEmailExists(String email) {
        return userDAO.findByEmail(email).isPresent();
    }

    @Override
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
    @Override
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
    @Override
    public void resetPassword(String email, String newPassword) {
        Optional<User> userOpt = userDAO.findByEmail(email);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("Xin lỗi, tài khoản không tồn tại!");
        }

        User user = userOpt.get();
        user.setPasswordHash(PasswordUtil.hashPassword(newPassword));
        userDAO.save(user);
    }

    @Override
    public User getUserById(Long id) {
        return userDAO.findById(id)
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại!"));
    }

    @Override
    public void updateUserProfile(Long userId, String fullName, String phoneNumber) {
        User user = getUserById(userId);
        user.setFullName(fullName);
        user.setPhoneNumber(phoneNumber);
        userDAO.save(user);
    }

    @Override
    public void updateAvatar(Long userId, String avatarUrl) {
        User user = getUserById(userId);
        user.setAvatar(avatarUrl);
        userDAO.save(user);
    }

    @Override
    public java.util.List<User> searchUsers(String keyword, UserRole role, UserStatus status) {
        return userDAO.search(keyword, role, status);
    }

    @Override
    public java.util.List<User> searchUsers(String keyword, UserRole role, UserStatus status, int offset, int limit) {
        return userDAO.search(keyword, role, status, offset, limit);
    }

    @Override
    public long countSearchUsers(String keyword, UserRole role, UserStatus status) {
        return userDAO.countSearch(keyword, role, status);
    }

    @Override
    public User createUser(String email, String username, String fullName, String password, String phoneNumber,
                           UserRole role, UserStatus status) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email không được để trống");
        }
        if (fullName == null || fullName.trim().isEmpty()) {
            throw new IllegalArgumentException("Họ tên không được để trống");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Mật khẩu không được để trống");
        }
        if (password.length() < 6) {
            throw new IllegalArgumentException("Mật khẩu phải có ít nhất 6 ký tự");
        }
        if (userDAO.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("Email đã tồn tại");
        }

        String finalUsername = (username == null || username.trim().isEmpty())
                ? email.split("@")[0]
                : username.trim();
        if (userDAO.findByUsername(finalUsername).isPresent()) {
            throw new IllegalArgumentException("Username đã tồn tại");
        }

        User user = new User();
        user.setEmail(email.trim());
        user.setUsername(finalUsername);
        user.setFullName(fullName.trim());
        user.setPasswordHash(PasswordUtil.hashPassword(password));
        user.setPhoneNumber(phoneNumber == null || phoneNumber.trim().isEmpty() ? null : phoneNumber.trim());
        user.setRole(role == null ? UserRole.customer : role);
        user.setStatus(status == null ? UserStatus.active : status);
        return userDAO.save(user);
    }

    @Override
    public User updateUser(Long id, String email, String username, String fullName, String password, String phoneNumber,
                           UserRole role, UserStatus status) {
        User user = getUserById(id);

        if (email != null && !email.trim().isEmpty() && !email.trim().equalsIgnoreCase(user.getEmail())) {
            if (userDAO.findByEmail(email.trim()).isPresent()) {
                throw new IllegalArgumentException("Email đã tồn tại");
            }
            user.setEmail(email.trim());
        }
        if (username != null && !username.trim().isEmpty() && !username.trim().equalsIgnoreCase(user.getUsername())) {
            if (userDAO.findByUsername(username.trim()).isPresent()) {
                throw new IllegalArgumentException("Username đã tồn tại");
            }
            user.setUsername(username.trim());
        }
        if (fullName != null && !fullName.trim().isEmpty()) {
            user.setFullName(fullName.trim());
        }
        if (password != null && !password.trim().isEmpty()) {
            if (password.length() < 6) {
                throw new IllegalArgumentException("Mật khẩu phải có ít nhất 6 ký tự");
            }
            user.setPasswordHash(PasswordUtil.hashPassword(password));
        }
        if (phoneNumber != null) {
            user.setPhoneNumber(phoneNumber.trim().isEmpty() ? null : phoneNumber.trim());
        }
        if (role != null) {
            user.setRole(role);
        }
        if (status != null) {
            user.setStatus(status);
        }
        return userDAO.save(user);
    }

    @Override
    public User updateUserStatus(Long id, UserStatus status) {
        User user = getUserById(id);
        user.setStatus(status);
        return userDAO.save(user);
    }

    @Override
    public User updateUserRole(Long id, UserRole role) {
        User user = getUserById(id);
        user.setRole(role);
        return userDAO.save(user);
    }

    @Override
    public void lockUser(Long id) {
        User user = getUserById(id);
        user.setStatus(UserStatus.locked);
        userDAO.save(user);
    }
}
