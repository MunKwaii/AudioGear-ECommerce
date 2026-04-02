package vn.edu.ute.controller.profile.command;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import vn.edu.ute.entity.Address;

import java.io.IOException;

public class AddAddressCommand extends ProfileCommand {
    @Override
    public void execute(HttpServletRequest req, HttpServletResponse resp, Long userId) throws ServletException, IOException {
        Address address = facade.extractAddressFromRequest(req);
        facade.addAddress(userId, address);
        resp.sendRedirect(req.getContextPath() + "/profile/addresses?success=true");
    }
}
