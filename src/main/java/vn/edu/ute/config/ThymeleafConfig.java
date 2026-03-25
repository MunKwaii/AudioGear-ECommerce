package vn.edu.ute.config;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.WebApplicationTemplateResolver;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;

/**
 * Lắng nghe sự kiện khởi động Server để cấu hình Thymeleaf TemplateEngine
 */
@WebListener
public class ThymeleafConfig implements ServletContextListener {

    private static TemplateEngine templateEngine;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        JakartaServletWebApplication application = JakartaServletWebApplication.buildApplication(sce.getServletContext());
        
        WebApplicationTemplateResolver templateResolver = new WebApplicationTemplateResolver(application);
        // Đặt thư mục chứa file HTML (Ví dụ: src/main/webapp/WEB-INF/templates/)
        templateResolver.setPrefix("/WEB-INF/templates/");
        templateResolver.setSuffix(".html");
        templateResolver.setTemplateMode(TemplateMode.HTML);
        templateResolver.setCharacterEncoding("UTF-8");
        templateResolver.setCacheable(false); // Đặt false khi dev để thấy thay đổi ngay lập tức

        templateEngine = new TemplateEngine();
        templateEngine.setTemplateResolver(templateResolver);

        // Lưu TemplateEngine vào Context để các Controller gọi ra dùng
        sce.getServletContext().setAttribute("templateEngine", templateEngine);
        System.out.println("Thymeleaf TemplateEngine initialized.");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // Cleanup if needed
    }

    public static TemplateEngine getTemplateEngine() {
        return templateEngine;
    }
}
