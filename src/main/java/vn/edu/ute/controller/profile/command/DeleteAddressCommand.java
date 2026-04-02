package vn.edu.ute.controller.profile.command;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public class DeleteAddressCommand extends ProfileCommand {
    @Override
    public void execute(HttpServletRequest req, HttpServletResponse resp, Long userId) throws ServletException, IOException {
        String idStr = req.getParameter("id");
        if (idStr != null) {
            facade.deleteAddress(Long.parseLong(idStr));
        }
        resp.sendRedirect(req.getContextPath() + "/profile/addresses?success=true");
    }
}
