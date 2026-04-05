package vn.edu.ute.controller.order.command;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.thymeleaf.context.WebContext;
import vn.edu.ute.entity.Order;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class TrackOrderCommand extends OrderCommand {
    @Override
    public void execute(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/html;charset=UTF-8");
        WebContext context = createWebContext(req, resp);
        
        String method = req.getMethod();
        
        if ("POST".equalsIgnoreCase(method)) {
            String orderCodesInput = req.getParameter("orderCodes");
            if (orderCodesInput != null && !orderCodesInput.trim().isEmpty()) {
                // Sử dụng Stream API để xử lý chuỗi nhập vào
                List<String> orderCodes = Arrays.stream(orderCodesInput.split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .distinct()
                        .collect(Collectors.toList());
                
                List<Order> orders = facade.getOrdersByOrderCodes(orderCodes);
                context.setVariable("orders", orders);
                context.setVariable("searchedCodes", orderCodesInput);
            }
        }
        
        render(req, resp, "order-tracking", context);
    }
}
