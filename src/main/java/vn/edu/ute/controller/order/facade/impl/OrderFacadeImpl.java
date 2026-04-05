package vn.edu.ute.controller.order.facade.impl;

import jakarta.servlet.ServletContext;
import vn.edu.ute.controller.order.facade.OrderFacade;
import vn.edu.ute.entity.Order;
import vn.edu.ute.service.OrderService;
import vn.edu.ute.service.impl.OrderServiceImpl;

import java.util.List;

public class OrderFacadeImpl implements OrderFacade {
    private final OrderService orderService;

    public OrderFacadeImpl(ServletContext context) {
        this.orderService = new OrderServiceImpl();
    }

    @Override
    public List<Order> getOrdersByOrderCodes(List<String> orderCodes) {
        return orderService.getOrdersByOrderCodes(orderCodes);
    }

    @Override
    public Order getOrderByOrderCode(String orderCode) {
        return orderService.getOrderByOrderCode(orderCode);
    }
}
