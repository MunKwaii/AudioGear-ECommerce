package vn.edu.ute.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;
import vn.edu.ute.config.ThymeleafConfig;
import vn.edu.ute.controller.order.command.OrderCommand;
import vn.edu.ute.controller.order.command.OrderCommandFactory;
import vn.edu.ute.controller.order.facade.OrderFacade;
import vn.edu.ute.controller.order.facade.impl.OrderFacadeImpl;

import java.io.IOException;

@WebServlet({ "/order/track" })
public class OrderTrackingController extends HttpServlet {
    private OrderFacade orderFacade;
    private ITemplateEngine templateEngine;
    private JakartaServletWebApplication application;

    @Override
    public void init() throws ServletException {
        this.orderFacade = new OrderFacadeImpl(getServletContext());
        this.templateEngine = ThymeleafConfig.getTemplateEngine();
        this.application = JakartaServletWebApplication.buildApplication(getServletContext());
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        processRequest(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        processRequest(req, resp);
    }

    private void processRequest(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getServletPath();
        String method = req.getMethod();

        OrderCommand command = OrderCommandFactory.getCommand(method, path);

        if (command != null) {
            command.init(orderFacade, templateEngine, application);
            command.execute(req, resp);
        } else {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }
}
