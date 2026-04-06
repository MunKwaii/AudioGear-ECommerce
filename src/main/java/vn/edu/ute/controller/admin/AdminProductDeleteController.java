package vn.edu.ute.controller.admin;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;
import vn.edu.ute.entity.Product;
import vn.edu.ute.service.ProductService;
import vn.edu.ute.service.impl.ProductServiceImpl;
import vn.edu.ute.util.FlashMessage;

import java.io.IOException;
import java.util.Optional;

@WebServlet("/admin/products/delete")
public class AdminProductDeleteController extends HttpServlet {

    private final ProductService productService = new ProductServiceImpl();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Long id = parseLong(req.getParameter("id"));
        if (id == null) {
            resp.sendRedirect(req.getContextPath() + "/admin/products");
            return;
        }

        Optional<Product> productOpt = productService.getProductById(id);
        if (productOpt.isEmpty()) {
            resp.sendRedirect(req.getContextPath() + "/admin/products");
            return;
        }

        resp.setContentType("text/html;charset=UTF-8");
        TemplateEngine engine = (TemplateEngine) getServletContext().getAttribute("templateEngine");
        JakartaServletWebApplication webApp = JakartaServletWebApplication.buildApplication(getServletContext());
        WebContext ctx = new WebContext(webApp.buildExchange(req, resp), resp.getLocale());

        ctx.setVariable("title", "Xóa sản phẩm");
        ctx.setVariable("activePage", "products");
        ctx.setVariable("product", productOpt.get());

        engine.process("admin/admin-product-delete", ctx, resp.getWriter());
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Long id = parseLong(req.getParameter("id"));
        if (id != null) {
            try {
                productService.deleteProduct(id);
                req.getSession().setAttribute("flashMessage", FlashMessage.success("Đã xóa sản phẩm thành công!"));
            } catch (Exception e) {
                req.getSession().setAttribute("flashMessage", FlashMessage.error("Không thể xóa sản phẩm: " + e.getMessage()));
            }
        }
        resp.sendRedirect(req.getContextPath() + "/admin/products");
    }

    private Long parseLong(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
