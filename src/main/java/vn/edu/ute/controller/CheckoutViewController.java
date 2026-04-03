package vn.edu.ute.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.web.IWebExchange;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;
import vn.edu.ute.cart.GuestCartService;
import vn.edu.ute.config.ThymeleafConfig;
import vn.edu.ute.dto.CartDTO;
import vn.edu.ute.homepage.factory.ServiceFactory;

import java.io.IOException;

@WebServlet("/checkout")
public class CheckoutViewController extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/html;charset=UTF-8");
        resp.setCharacterEncoding("UTF-8");

        Long userId = (Long) req.getAttribute("currentUserId");
        CartDTO cart;

        if (userId != null) {
            cart = ServiceFactory.getCartFacadeService().getCartDetails(userId);
        } else {
            cart = GuestCartService.getInstance().getCart(req);
        }

        if (cart == null || cart.getItems() == null || cart.getItems().isEmpty()) {
            resp.sendRedirect(req.getContextPath() + "/cart");
            return;
        }

        ITemplateEngine templateEngine = ThymeleafConfig.getTemplateEngine();
        JakartaServletWebApplication application = JakartaServletWebApplication.buildApplication(getServletContext());
        IWebExchange webExchange = application.buildExchange(req, resp);
        WebContext context = new WebContext(webExchange, webExchange.getLocale());

        context.setVariable("cart", cart);
        context.setVariable("pageTitle", "Xác nhận thanh toán");
        context.setVariable("isGuest", userId == null);

        templateEngine.process("checkout", context, resp.getWriter());
    }
}
