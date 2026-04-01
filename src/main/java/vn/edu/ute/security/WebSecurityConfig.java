package vn.edu.ute.security;

import java.util.Arrays;
import java.util.List;

/**
 * Cấu hình bảo mật đường dẫn và phân quyền (Mô phỏng cơ chế của Spring Security).
 * Thay vì cấu hình rải rác trong Filter, mọi quy tắc bảo mật được tập trung ở đây.
 */
public class WebSecurityConfig {

    // 1. CÁC ĐƯỜNG DẪN CÔNG KHAI (PERMIT ALL) 
    // Dựa trên Use Case: Guest (Khách hàng)
    private static final List<String> PERMIT_ALL_PATHS = Arrays.asList(
            // Auth & OAuth
            "/api/auth/login",
            "/api/auth/register",
            "/api/auth/google",
            "/api/auth/forgot-password",
            "/api/auth/otp",
            
            // Public Views
            "/login",
            "/register",
            "/forgot-password",
            "/product/",
            
            // Public APIs (Duyệt & Tìm kiếm)
            "/api/products", // Xem danh sách, chi tiết, đánh giá, lọc
            "/api/v1/products", 
            "/api/categories",
            "/api/brands",
            
            // Đặt hàng & Tra cứu public
            "/api/cart",          // Khách vãng lai cũng có giỏ hàng (session-based)
            "/api/checkout",      // Hỗ trợ Guest Checkout
            "/api/orders/track",  // Tra cứu công khai
            
            // Tài nguyên tĩnh
            "/css/",
            "/js/",
            "/images/",
            "/assets/",
            "/webjars/",
            "/static/"
    );

    // 2. CÁC ĐƯỜNG DẪN QUẢN TRỊ VIÊN (ADMIN ONLY)
    // Dựa trên Use Case: Admin (Quản trị hệ thống)
    private static final List<String> ADMIN_PATHS = Arrays.asList(
            "/api/admin/",         // Toàn bộ API quản trị
            "/admin/"              // Giao diện quản trị
    );

    // 3. CÁC ĐƯỜNG DẪN CỦA THÀNH VIÊN ĐÃ ĐĂNG NHẬP (AUTHENTICATED)
    // Bao gồm User và Admin
    private static final List<String> USER_PATHS = Arrays.asList(
            "/api/user/",          // Quản lý Profile, Mật khẩu
            "/api/orders/history", // Xem lịch sử đặt hàng
            "/api/orders/cancel",  // Huỷ đơn
            "/api/reviews/rate",   // Đánh giá sản phẩm đã mua
            "/user/"               // Giao diện cá nhân
    );

    /**
     * Kiểm tra xem đường dẫn có được công khai không (PermitAll)
     */
    public static boolean isPermitAll(String requestURI) {
        // Luôn cho phép trang lỗi hoặc đúng trang chủ "/"
        if (requestURI.startsWith("/error") || requestURI.equals("/")) return true;
        
        for (String path : PERMIT_ALL_PATHS) {
            if (requestURI.startsWith(path)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Kiểm tra xem đường dẫn có yêu cầu quyền Admin không
     */
    public static boolean requiresAdminRole(String requestURI) {
        for (String path : ADMIN_PATHS) {
            if (requestURI.startsWith(path)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Kiểm tra xem đường dẫn có yêu cầu Đăng nhập (Authenticated nhưng không cần Admin)
     */
    public static boolean requiresAuthentication(String requestURI) {
        // Tóm lại: Nếu không Public, nó CHẮC CHẮN yêu cầu Authenticated.
        // Code này check tường minh danh sách USER_PATHS để phòng trường hợp ứng dụng mở rông.
        for (String path : USER_PATHS) {
            if (requestURI.startsWith(path)) {
                return true;
            }
        }
        
        // Mặc định: Nếu uri không phải permitAll thì auto yêu cầu Authenticate (.anyRequest().authenticated() trong Spring)
        return !isPermitAll(requestURI);
    }
}
