package vn.edu.ute.service;

import vn.edu.ute.dto.request.RegisterRequest;
import vn.edu.ute.entity.User;

public interface UserService {
    boolean isUsernameExists(String username);
    boolean isEmailExists(String email);
    User registerPendingUser(RegisterRequest request);
    User activateUser(String email);
    void resetPassword(String email, String newPassword);
    User getUserById(Long id);
    void updateUserProfile(Long userId, String fullName, String phoneNumber);
    void updateAvatar(Long userId, String avatarUrl);
    java.util.List<User> searchUsers(String keyword, vn.edu.ute.entity.enums.UserRole role, vn.edu.ute.entity.enums.UserStatus status);
    User createUser(String email, String username, String fullName, String password, String phoneNumber,
                    vn.edu.ute.entity.enums.UserRole role, vn.edu.ute.entity.enums.UserStatus status);
    User updateUser(Long id, String email, String username, String fullName, String password, String phoneNumber,
                    vn.edu.ute.entity.enums.UserRole role, vn.edu.ute.entity.enums.UserStatus status);
    User updateUserStatus(Long id, vn.edu.ute.entity.enums.UserStatus status);
    User updateUserRole(Long id, vn.edu.ute.entity.enums.UserRole role);
    void lockUser(Long id);
}
