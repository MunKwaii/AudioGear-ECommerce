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

import vn.edu.ute.dto.HomePageDTO;
import vn.edu.ute.factory.ServiceFactory;
import vn.edu.ute.service.HomeFacadeService;

import java.io.IOException;

@WebServlet(name = "HomeController", urlPatterns = {"", "/home"})
public class HomeController extends HttpServlet {

    private HomeFacadeService homeFacadeService;
    private ITemplateEngine templateEngine;
    private JakartaServletWebApplication application;

    @Override
    public void init() throws ServletException {
        // Lấy service qua Factory
        this.homeFacadeService = ServiceFactory.getHomeFacadeService();
        // Lấy Thymeleaf Template Engine từ Servlet Context
        this.templateEngine = (ITemplateEngine) getServletContext().getAttribute("templateEngine");
        this.application = JakartaServletWebApplication.buildApplication(getServletContext());
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");

        // Design Pattern: Facade - gom nhóm nhiều request lấy cục data trang chủ (Featured, Newest, Category)
        HomePageDTO homeData = homeFacadeService.getHomePageData();

        // Chuẩn bị context Thymeleaf
        IWebExchange webExchange = application.buildExchange(request, response);
        WebContext context = new WebContext(webExchange, webExchange.getLocale());
        
        // Truyền dữ liệu sang View model
        context.setVariable("featuredProducts", homeData.getFeaturedProducts());
        context.setVariable("newProducts", homeData.getNewProducts());
        context.setVariable("categories", homeData.getCategories());

        // Process view "home.html"
        templateEngine.process("home", context, response.getWriter());
    }
}
