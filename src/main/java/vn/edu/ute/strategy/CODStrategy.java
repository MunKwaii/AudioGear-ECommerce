package vn.edu.ute.strategy;

import java.math.BigDecimal;

public class CODStrategy implements PaymentStrategy {
    @Override
    public void pay(BigDecimal amount) {
        // TODO: Người làm Service sẽ viết logic ở đây
    }

    @Override
    public String getStrategyCode() {
        return "COD";
    }
}