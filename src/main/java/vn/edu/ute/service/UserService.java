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
}
