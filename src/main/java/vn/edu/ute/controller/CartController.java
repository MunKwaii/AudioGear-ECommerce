package vn.edu.ute.controller;

import vn.edu.ute.cart.GuestCartService;
import vn.edu.ute.dto.CartDTO;
import vn.edu.ute.homepage.factory.ServiceFactory;
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

@WebServlet(urlPatterns = {"/cart", "/cart/add", "/cart/remove", "/cart/update", "/cart/count"})
public class CartController extends HttpServlet {

    private Long getUserIdFromCookie(HttpServletRequest req) {
        if (req.getCookies() == null) return null;
        return java.util.Arrays.stream(req.getCookies())
                .filter(c -> "accessToken".equals(c.getName()))
                .map(Cookie::getValue)
                .filter(token -> {
                    JwtUtil jwt = new JwtUtil();
                    return jwt.validateToken(token);
                })
                .map(token -> {
                    JwtUtil jwt = new JwtUtil();
                    return jwt.getUserIdFromToken(token);
                })
                .findFirst()
                .orElse(null);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getServletPath();
        Long userId = getUserIdFromCookie(req);

        if ("/cart".equals(path)) {
            CartDTO cart = (userId != null)
                    ? ServiceFactory.getCartFacadeService().getCartDetails(userId)
                    : GuestCartService.getInstance().getCart(req);
            renderPage(req, resp, "cart", cart);
        } else if ("/cart/count".equals(path)) {
            CartDTO cart = (userId != null)
                    ? ServiceFactory.getCartFacadeService().getCartDetails(userId)
                    : GuestCartService.getInstance().getCart(req);
            int count = (cart != null && cart.getItems() != null) ? cart.getItems().size() : 0;
            resp.setContentType("application/json");
            resp.getWriter().write("{\"count\":" + count + "}");
        } else {
            resp.sendRedirect(req.getContextPath() + "/cart");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getServletPath();
        Long userId = getUserIdFromCookie(req);

        try {
            if ("/cart/add".equals(path)) {
                Long productId = Long.parseLong(req.getParameter("productId"));
                int quantity = 1;
                if (req.getParameter("quantity") != null) {
                    quantity = Integer.parseInt(req.getParameter("quantity"));
                }

                if (userId != null) {
                    ServiceFactory.getCartFacadeService().addToCart(userId, productId, quantity);
                } else {
                    GuestCartService.getInstance().addToCart(req, resp, productId, quantity);
                }

                String requestedWith = req.getHeader("X-Requested-With");
                if ("XMLHttpRequest".equals(requestedWith) || (req.getHeader("Accept") != null && req.getHeader("Accept").contains("application/json"))) {
                    resp.setStatus(HttpServletResponse.SC_OK);
                    resp.getWriter().write("{\"success\":true}");
                    return;
                }

                String referer = req.getHeader("Referer");
                if (referer != null && !referer.contains("/cart")) {
                    resp.sendRedirect(referer);
                    return;
                }
                resp.sendRedirect(req.getContextPath() + "/cart");
                return;

            } else if ("/cart/remove".equals(path)) {
                if (userId != null) {
                    Long itemId = Long.parseLong(req.getParameter("itemId"));
                    ServiceFactory.getCartFacadeService().removeCartItem(itemId);
                } else {
                    Long productId = Long.parseLong(req.getParameter("productId"));
                    GuestCartService.getInstance().removeCartItem(req, resp, productId);
                }
            } else if ("/cart/update".equals(path)) {
                if (userId != null) {
                    Long itemId = Long.parseLong(req.getParameter("itemId"));
                    int quantity = Integer.parseInt(req.getParameter("quantity"));
                    ServiceFactory.getCartFacadeService().updateQuantity(itemId, quantity);
                } else {
                    Long productId = Long.parseLong(req.getParameter("productId"));
                    int quantity = Integer.parseInt(req.getParameter("quantity"));
                    GuestCartService.getInstance().updateQuantity(req, resp, productId, quantity);
                }
            }
        } catch (vn.edu.ute.exception.InsufficientStockException e) {
            String requestedWith = req.getHeader("X-Requested-With");
            if ("XMLHttpRequest".equals(requestedWith) || (req.getHeader("Accept") != null && req.getHeader("Accept").contains("application/json"))) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.setContentType("application/json;charset=UTF-8");
                resp.getWriter().write("{\"success\":false, \"message\":\"" + e.getMessage() + "\"}");
                return;
            }
            req.getSession().setAttribute("cartError", e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            req.getSession().setAttribute("cartError", "Có lỗi xảy ra: " + e.getMessage());
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
        context.setVariable("isGuest", getUserIdFromCookie(req) == null);

        templateEngine.process(templateName, context, resp.getWriter());
    }
}
