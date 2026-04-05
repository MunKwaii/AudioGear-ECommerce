package vn.edu.ute.controller.order.command;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.web.IWebExchange;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;
import vn.edu.ute.controller.order.facade.OrderFacade;

import java.io.IOException;

public abstract class OrderCommand {
    protected OrderFacade facade;
    protected ITemplateEngine templateEngine;
    protected JakartaServletWebApplication application;

    public void init(OrderFacade facade, ITemplateEngine templateEngine, JakartaServletWebApplication application) {
        this.facade = facade;
        this.templateEngine = templateEngine;
        this.application = application;
    }

    public abstract void execute(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException;

    protected void render(HttpServletRequest req, HttpServletResponse resp, String template, WebContext context) throws IOException {
        templateEngine.process(template, context, resp.getWriter());
    }

    protected WebContext createWebContext(HttpServletRequest req, HttpServletResponse resp) {
        IWebExchange webExchange = application.buildExchange(req, resp);
        return new WebContext(webExchange, webExchange.getLocale());
    }
}
