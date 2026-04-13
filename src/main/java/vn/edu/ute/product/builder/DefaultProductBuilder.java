package vn.edu.ute.product.builder;

import vn.edu.ute.entity.Brand;
import vn.edu.ute.entity.Category;
import vn.edu.ute.entity.Product;

import java.math.BigDecimal;

public class DefaultProductBuilder implements ProductBuilder {
    private final Product product;

    public DefaultProductBuilder() {
        this.product = new Product();
    }

    @Override
    public ProductBuilder name(String name) {
        product.setName(name);
        return this;
    }

    @Override
    public ProductBuilder description(String description) {
        product.setDescription(description);
        return this;
    }

    @Override
    public ProductBuilder price(BigDecimal price) {
        product.setPrice(price);
        return this;
    }

    @Override
    public ProductBuilder thumbnailUrl(String thumbnailUrl) {
        product.setThumbnailUrl(thumbnailUrl);
        return this;
    }

    @Override
    public ProductBuilder specifications(String specifications) {
        product.setSpecifications(specifications);
        return this;
    }

    @Override
    public ProductBuilder status(Boolean status) {
        product.setStatus(status);
        return this;
    }

    @Override
    public ProductBuilder category(Category category) {
        product.setCategory(category);
        return this;
    }


    @Override
    public ProductBuilder brand(Brand brand) {
        product.setBrand(brand);
        return this;
    }

    @Override
    public Product build() {
        return product;
    }
}
