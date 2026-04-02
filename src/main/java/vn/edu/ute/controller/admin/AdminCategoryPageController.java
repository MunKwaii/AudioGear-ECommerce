package vn.edu.ute.controller.admin;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;
import vn.edu.ute.dto.CategoryDTO;
import vn.edu.ute.service.CategoryService;
import vn.edu.ute.service.impl.CategoryServiceImpl;

import java.io.IOException;
import java.util.List;

@WebServlet("/admin/categories")
public class AdminCategoryPageController extends HttpServlet {

    private final CategoryService categoryService = new CategoryServiceImpl();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("text/html;charset=UTF-8");

        List<CategoryDTO> tree = categoryService.getAllCategoriesAsTree();
        List<CategoryDTO> flat = categoryService.getAllCategoriesFlat();

        TemplateEngine engine = (TemplateEngine) getServletContext().getAttribute("templateEngine");
        JakartaServletWebApplication webApp =
                JakartaServletWebApplication.buildApplication(getServletContext());
        WebContext ctx = new WebContext(webApp.buildExchange(req, resp), resp.getLocale());

        ctx.setVariable("title", "Quản lý Danh mục");
        ctx.setVariable("activePage", "categories");
        ctx.setVariable("categories", tree);
        ctx.setVariable("flatCategories", flat);

        engine.process("admin/admin-categories", ctx, resp.getWriter());
    }
}
