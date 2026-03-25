package vn.edu.ute.auth.adapter;

import vn.edu.ute.entity.User;
import vn.edu.ute.entity.enums.UserRole;
import vn.edu.ute.entity.enums.UserStatus;
import org.mindrot.jbcrypt.BCrypt;
import java.util.UUID;

/**
 * Adapter Class: Chuyển đổi GoogleProfile (Adaptee) thành User (Target)
 * Theo đúng định dạng bảng users trong Database
 */
public class GoogleToUserAdapter {

    private final GoogleProfile googleProfile;

    public GoogleToUserAdapter(GoogleProfile googleProfile) {
        this.googleProfile = googleProfile;
    }

    /**
     * Bóp dữ liệu của Google cho vừa với format bảng User
     */
    public User adaptToUser() {
        User user = new User();
        
        // 1. Email map trực tiếp
        user.setEmail(googleProfile.getEmail());
        
        // 2. Tên map trực tiếp
        user.setFullName(googleProfile.getName());
        
        // 3. Username không có ở Google, ta tạo tự động từ tiền tố email + mã ngẫu nhiên
        String baseUsername = googleProfile.getEmail().split("@")[0];
        String randomSuffix = UUID.randomUUID().toString().substring(0, 5);
        user.setUsername(baseUsername + "_" + randomSuffix);
        
        // 4. Mật khẩu để null để phân biệt với tài khoản đăng ký Local
        user.setPasswordHash(null);
        
        // 5. Các trường mặc định cho đăng nhập qua Google
        user.setRole(UserRole.customer);
        user.setStatus(UserStatus.active); // OAuth login thì email đã verify
        
        return user;
    }
}
