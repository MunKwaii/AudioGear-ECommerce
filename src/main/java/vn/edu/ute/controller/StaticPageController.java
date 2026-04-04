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

@WebServlet(name = "StaticPageController", urlPatterns = {"/warranty", "/shopping-guide"})
public class StaticPageController extends HttpServlet {

    private ITemplateEngine templateEngine;
    private JakartaServletWebApplication application;

    @Override
    public void init() throws ServletException {
        // Lấy Thymeleaf Template Engine từ Servlet Context
        this.templateEngine = (ITemplateEngine) getServletContext().getAttribute("templateEngine");
        this.application = JakartaServletWebApplication.buildApplication(getServletContext());
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");

        String path = request.getServletPath();
        String viewName;
        
        if ("/warranty".equals(path)) {
            viewName = "warranty";
        } else {
            viewName = "shopping-guide";
        }

        IWebExchange webExchange = application.buildExchange(request, response);
        WebContext context = new WebContext(webExchange, webExchange.getLocale());
        
        // Truyền các biến cần thiết nếu có (ví dụ breadcrumbs, title)
        context.setVariable("pageTitle", "/warranty".equals(path) ? "Chính sách bảo hành" : "Hướng dẫn mua hàng");

        templateEngine.process(viewName, context, response.getWriter());
    }
}
