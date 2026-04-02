package vn.edu.ute.controller.profile.command;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public class UpdateProfileCommand extends ProfileCommand {
    @Override
    public void execute(HttpServletRequest req, HttpServletResponse resp, Long userId) throws ServletException, IOException {
        String fullName = req.getParameter("fullName");
        String phoneNumber = req.getParameter("phoneNumber");
        
        facade.updateProfile(userId, fullName, phoneNumber);
        
        resp.sendRedirect(req.getContextPath() + "/profile?success=true");
    }
}
