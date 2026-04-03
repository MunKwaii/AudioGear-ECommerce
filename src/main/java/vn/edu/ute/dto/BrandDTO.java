package vn.edu.ute.dto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class BrandDTO {
    private Long id;
    private String name;
    private String description;
    private String logoUrl;
    private long productCount;
    private String createdAt;
    private String updatedAt;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public BrandDTO() {}

    private BrandDTO(Builder builder) {
        this.id = builder.id;
        this.name = builder.name;
        this.description = builder.description;
        this.logoUrl = builder.logoUrl;
        this.productCount = builder.productCount;
        this.createdAt = builder.createdAt;
        this.updatedAt = builder.updatedAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getLogoUrl() { return logoUrl; }
    public void setLogoUrl(String logoUrl) { this.logoUrl = logoUrl; }

    public long getProductCount() { return productCount; }
    public void setProductCount(long productCount) { this.productCount = productCount; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    public static class Builder {
        private Long id;
        private String name;
        private String description;
        private String logoUrl;
        private long productCount;
        private String createdAt;
        private String updatedAt;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder name(String name) { this.name = name; return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder logoUrl(String logoUrl) { this.logoUrl = logoUrl; return this; }
        public Builder productCount(long productCount) { this.productCount = productCount; return this; }

        public Builder createdAt(LocalDateTime dt) {
            this.createdAt = (dt != null) ? dt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : null;
            return this;
        }
        public Builder updatedAt(LocalDateTime dt) {
            this.updatedAt = (dt != null) ? dt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : null;
            return this;
        }

        public BrandDTO build() { return new BrandDTO(this); }
    }
}
