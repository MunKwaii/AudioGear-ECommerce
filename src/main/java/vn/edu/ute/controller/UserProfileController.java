package vn.edu.ute.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.web.IWebExchange;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;
import vn.edu.ute.config.ThymeleafConfig;
import vn.edu.ute.entity.Address;
import vn.edu.ute.entity.Order;
import vn.edu.ute.entity.User;
import vn.edu.ute.service.AddressService;
import vn.edu.ute.service.OrderService;
import vn.edu.ute.service.UserService;
import vn.edu.ute.service.impl.AddressServiceImpl;
import vn.edu.ute.service.impl.OrderServiceImpl;
import vn.edu.ute.service.impl.UserServiceImpl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@WebServlet({ "/profile", "/profile/update", "/profile/avatar", "/profile/addresses", "/profile/addresses/add",
        "/profile/addresses/edit", "/profile/addresses/delete", "/profile/addresses/default", "/profile/orders" })
@MultipartConfig
public class UserProfileController extends HttpServlet {

    private UserService userService;
    private AddressService addressService;
    private OrderService orderService;
    private ITemplateEngine templateEngine;
    private JakartaServletWebApplication application;

    @Override
    public void init() throws ServletException {
        this.userService = new UserServiceImpl();
        this.addressService = new AddressServiceImpl();
        this.orderService = new OrderServiceImpl();
        this.templateEngine = ThymeleafConfig.getTemplateEngine();
        this.application = JakartaServletWebApplication.buildApplication(getServletContext());
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getServletPath();
        Long userId = (Long) req.getAttribute("currentUserId");

        if (userId == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        try {
            if ("/profile".equals(path)) {
                showProfilePage(req, resp, userId);
            } else if ("/profile/addresses".equals(path)) {
                showAddressesPage(req, resp, userId);
            } else if ("/profile/orders".equals(path)) {
                showOrdersPage(req, resp, userId);
            } else {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (Exception e) {
            req.setAttribute("errorMessage", e.getMessage());
            showProfilePage(req, resp, userId);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getServletPath();
        Long userId = (Long) req.getAttribute("currentUserId");

        if (userId == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        try {
            if ("/profile/update".equals(path)) {
                String fullName = req.getParameter("fullName");
                String phoneNumber = req.getParameter("phoneNumber");
                userService.updateUserProfile(userId, fullName, phoneNumber);
                resp.sendRedirect(req.getContextPath() + "/profile?success=true");

            } else if ("/profile/addresses/add".equals(path)) {
                Address address = extractAddressFromRequest(req);
                addressService.addAddress(userId, address);
                resp.sendRedirect(req.getContextPath() + "/profile/addresses?success=true");

            } else if ("/profile/addresses/edit".equals(path)) {
                Address address = extractAddressFromRequest(req);
                if (req.getParameter("id") != null) {
                    address.setId(Long.parseLong(req.getParameter("id")));
                    addressService.updateAddress(userId, address);
                }
                resp.sendRedirect(req.getContextPath() + "/profile/addresses?success=true");

            } else if ("/profile/addresses/delete".equals(path)) {
                Long addressId = Long.parseLong(req.getParameter("id"));
                addressService.deleteAddress(addressId);
                resp.sendRedirect(req.getContextPath() + "/profile/addresses?success=true");

            } else if ("/profile/addresses/default".equals(path)) {
                Long addressId = Long.parseLong(req.getParameter("id"));
                addressService.setDefaultAddress(userId, addressId);
                resp.sendRedirect(req.getContextPath() + "/profile/addresses?success=true");

            } else if ("/profile/avatar".equals(path)) {
                handleAvatarUpload(req, resp, userId);
            } else {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (Exception e) {
            req.setAttribute("errorMessage", e.getMessage());
            if (path.startsWith("/profile/addresses")) {
                showAddressesPage(req, resp, userId);
            } else {
                showProfilePage(req, resp, userId);
            }
        }
    }

    private void showProfilePage(HttpServletRequest req, HttpServletResponse resp, Long userId) throws IOException {
        resp.setContentType("text/html;charset=UTF-8");
        User user = userService.getUserById(userId);

        IWebExchange webExchange = application.buildExchange(req, resp);
        WebContext context = new WebContext(webExchange, webExchange.getLocale());
        context.setVariable("user", user);

        if ("true".equals(req.getParameter("success"))) {
            context.setVariable("successMessage", "Cập nhật hồ sơ thành công!");
        } else if ("AvatarUpdated".equals(req.getParameter("success"))) {
            context.setVariable("successMessage", "Cập nhật ảnh đại diện thành công!");
        }
        if (req.getAttribute("errorMessage") != null) {
            context.setVariable("errorMessage", req.getAttribute("errorMessage"));
        }

        templateEngine.process("profile", context, resp.getWriter());
    }

    private void showAddressesPage(HttpServletRequest req, HttpServletResponse resp, Long userId) throws IOException {
        resp.setContentType("text/html;charset=UTF-8");
        List<Address> addresses = addressService.getAddressesByUserId(userId);
        User user = userService.getUserById(userId);

        IWebExchange webExchange = application.buildExchange(req, resp);
        WebContext context = new WebContext(webExchange, webExchange.getLocale());
        context.setVariable("addresses", addresses);
        context.setVariable("user", user);
        context.setVariable("activePage", "addresses");

        if (req.getParameter("success") != null) {
            context.setVariable("successMessage", "Cập nhật sổ địa chỉ thành công!");
        }
        if (req.getAttribute("errorMessage") != null) {
            context.setVariable("errorMessage", req.getAttribute("errorMessage"));
        }

        templateEngine.process("addresses", context, resp.getWriter());
    }

    private void showOrdersPage(HttpServletRequest req, HttpServletResponse resp, Long userId) throws IOException {
        resp.setContentType("text/html;charset=UTF-8");
        List<Order> orders = orderService.getOrdersByUserId(userId);
        User user = userService.getUserById(userId);

        IWebExchange webExchange = application.buildExchange(req, resp);
        WebContext context = new WebContext(webExchange, webExchange.getLocale());
        context.setVariable("orders", orders);
        context.setVariable("user", user);
        context.setVariable("activePage", "orders");

        templateEngine.process("orders", context, resp.getWriter());
    }

    private void handleAvatarUpload(HttpServletRequest req, HttpServletResponse resp, Long userId)
            throws ServletException, IOException {
        Part filePart = req.getPart("avatar");
        if (filePart == null || filePart.getSize() == 0) {
            resp.sendRedirect(req.getContextPath() + "/profile?error=NoFile");
            return;
        }

        String fileName = Paths.get(filePart.getSubmittedFileName()).getFileName().toString();
        String extension = fileName.substring(fileName.lastIndexOf("."));
        String newFileName = "avatar_" + userId + "_" + UUID.randomUUID() + extension;

        String rootPath = "/home/okarin/Documents/Arch_Programming/AudioGear-ECommerce";
        String srcPath = rootPath + File.separator + "src/main/webapp/static/images/avatars";
        String deployPath = getServletContext().getRealPath("/static/images/avatars");

        // Ensure both directories exist
        File srcDir = new File(srcPath);
        if (!srcDir.exists())
            srcDir.mkdirs();

        File deployDir = new File(deployPath);
        if (deployPath != null && !deployDir.exists())
            deployDir.mkdirs();

        // 1. Save to src directory (for persistence)
        File srcFile = new File(srcDir, newFileName);
        try (java.io.InputStream input = filePart.getInputStream()) {
            Files.copy(input, srcFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }

        // 2. Also save to deploy directory (for immediate display)
        if (deployPath != null && !srcPath.equals(deployPath)) {
            File deployFile = new File(deployDir, newFileName);
            Files.copy(srcFile.toPath(), deployFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }

        String avatarUrl = "/static/images/avatars/" + newFileName;
        userService.updateAvatar(userId, avatarUrl);

        resp.sendRedirect(req.getContextPath() + "/profile?success=AvatarUpdated");
    }

    private Address extractAddressFromRequest(HttpServletRequest req) {
        Address address = new Address();
        address.setRecipientName(req.getParameter("recipientName"));
        address.setPhoneNumber(req.getParameter("phoneNumber"));
        address.setStreetAddress(req.getParameter("streetAddress"));
        address.setProvince(req.getParameter("province"));
        address.setProvinceCode(req.getParameter("provinceCode"));
        address.setDistrict(req.getParameter("district"));
        address.setDistrictCode(req.getParameter("districtCode"));
        address.setWard(req.getParameter("ward"));
        address.setWardCode(req.getParameter("wardCode"));
        address.setIsDefault("on".equals(req.getParameter("isDefault")));
        return address;
    }
}
