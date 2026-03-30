package vn.edu.ute.service.impl;

import vn.edu.ute.dao.OrderDao;
import vn.edu.ute.dao.impl.OrderDaoImpl;
import vn.edu.ute.entity.Order;
import vn.edu.ute.service.OrderService;
import vn.edu.ute.state.OrderContext;

import java.util.List;

public class OrderServiceImpl implements OrderService {

    private final OrderDao orderDao;

    public OrderServiceImpl() {
        this.orderDao = new OrderDaoImpl();
    }

    @Override
    public List<Order> getAllOrders() {
        return orderDao.findAll();
    }

    @Override
    public Order getOrderById(Long id) {
        return orderDao.findById(id).orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng ID: " + id));
    }

    @Override
    public Order processOrder(Long orderId) {
        Order order = getOrderById(orderId);
        OrderContext context = new OrderContext(order);
        
        context.processOrder();
        return orderDao.save(context.getOrder());
    }

    @Override
    public Order shipOrder(Long orderId) {
        Order order = getOrderById(orderId);
        OrderContext context = new OrderContext(order);
        
        context.shipOrder();
        return orderDao.save(context.getOrder());
    }

    @Override
    public Order deliverOrder(Long orderId) {
        Order order = getOrderById(orderId);
        OrderContext context = new OrderContext(order);
        
        context.deliverOrder();
        return orderDao.save(context.getOrder());
    }

    @Override
    public Order cancelOrder(Long orderId, String reason) {
        Order order = getOrderById(orderId);
        OrderContext context = new OrderContext(order);
        
        context.cancelOrder(reason != null ? reason : "Không có lý do cụ thể");
        return orderDao.save(context.getOrder());
    }
}
