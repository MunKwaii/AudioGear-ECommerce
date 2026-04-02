package vn.edu.ute.controller.profile.command;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

import java.io.IOException;

public class UploadAvatarCommand extends ProfileCommand {
    @Override
    public void execute(HttpServletRequest req, HttpServletResponse resp, Long userId) throws ServletException, IOException {
        Part filePart = req.getPart("avatar");
        if (filePart == null || filePart.getSize() == 0) {
            resp.sendRedirect(req.getContextPath() + "/profile?error=NoFile");
            return;
        }

        facade.updateAvatar(userId, filePart);
        
        resp.sendRedirect(req.getContextPath() + "/profile?success=AvatarUpdated");
    }
}
