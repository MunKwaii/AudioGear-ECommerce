package vn.edu.ute.controller.order.facade;

import vn.edu.ute.entity.Order;
import java.util.List;

public interface OrderFacade {
    List<Order> getOrdersByOrderCodes(List<String> orderCodes);
    Order getOrderByOrderCode(String orderCode);
}
