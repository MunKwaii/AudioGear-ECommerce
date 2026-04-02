package vn.edu.ute.controller.profile.command;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.web.IWebExchange;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;
import vn.edu.ute.controller.profile.facade.UserProfileFacade;

import java.io.IOException;

/**
 * Command Pattern: Abstract base for profile-related actions.
 */
public abstract class ProfileCommand {
    
    protected UserProfileFacade facade;
    protected ITemplateEngine templateEngine;
    protected JakartaServletWebApplication application;

    public void init(UserProfileFacade facade, ITemplateEngine templateEngine, JakartaServletWebApplication application) {
        this.facade = facade;
        this.templateEngine = templateEngine;
        this.application = application;
    }

    /**
     * Executes the action.
     */
    public abstract void execute(HttpServletRequest req, HttpServletResponse resp, Long userId) throws ServletException, IOException;

    /**
     * Helper to render a Thymeleaf template.
     */
    protected void render(HttpServletRequest req, HttpServletResponse resp, String template, WebContext context) throws IOException {
        templateEngine.process(template, context, resp.getWriter());
    }

    /**
     * Helper to create a WebContext.
     */
    protected WebContext createWebContext(HttpServletRequest req, HttpServletResponse resp) {
        IWebExchange webExchange = application.buildExchange(req, resp);
        return new WebContext(webExchange, webExchange.getLocale());
    }
}
