package vn.edu.ute.controller.admin;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;
import vn.edu.ute.dto.BrandDTO;
import vn.edu.ute.service.BrandService;
import vn.edu.ute.service.impl.BrandServiceImpl;

import java.io.IOException;
import java.util.List;

@WebServlet("/admin/brands")
public class AdminBrandPageController extends HttpServlet {

    private final BrandService brandService = new BrandServiceImpl();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("text/html;charset=UTF-8");

        List<BrandDTO> brands = brandService.getAllBrands();

        TemplateEngine engine = (TemplateEngine) getServletContext().getAttribute("templateEngine");
        JakartaServletWebApplication webApp =
                JakartaServletWebApplication.buildApplication(getServletContext());
        WebContext ctx = new WebContext(webApp.buildExchange(req, resp), resp.getLocale());

        ctx.setVariable("title", "Quản lý Thương hiệu");
        ctx.setVariable("activePage", "brands");
        ctx.setVariable("brands", brands);

        engine.process("admin/admin-brands", ctx, resp.getWriter());
    }
}
