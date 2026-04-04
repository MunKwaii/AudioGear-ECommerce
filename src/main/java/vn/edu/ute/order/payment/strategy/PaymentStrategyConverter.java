package vn.edu.ute.order.payment.strategy;

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

        switch (dbData.toUpperCase()) {
            case "COD":
                return new CODStrategy();
            case "MOMO":
                return new MomoStrategy();
            case "BANK":
            case "BANK_TRANSFER": // Giữ lại dự phòng lỗi cho DB cũ
                return new BankTransferStrategy();
            case "STORE_PICKUP":
                return new StorePickupStrategy();
            case "SEPAY_QR":
                return new SePayStrategy();
            default:
                // Tránh việc văng lỗi trắng trang Admin nếu trong DB có dữ liệu cũ chưa map
                System.err.println("Unknown payment strategy code in DB: " + dbData + ". Falling back to COD.");
                return new CODStrategy();
        }
    }
}
