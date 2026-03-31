package vn.edu.ute.controller;

import vn.edu.ute.dto.CartDTO;
import vn.edu.ute.factory.ServiceFactory;
import vn.edu.ute.util.JwtUtil;
import vn.edu.ute.config.ThymeleafConfig;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.web.IWebExchange;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;

import java.io.IOException;

@WebServlet(urlPatterns = {"/cart", "/cart/add", "/cart/remove", "/cart/update"})
public class CartController extends HttpServlet {

    private Long getUserIdFromCookie(HttpServletRequest req) {
        if (req.getCookies() == null) return null;
        for (Cookie c : req.getCookies()) {
            if ("accessToken".equals(c.getName())) {
                JwtUtil jwt = new JwtUtil();
                if (jwt.validateToken(c.getValue())) {
                    return jwt.getUserIdFromToken(c.getValue());
                }
            }
        }
        return null;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getServletPath();
        Long userId = getUserIdFromCookie(req);

        if (userId == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        if ("/cart".equals(path)) {
            CartDTO cart = ServiceFactory.getCartFacadeService().getCartDetails(userId);
            renderPage(req, resp, "cart", cart);
        } else {
            resp.sendRedirect(req.getContextPath() + "/cart");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getServletPath();
        Long userId = getUserIdFromCookie(req);

        if (userId == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        try {
            if ("/cart/add".equals(path)) {
                Long productId = Long.parseLong(req.getParameter("productId"));
                int quantity = 1;
                if (req.getParameter("quantity") != null) {
                    quantity = Integer.parseInt(req.getParameter("quantity"));
                }
                ServiceFactory.getCartFacadeService().addToCart(userId, productId, quantity);
                
                // Trả về trang trước đó hoặc giỏ hàng
                String referer = req.getHeader("Referer");
                if (referer != null && !referer.contains("/cart")) {
                    resp.sendRedirect(referer);
                    return;
                }
                resp.sendRedirect(req.getContextPath() + "/cart");
                return;
                
            } else if ("/cart/remove".equals(path)) {
                Long itemId = Long.parseLong(req.getParameter("itemId"));
                ServiceFactory.getCartFacadeService().removeCartItem(itemId);
            } else if ("/cart/update".equals(path)) {
                Long itemId = Long.parseLong(req.getParameter("itemId"));
                int quantity = Integer.parseInt(req.getParameter("quantity"));
                ServiceFactory.getCartFacadeService().updateQuantity(itemId, quantity);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        resp.sendRedirect(req.getContextPath() + "/cart");
    }

    private void renderPage(HttpServletRequest req, HttpServletResponse resp, String templateName, CartDTO cart) throws IOException {
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("text/html;charset=UTF-8");

        ITemplateEngine templateEngine = ThymeleafConfig.getTemplateEngine();
        JakartaServletWebApplication application = JakartaServletWebApplication.buildApplication(getServletContext());
        IWebExchange webExchange = application.buildExchange(req, resp);
        WebContext context = new WebContext(webExchange, webExchange.getLocale());

        context.setVariable("cart", cart);

        templateEngine.process(templateName, context, resp.getWriter());
    }
}
