package vn.edu.ute.strategy;

import java.math.BigDecimal;

public class MomoStrategy implements PaymentStrategy {
    @Override
    public void pay(BigDecimal amount) {
        // TODO: Call Momo API logic here
    }

    @Override
    public String getStrategyCode() {
        return "MOMO";
    }
}
