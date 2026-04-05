package vn.edu.ute.filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import vn.edu.ute.cart.CartFacadeService;
import vn.edu.ute.cart.GuestCartService;
import vn.edu.ute.dto.CartDTO;
import vn.edu.ute.homepage.factory.ServiceFactory;
import vn.edu.ute.util.JwtUtil;

import java.io.IOException;

/**
 * Filter to calculate cart count globally for every request
 * and make it available as a request attribute 'cartCount'
 */
@WebFilter(urlPatterns = "/*")
public class CartCountFilter implements Filter {

    private final JwtUtil jwtUtil = new JwtUtil();

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Initialization if needed
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        if (request instanceof HttpServletRequest req && response instanceof HttpServletResponse) {
            String path = req.getServletPath();
            
            // Skip static resources and API calls that don't need UI header data
            if (!path.contains(".") && !path.startsWith("/api/")) {
                Long userId = getUserIdFromCookie(req);
                int cartCount = 0;

                try {
                    if (userId != null) {
                        CartFacadeService cartService = ServiceFactory.getCartFacadeService();
                        CartDTO cart = cartService.getCartDetails(userId);
                        if (cart != null && cart.getItems() != null) {
                            cartCount = cart.getItems().size();
                        }
                    } else {
                        CartDTO cart = GuestCartService.getInstance().getCart(req);
                        if (cart != null && cart.getItems() != null) {
                            cartCount = cart.getItems().size();
                        }
                    }
                } catch (Exception e) {
                    // Fail gracefully, cart count will be 0
                    e.printStackTrace();
                }

                req.setAttribute("cartCount", cartCount);
            }
        }
        
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        // Cleanup if needed
    }

    private Long getUserIdFromCookie(HttpServletRequest req) {
        if (req.getCookies() == null) return null;
        return java.util.Arrays.stream(req.getCookies())
                .filter(c -> "accessToken".equals(c.getName()))
                .map(Cookie::getValue)
                .filter(jwtUtil::validateToken)
                .map(jwtUtil::getUserIdFromToken)
                .findFirst()
                .orElse(null);
    }
}
