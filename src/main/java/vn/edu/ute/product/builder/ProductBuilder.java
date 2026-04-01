package vn.edu.ute.product.builder;

import vn.edu.ute.entity.Brand;
import vn.edu.ute.entity.Category;
import vn.edu.ute.entity.Product;

import java.math.BigDecimal;

public interface ProductBuilder {
    ProductBuilder name(String name);
    ProductBuilder description(String description);
    ProductBuilder price(BigDecimal price);
    ProductBuilder thumbnailUrl(String thumbnailUrl);
    ProductBuilder specifications(String specifications);
    ProductBuilder status(Boolean status);
    ProductBuilder stockQuantity(Integer stockQuantity);
    ProductBuilder category(Category category);
    ProductBuilder brand(Brand brand);

    Product build();
}
