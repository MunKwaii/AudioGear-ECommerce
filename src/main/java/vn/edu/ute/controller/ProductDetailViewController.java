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

import java.io.IOException;

@WebServlet(name = "ProductDetailViewController", urlPatterns = {"/product/*"})
public class ProductDetailViewController extends HttpServlet {

    private ITemplateEngine templateEngine;
    private JakartaServletWebApplication application;

    @Override
    public void init() throws ServletException {
        this.templateEngine = (ITemplateEngine) getServletContext().getAttribute("templateEngine");
        this.application = JakartaServletWebApplication.buildApplication(getServletContext());
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");

        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Không tìm thấy sản phẩm");
            return;
        }

        String productIdStr = pathInfo.substring(1);
        try {
            Long.parseLong(productIdStr);
            
            IWebExchange webExchange = application.buildExchange(request, response);
            WebContext context = new WebContext(webExchange, webExchange.getLocale());
            
            // Trang này chủ yếu là shell, dữ liệu chi tiết sẽ được JS tải qua AJAX
            context.setVariable("productId", productIdStr);
            context.setVariable("pageTitle", "Chi tiết sản phẩm");
            
            templateEngine.process("product-detail", context, response.getWriter());
            
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "ID sản phẩm không hợp lệ");
        }
    }
}
