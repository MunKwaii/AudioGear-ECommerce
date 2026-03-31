package vn.edu.ute.order.payment.strategy;

import java.math.BigDecimal;

public interface PaymentStrategy {
    void pay(BigDecimal amount);
    String getStrategyCode(); // Phục vụ cho Converter
}