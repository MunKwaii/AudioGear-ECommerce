package vn.edu.ute.dto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class CategoryDTO {
    private Long id;
    private String name;
    private String description;
    private Long parentId;
    private String parentName;
    private List<CategoryDTO> children = new ArrayList<>();
    private long productCount;
    private int level;
    // Sử dụng String thay vì LocalDateTime để tránh lỗi Jackson serialize trong Thymeleaf inline JS
    private String createdAt;
    private String updatedAt;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public CategoryDTO() {}

    public CategoryDTO(Long id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    private CategoryDTO(Builder builder) {
        this.id = builder.id;
        this.name = builder.name;
        this.description = builder.description;
        this.parentId = builder.parentId;
        this.parentName = builder.parentName;
        this.children = builder.children;
        this.productCount = builder.productCount;
        this.level = builder.level;
        this.createdAt = builder.createdAt;
        this.updatedAt = builder.updatedAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Long getParentId() { return parentId; }
    public void setParentId(Long parentId) { this.parentId = parentId; }

    public String getParentName() { return parentName; }
    public void setParentName(String parentName) { this.parentName = parentName; }

    public List<CategoryDTO> getChildren() { return children; }
    public void setChildren(List<CategoryDTO> children) { this.children = children; }

    public long getProductCount() { return productCount; }
    public void setProductCount(long productCount) { this.productCount = productCount; }

    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    public static class Builder {
        private Long id;
        private String name;
        private String description;
        private Long parentId;
        private String parentName;
        private List<CategoryDTO> children = new ArrayList<>();
        private long productCount;
        private int level;
        private String createdAt;
        private String updatedAt;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder name(String name) { this.name = name; return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder parentId(Long parentId) { this.parentId = parentId; return this; }
        public Builder parentName(String parentName) { this.parentName = parentName; return this; }
        public Builder children(List<CategoryDTO> children) { this.children = children; return this; }
        public Builder productCount(long productCount) { this.productCount = productCount; return this; }
        public Builder level(int level) { this.level = level; return this; }

        public Builder createdAt(LocalDateTime dt) {
            this.createdAt = (dt != null) ? dt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : null;
            return this;
        }
        public Builder updatedAt(LocalDateTime dt) {
            this.updatedAt = (dt != null) ? dt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : null;
            return this;
        }

        public CategoryDTO build() { return new CategoryDTO(this); }
    }
}
