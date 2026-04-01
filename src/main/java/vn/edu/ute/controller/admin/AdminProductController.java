package vn.edu.ute.controller.admin;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;
import vn.edu.ute.dto.request.CreateProductRequest;
import vn.edu.ute.entity.Brand;
import vn.edu.ute.entity.Category;
import vn.edu.ute.entity.Product;
import vn.edu.ute.service.ProductService;
import vn.edu.ute.service.impl.ProductServiceImpl;

import java.io.IOException;
import java.util.List;

@WebServlet("/admin/products/new")
public class AdminProductController extends HttpServlet {

    private final ProductService productService = new ProductServiceImpl();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        renderPage(req, resp, new CreateProductRequest(), null, null);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        CreateProductRequest form = new CreateProductRequest();
        form.setName(req.getParameter("name"));
        form.setDescription(req.getParameter("description"));
        form.setPrice(req.getParameter("price"));
        form.setStockQuantity(req.getParameter("stockQuantity"));
        form.setThumbnailUrl(req.getParameter("thumbnailUrl"));
        form.setSpecifications(req.getParameter("specifications"));
        form.setCategoryId(req.getParameter("categoryId"));
        form.setBrandId(req.getParameter("brandId"));
        form.setStatus(req.getParameter("status"));

        try {
            Product product = productService.createProduct(form);
            renderPage(req, resp, new CreateProductRequest(), "Tạo sản phẩm thành công: " + product.getName(), null);
        } catch (IllegalArgumentException ex) {
            renderPage(req, resp, form, null, ex.getMessage());
        } catch (Exception ex) {
            renderPage(req, resp, form, null, "Lỗi máy chủ: " + ex.getMessage());
        }
    }

    private void renderPage(HttpServletRequest req, HttpServletResponse resp,
                            CreateProductRequest form, String successMessage, String errorMessage)
            throws IOException {

        resp.setContentType("text/html;charset=UTF-8");

        List<Category> categories = productService.getAllCategories();
        List<Brand> brands = productService.getAllBrands();

        TemplateEngine engine = (TemplateEngine) getServletContext().getAttribute("templateEngine");

        JakartaServletWebApplication webApp =
                JakartaServletWebApplication.buildApplication(getServletContext());
        WebContext ctx = new WebContext(webApp.buildExchange(req, resp), resp.getLocale());

        ctx.setVariable("title", "Thêm sản phẩm mới");
        ctx.setVariable("activePage", "products");
        ctx.setVariable("categories", categories);
        ctx.setVariable("brands", brands);
        ctx.setVariable("form", form);
        ctx.setVariable("successMessage", successMessage);
        ctx.setVariable("errorMessage", errorMessage);

        engine.process("admin/admin-product-create", ctx, resp.getWriter());
    }
}
