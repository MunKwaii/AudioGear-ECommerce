package vn.edu.ute.controller.admin;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;
import vn.edu.ute.dto.ProductDTO;
import vn.edu.ute.entity.Category;
import vn.edu.ute.entity.Inventory;
import vn.edu.ute.entity.Product;
import vn.edu.ute.homepage.factory.DaoFactory;
import vn.edu.ute.service.ProductService;
import vn.edu.ute.service.impl.ProductServiceImpl;

import java.io.IOException;
import java.util.List;

@WebServlet("/admin/products")
public class AdminProductListController extends HttpServlet {

    private final ProductService productService = new ProductServiceImpl();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String keyword = trimOrNull(req.getParameter("q"));
        Long categoryId = parseLong(req.getParameter("categoryId"));
        Boolean status = parseStatus(req.getParameter("status"));
        int page = parseInt(req.getParameter("page"), 1);
        int size = parseInt(req.getParameter("size"), 10);
        if (page < 1) {
            page = 1;
        }
        if (size < 5) {
            size = 5;
        }

        int offset = (page - 1) * size;
        List<Product> productEntities = productService.searchProductsForAdmin(keyword, categoryId, status, offset, size);
        
        List<ProductDTO> products = productEntities.stream().map(p -> {
            Integer stock = DaoFactory.getInventoryDao().findByProductId(p.getId()).map(Inventory::getStockQuantity).orElse(0);
            return new ProductDTO(
                p.getId(),
                p.getName(),
                p.getPrice(),
                p.getThumbnailUrl(),
                p.getCategory() != null ? p.getCategory().getName() : "-",
                p.getBrand() != null ? p.getBrand().getName() : "-",
                stock,
                p.getStatus()
            );
        }).toList();

        long totalItems = productService.countSearchProductsForAdmin(keyword, categoryId, status);
        int totalPages = (int) Math.ceil((double) totalItems / size);

        List<Category> categories = productService.getAllCategories();

        resp.setContentType("text/html;charset=UTF-8");
        TemplateEngine engine = (TemplateEngine) getServletContext().getAttribute("templateEngine");
        JakartaServletWebApplication webApp = JakartaServletWebApplication.buildApplication(getServletContext());
        WebContext ctx = new WebContext(webApp.buildExchange(req, resp), resp.getLocale());

        ctx.setVariable("title", "Danh sách sản phẩm");
        ctx.setVariable("activePage", "products");
        ctx.setVariable("products", products);
        ctx.setVariable("categories", categories);
        ctx.setVariable("keyword", keyword == null ? "" : keyword);
        ctx.setVariable("selectedCategoryId", categoryId == null ? "" : categoryId.toString());
        ctx.setVariable("statusFilter", status == null ? "all" : status.toString());
        ctx.setVariable("page", page);
        ctx.setVariable("size", size);
        ctx.setVariable("totalPages", totalPages);
        ctx.setVariable("totalItems", totalItems);

        engine.process("admin/admin-products", ctx, resp.getWriter());
    }

    private String trimOrNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
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

    private int parseInt(String value, int defaultValue) {
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }

    private Boolean parseStatus(String value) {
        if (value == null || value.trim().isEmpty() || "all".equalsIgnoreCase(value)) {
            return null;
        }
        return Boolean.parseBoolean(value.trim());
    }
}
