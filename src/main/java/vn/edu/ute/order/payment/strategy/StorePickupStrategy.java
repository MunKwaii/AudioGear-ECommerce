package vn.edu.ute.order.payment.strategy;

import java.math.BigDecimal;

public class StorePickupStrategy implements PaymentStrategy {
    @Override
    public void pay(BigDecimal amount) {
        // TODO: Người làm Service sẽ viết logic ở đây
    }

    @Override
    public String getStrategyCode() {
        return "STORE_PICKUP";
    }
}