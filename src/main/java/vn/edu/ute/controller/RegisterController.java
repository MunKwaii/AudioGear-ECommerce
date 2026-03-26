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
import vn.edu.ute.auth.RegisterService;
import vn.edu.ute.config.ThymeleafConfig;
import vn.edu.ute.dto.RegisterRequest;

import java.io.IOException;

@WebServlet("/register")
public class RegisterController extends HttpServlet {

    private RegisterService registerService;

    @Override
    public void init() throws ServletException {
        registerService = new RegisterService();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        renderTemplate(req, resp, null, null, null, null);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");

        String username = req.getParameter("username");
        String fullName = req.getParameter("fullName");
        String email = req.getParameter("email");
        String password = req.getParameter("password");
        String confirmPassword = req.getParameter("confirmPassword");

        RegisterRequest request = new RegisterRequest(username, fullName, email, password, confirmPassword);

        try {
            registerService.register(request);
            resp.sendRedirect(req.getContextPath() + "/login");
        } catch (Exception e) {
            renderTemplate(req, resp, e.getMessage(), username, fullName, email);
        }
    }

    private void renderTemplate(HttpServletRequest req,
                                HttpServletResponse resp,
                                String error,
                                String username,
                                String fullName,
                                String email) throws IOException {
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("text/html;charset=UTF-8");

        ITemplateEngine templateEngine = ThymeleafConfig.getTemplateEngine();
        JakartaServletWebApplication application = JakartaServletWebApplication.buildApplication(getServletContext());
        IWebExchange webExchange = application.buildExchange(req, resp);
        WebContext context = new WebContext(webExchange, webExchange.getLocale());

        context.setVariable("error", error);
        context.setVariable("username", username);
        context.setVariable("fullName", fullName);
        context.setVariable("email", email);

        templateEngine.process("register", context, resp.getWriter());
    }
}