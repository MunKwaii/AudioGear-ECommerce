package vn.edu.ute.auth;

import org.mindrot.jbcrypt.BCrypt;
import vn.edu.ute.auth.adapter.GoogleProfile;
import vn.edu.ute.dto.request.AuthRequest;
import vn.edu.ute.dao.UserDAO;
import vn.edu.ute.dao.impl.UserDAOImpl;
import vn.edu.ute.entity.User;
import vn.edu.ute.entity.enums.UserStatus;
import vn.edu.ute.service.AuthService;
import vn.edu.ute.service.impl.AuthServiceImpl;

public class TestAuth {
    public static void main(String[] args) {
        System.out.println("--- Bắt đầu Test luồng Đăng nhập (Strategy + Adapter) ---");
        
        AuthService authService = new AuthServiceImpl();
        UserDAO dao = new UserDAOImpl();
        
        // 1. Dọn dẹp Mock Data (Nếu chạy nhiều lần, email có thể bị trùng do UNIQUE)
        // Lưu ý: Trong thực tế ta truncate DB test, nhưng script này chạy cẩn thận
        String testEmail = "localtest@gmail.com";
        String googleEmail = "googletest@gmail.com";
        
        System.out.println("\n[Test 1]: Đăng nhập Local sai thông tin");
        try {
            AuthRequest badReq = new AuthRequest();
            badReq.setUsernameOrEmail(testEmail);
            badReq.setPassword("wrongpass");
            authService.login(LoginType.LOCAL, badReq);
        } catch (Exception e) {
            System.out.println("-> OK! Bắt được lỗi mong muốn: " + e.getMessage());
        }

        System.out.println("\n[Test 2]: Seed User và Đăng nhập Local đúng thông tin");
        try {
            User mockUser = new User();
            mockUser.setUsername("testuser123");
            mockUser.setEmail(testEmail);
            mockUser.setFullName("Test User");
            mockUser.setPasswordHash(BCrypt.hashpw("realpass", BCrypt.gensalt()));
            mockUser.setStatus(UserStatus.active);
            
            // Nếu chưa có thì save (tránh lỗi duplicate email)
            if (dao.findByEmail(testEmail).isEmpty()) {
                dao.save(mockUser);
            }
            
            AuthRequest goodReq = new AuthRequest();
            goodReq.setUsernameOrEmail(testEmail);
            goodReq.setPassword("realpass");
            User loggedIn = authService.login(LoginType.LOCAL, goodReq);
            System.out.println("-> OK! Đăng nhập Local thành công với User: " + loggedIn.getFullName());
        } catch (Exception e) {
            System.out.println("-> FAILED: Lỗi không mong muốn: " + e.getMessage());
        }

        System.out.println("\n[Test 3]: Đăng nhập qua Google (Auto Registration bằng Adapter)");
        try {
            GoogleProfile proxyProfile = new GoogleProfile();
            proxyProfile.setEmail(googleEmail);
            proxyProfile.setName("Google Tester");
            proxyProfile.setGoogleId("1234567890");
            
            AuthRequest ggReq = new AuthRequest();
            ggReq.setGoogleProfile(proxyProfile);
            
            User ggUser = authService.login(LoginType.GOOGLE, ggReq);
            System.out.println("-> OK! Đăng nhập Google thành công. Tên trong DB: " + ggUser.getFullName());
            System.out.println("     Username tự cấp phát qua Adapter: " + ggUser.getUsername());
            
        } catch (Exception e) {
            System.out.println("-> FAILED: Lỗi không mong muốn: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("\n--- Hoàn tất Test ---");
        System.exit(0);
    }
}
