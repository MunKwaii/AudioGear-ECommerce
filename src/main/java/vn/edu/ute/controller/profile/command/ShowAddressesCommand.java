package vn.edu.ute.controller.profile.command;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.thymeleaf.context.WebContext;
import vn.edu.ute.entity.Address;
import vn.edu.ute.entity.User;

import java.io.IOException;
import java.util.List;

public class ShowAddressesCommand extends ProfileCommand {
    @Override
    public void execute(HttpServletRequest req, HttpServletResponse resp, Long userId) throws ServletException, IOException {
        resp.setContentType("text/html;charset=UTF-8");
        List<Address> addresses = facade.getUserAddresses(userId);
        User user = facade.getUserDetails(userId);

        WebContext context = createWebContext(req, resp);
        context.setVariable("addresses", addresses);
        context.setVariable("user", user);
        context.setVariable("activePage", "addresses");

        if (req.getParameter("success") != null) {
            context.setVariable("successMessage", "Cập nhật sổ địa chỉ thành công!");
        }
        if (req.getAttribute("errorMessage") != null) {
            context.setVariable("errorMessage", req.getAttribute("errorMessage"));
        }

        render(req, resp, "addresses", context);
    }
}
