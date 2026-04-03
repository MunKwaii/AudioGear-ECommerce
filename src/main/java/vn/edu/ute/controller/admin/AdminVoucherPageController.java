package vn.edu.ute.controller.admin;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;

import java.io.IOException;

/**
 * Servlet Controller for rendering the Admin Voucher Management Page. 
 * Inherits the standard pattern of using Thymeleaf TemplateEngine manually.
 */
@WebServlet("/admin/vouchers")
public class AdminVoucherPageController extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("text/html;charset=UTF-8");

        TemplateEngine engine = (TemplateEngine) getServletContext().getAttribute("templateEngine");
        JakartaServletWebApplication webApp = 
                JakartaServletWebApplication.buildApplication(getServletContext());
        WebContext ctx = new WebContext(webApp.buildExchange(req, resp), resp.getLocale());

        ctx.setVariable("title", "Quản lý Mã giảm giá");
        ctx.setVariable("activePage", "vouchers");
        
        // Pass essential data for initializing the page (e.g. enum values for modals)
        ctx.setVariable("voucherStatuses", vn.edu.ute.entity.enums.VoucherStatus.values());
        ctx.setVariable("discountTypes", vn.edu.ute.entity.enums.DiscountType.values());

        engine.process("admin/admin-vouchers", ctx, resp.getWriter());
    }
}
