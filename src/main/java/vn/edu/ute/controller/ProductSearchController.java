package vn.edu.ute.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.web.IWebExchange;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;

import vn.edu.ute.dto.PageDTO;
import vn.edu.ute.dto.ProductDTO;
import vn.edu.ute.homepage.factory.DaoFactory;
import vn.edu.ute.homepage.factory.ServiceFactory;
import vn.edu.ute.homepage.facade.ProductFacadeService;

import java.io.IOException;

@WebServlet(name = "ProductSearchController", urlPatterns = {"/products"})
public class ProductSearchController extends HttpServlet {

    private ProductFacadeService productFacadeService;
    private ITemplateEngine templateEngine;
    private JakartaServletWebApplication application;

    @Override
    public void init() throws ServletException {
        this.productFacadeService = ServiceFactory.getProductFacadeService();
        this.templateEngine = (ITemplateEngine) getServletContext().getAttribute("templateEngine");
        this.application = JakartaServletWebApplication.buildApplication(getServletContext());
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");

        String keyword = request.getParameter("q");
        String catIdStr = request.getParameter("catId");
        String pageStr = request.getParameter("page");
        String sort = request.getParameter("sort");

        Long categoryId = null;
        if (catIdStr != null && !catIdStr.trim().isEmpty()) {
            try { categoryId = Long.parseLong(catIdStr); } catch (NumberFormatException ignored) {}
        }

        int page = 1; // Default
        if (pageStr != null && !pageStr.trim().isEmpty()) {
            try { page = Integer.parseInt(pageStr); } catch (NumberFormatException ignored) {}
        }

        // Limit 12 products per page
        PageDTO<ProductDTO> productPage = productFacadeService.searchAndPaginate(keyword, categoryId, sort, page, 12);

        IWebExchange webExchange = application.buildExchange(request, response);
        WebContext context = new WebContext(webExchange, webExchange.getLocale());
        
        context.setVariable("productPage", productPage);
        context.setVariable("keyword", keyword);
        context.setVariable("categoryId", categoryId);
        context.setVariable("sort", sort);
        context.setVariable("hideSearch", true);
        // Fetch categories directly to populate the sidebar filter UI
        context.setVariable("categories", ServiceFactory.getCategoryService().getAllCategoriesAsTree());

        templateEngine.process("products", context, response.getWriter());
    }
}
