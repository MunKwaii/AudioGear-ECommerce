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
import vn.edu.ute.util.JsonUtil;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@WebServlet("/admin/products/edit")
public class AdminProductEditController extends HttpServlet {

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

        CreateProductRequest form = mapToForm(productOpt.get());
        String formAction = req.getContextPath() + "/admin/products/edit?id=" + id;
        renderPage(req, resp, form, null, null, formAction);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Long id = parseLong(req.getParameter("id"));
        if (id == null) {
            resp.sendRedirect(req.getContextPath() + "/admin/products");
            return;
        }

        CreateProductRequest form = new CreateProductRequest();
        form.setName(req.getParameter("name"));
        form.setDescription(req.getParameter("description"));
        form.setPrice(req.getParameter("price"));
        form.setStockQuantity(req.getParameter("stockQuantity"));
        form.setThumbnailUrl(req.getParameter("thumbnailUrl"));
        form.setSpecifications(req.getParameter("specifications"));
        form.setImageUrls(req.getParameter("imageUrls"));
        form.setCategoryId(req.getParameter("categoryId"));
        form.setBrandId(req.getParameter("brandId"));
        form.setStatus(req.getParameter("status"));

        String formAction = req.getContextPath() + "/admin/products/edit?id=" + id;
        try {
            Product product = productService.updateProduct(id, form);
            renderPage(req, resp, mapToForm(product), "Cập nhật sản phẩm thành công: " + product.getName(), null, formAction);
        } catch (IllegalArgumentException ex) {
            renderPage(req, resp, form, null, ex.getMessage(), formAction);
        } catch (Exception ex) {
            renderPage(req, resp, form, null, "Lỗi máy chủ: " + ex.getMessage(), formAction);
        }
    }

    private void renderPage(HttpServletRequest req, HttpServletResponse resp,
                            CreateProductRequest form, String successMessage, String errorMessage, String formAction)
            throws IOException {

        resp.setContentType("text/html;charset=UTF-8");

        List<Category> categories = productService.getAllCategories();
        List<Brand> brands = productService.getAllBrands();

        TemplateEngine engine = (TemplateEngine) getServletContext().getAttribute("templateEngine");

        JakartaServletWebApplication webApp =
                JakartaServletWebApplication.buildApplication(getServletContext());
        WebContext ctx = new WebContext(webApp.buildExchange(req, resp), resp.getLocale());

        ctx.setVariable("title", "Chỉnh sửa sản phẩm");
        ctx.setVariable("activePage", "products");
        ctx.setVariable("categories", categories);
        ctx.setVariable("brands", brands);
        ctx.setVariable("form", form);
        ctx.setVariable("successMessage", successMessage);
        ctx.setVariable("errorMessage", errorMessage);
        ctx.setVariable("formAction", formAction);

        engine.process("admin/admin-product-create", ctx, resp.getWriter());
    }

    private CreateProductRequest mapToForm(Product product) {
        CreateProductRequest form = new CreateProductRequest();
        form.setName(product.getName());
        form.setDescription(product.getDescription());
        form.setPrice(product.getPrice() == null ? "" : product.getPrice().toString());
        form.setStockQuantity(product.getStockQuantity() == null ? "" : product.getStockQuantity().toString());
        form.setThumbnailUrl(product.getThumbnailUrl());
        form.setSpecifications(product.getSpecifications());
        if (product.getCategory() != null) {
            form.setCategoryId(product.getCategory().getId().toString());
        }
        if (product.getBrand() != null) {
            form.setBrandId(product.getBrand().getId().toString());
        }
        if (product.getStatus() != null) {
            form.setStatus(product.getStatus().toString());
        }

        if (product.getImages() != null && !product.getImages().isEmpty()) {
            List<String> imageUrls = product.getImages().stream()
                    .map(image -> image.getImageUrl())
                    .distinct()
                    .collect(Collectors.toList());
            if (product.getThumbnailUrl() != null && !product.getThumbnailUrl().isBlank()
                    && !imageUrls.contains(product.getThumbnailUrl())) {
                imageUrls.add(0, product.getThumbnailUrl());
            }
            form.setImageUrls(JsonUtil.toJson(imageUrls));
        } else if (product.getThumbnailUrl() != null && !product.getThumbnailUrl().isBlank()) {
            form.setImageUrls(JsonUtil.toJson(List.of(product.getThumbnailUrl())));
        }

        return form;
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
