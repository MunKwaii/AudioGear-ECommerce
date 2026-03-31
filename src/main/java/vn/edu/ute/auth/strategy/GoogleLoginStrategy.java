package vn.edu.ute.auth.strategy;
import vn.edu.ute.dto.request.AuthRequest;

import vn.edu.ute.auth.adapter.GoogleProfile;
import vn.edu.ute.auth.adapter.GoogleToUserAdapter;
import vn.edu.ute.dao.UserDAO;
import vn.edu.ute.dao.impl.UserDAOImpl;
import vn.edu.ute.entity.User;

import java.util.Optional;

/**
 * Strategy xử lý đăng nhập qua Google OAuth
 * Tích hợp Adapter Pattern để chuyển đổi GoogleProfile thành User Entity
 */
public class GoogleLoginStrategy implements LoginStrategy {

    private final UserDAO userDAO;

    public GoogleLoginStrategy() {
        this.userDAO = new UserDAOImpl();
    }

    @Override
    public User authenticate(AuthRequest request) {
        GoogleProfile googleProfile = request.getGoogleProfile();
        if (googleProfile == null || googleProfile.getEmail() == null) {
            throw new IllegalArgumentException("Dữ liệu từ Google bị thiếu hoặc không hợp lệ.");
        }

        String email = googleProfile.getEmail();
        Optional<User> existingUserOpt = userDAO.findByEmail(email);

        // Nếu email đã tồn tại => Đăng nhập thành công, trả về User
        if (existingUserOpt.isPresent()) {
            return existingUserOpt.get();
        }

        // Tự động tạo tài khoản mới nếu chưa tồn tại sử dụng Adapter
        GoogleToUserAdapter adapter = new GoogleToUserAdapter(googleProfile);
        User adaptedUser = adapter.adaptToUser();
        
        // Lưu vào DB
        return userDAO.save(adaptedUser);
    }
}
