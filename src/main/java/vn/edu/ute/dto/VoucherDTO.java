package vn.edu.ute.dto;

import vn.edu.ute.entity.Voucher;
import vn.edu.ute.entity.enums.DiscountType;
import vn.edu.ute.entity.enums.VoucherStatus;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

public class VoucherDTO {
    private Long id;
    private String code;
    private DiscountType discountType;
    private BigDecimal discountValue;
    private Integer maxUsage;
    private BigDecimal minOrderValue;
    private String expiryDate;
    private VoucherStatus status;
    private String createdAt;
    private String updatedAt;
    private long orderCount;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public VoucherDTO() {}

    public static VoucherDTO fromEntity(Voucher v, long count) {
        VoucherDTO dto = new VoucherDTO();
        dto.setId(v.getId());
        dto.setCode(v.getCode());
        dto.setDiscountType(v.getDiscountType());
        dto.setDiscountValue(v.getDiscountValue());
        dto.setMaxUsage(v.getMaxUsage());
        dto.setMinOrderValue(v.getMinOrderValue());
        dto.setExpiryDate(v.getExpiryDate() != null ? v.getExpiryDate().format(FORMATTER) : null);
        dto.setStatus(v.getStatus());
        dto.setCreatedAt(v.getCreatedAt() != null ? v.getCreatedAt().format(FORMATTER) : null);
        dto.setUpdatedAt(v.getUpdatedAt() != null ? v.getUpdatedAt().format(FORMATTER) : null);
        dto.setOrderCount(count);
        return dto;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public DiscountType getDiscountType() { return discountType; }
    public void setDiscountType(DiscountType discountType) { this.discountType = discountType; }

    public BigDecimal getDiscountValue() { return discountValue; }
    public void setDiscountValue(BigDecimal discountValue) { this.discountValue = discountValue; }

    public Integer getMaxUsage() { return maxUsage; }
    public void setMaxUsage(Integer maxUsage) { this.maxUsage = maxUsage; }

    public BigDecimal getMinOrderValue() { return minOrderValue; }
    public void setMinOrderValue(BigDecimal minOrderValue) { this.minOrderValue = minOrderValue; }

    public String getExpiryDate() { return expiryDate; }
    public void setExpiryDate(String expiryDate) { this.expiryDate = expiryDate; }

    public VoucherStatus getStatus() { return status; }
    public void setStatus(VoucherStatus status) { this.status = status; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    public long getOrderCount() { return orderCount; }
    public void setOrderCount(long orderCount) { this.orderCount = orderCount; }
}
