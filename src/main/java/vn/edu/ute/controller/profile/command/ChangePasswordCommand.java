package vn.edu.ute.controller.profile.command;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Command xử lý đổi mật khẩu cho user đang đăng nhập.
 */
public class ChangePasswordCommand extends ProfileCommand {

    @Override
    public void execute(HttpServletRequest req, HttpServletResponse resp, Long userId)
            throws ServletException, IOException {

        String currentPassword = req.getParameter("currentPassword");
        String newPassword = req.getParameter("newPassword");
        String confirmNewPassword = req.getParameter("confirmNewPassword");

        facade.changePassword(userId, currentPassword, newPassword, confirmNewPassword);

        resp.sendRedirect(req.getContextPath() + "/profile?passwordChanged=true");
    }
}