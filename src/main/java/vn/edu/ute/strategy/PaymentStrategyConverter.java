package vn.edu.ute.strategy;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class PaymentStrategyConverter implements AttributeConverter<PaymentStrategy, String> {

    @Override
    public String convertToDatabaseColumn(PaymentStrategy strategy) {
        if (strategy == null) {
            return null;
        }
        return strategy.getStrategyCode();
    }

    @Override
    public PaymentStrategy convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.trim().isEmpty()) {
            return null;
        }

        switch (dbData) {
            case "COD":
                return new CODStrategy();
            case "BANK_TRANSFER":
                return new BankTransferStrategy();
            case "STORE_PICKUP":
                return new StorePickupStrategy();
            default:
                throw new IllegalArgumentException("Unknown payment strategy code: " + dbData);
        }
    }
}
