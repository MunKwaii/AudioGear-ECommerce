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
import vn.edu.ute.config.ThymeleafConfig;
import vn.edu.ute.dao.OrderDao;
import vn.edu.ute.dao.impl.OrderDaoImpl;
import vn.edu.ute.entity.Order;

import java.io.IOException;
import java.util.Optional;

@WebServlet("/payment")
public class PaymentViewController extends HttpServlet {

    private final OrderDao orderDao = new OrderDaoImpl();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/html;charset=UTF-8");
        resp.setCharacterEncoding("UTF-8");

        String orderCode = req.getParameter("orderCode");
        
        if (orderCode == null || orderCode.isEmpty()) {
            resp.sendRedirect(req.getContextPath() + "/");
            return;
        }

        Optional<Order> orderOpt = orderDao.findByOrderCode(orderCode);
        
        if (orderOpt.isEmpty()) {
            resp.sendRedirect(req.getContextPath() + "/");
            return;
        }
        
        Order order = orderOpt.get();
        // Check if correct payment method
        if (!"SEPAY_QR".equalsIgnoreCase(order.getPaymentStrategy().getStrategyCode())) {
            resp.sendRedirect(req.getContextPath() + "/?orderCode=" + orderCode + "&status=success");
            return;
        }

        ITemplateEngine templateEngine = ThymeleafConfig.getTemplateEngine();
        JakartaServletWebApplication application = JakartaServletWebApplication.buildApplication(getServletContext());
        IWebExchange webExchange = application.buildExchange(req, resp);
        WebContext context = new WebContext(webExchange, webExchange.getLocale());

        context.setVariable("order", order);
        context.setVariable("pageTitle", "Thanh toán QR");
        
        // Use token logic similar to src_4
        context.setVariable("bankAccount", "0372008321"); // Placeholder from src_4
        context.setVariable("bankCode", "MBBank");

        templateEngine.process("payment", context, resp.getWriter());
    }
}
