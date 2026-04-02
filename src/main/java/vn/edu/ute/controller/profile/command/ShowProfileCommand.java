package vn.edu.ute.controller.profile.command;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.thymeleaf.context.WebContext;
import vn.edu.ute.entity.User;

import java.io.IOException;

public class ShowProfileCommand extends ProfileCommand {
    @Override
    public void execute(HttpServletRequest req, HttpServletResponse resp, Long userId) throws ServletException, IOException {
        resp.setContentType("text/html;charset=UTF-8");
        User user = facade.getUserDetails(userId);
        
        WebContext context = createWebContext(req, resp);
        context.setVariable("user", user);
        
        if ("true".equals(req.getParameter("success"))) {
            context.setVariable("successMessage", "Cập nhật hồ sơ thành công!");
        } else if ("AvatarUpdated".equals(req.getParameter("success"))) {
            context.setVariable("successMessage", "Cập nhật ảnh đại diện thành công!");
        }
        
        if (req.getAttribute("errorMessage") != null) {
            context.setVariable("errorMessage", req.getAttribute("errorMessage"));
        }
        
        render(req, resp, "profile", context);
    }
}
