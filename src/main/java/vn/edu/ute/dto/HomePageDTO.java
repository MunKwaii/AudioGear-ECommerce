package vn.edu.ute.dto;

import java.util.List;
import java.util.ArrayList;

public class HomePageDTO {
    private final List<ProductDTO> featuredProducts;
    private final List<ProductDTO> newProducts;
    private final List<CategoryDTO> categories;

    private HomePageDTO(Builder builder) {
        this.featuredProducts = builder.featuredProducts;
        this.newProducts = builder.newProducts;
        this.categories = builder.categories;
    }

    public List<ProductDTO> getFeaturedProducts() {
        return featuredProducts;
    }

    public List<ProductDTO> getNewProducts() {
        return newProducts;
    }

    public List<CategoryDTO> getCategories() {
        return categories;
    }

    public static class Builder {
        private List<ProductDTO> featuredProducts = new ArrayList<>();
        private List<ProductDTO> newProducts = new ArrayList<>();
        private List<CategoryDTO> categories = new ArrayList<>();

        public Builder featuredProducts(List<ProductDTO> featuredProducts) {
            this.featuredProducts = featuredProducts;
            return this;
        }

        public Builder newProducts(List<ProductDTO> newProducts) {
            this.newProducts = newProducts;
            return this;
        }

        public Builder categories(List<CategoryDTO> categories) {
            this.categories = categories;
            return this;
        }

        public HomePageDTO build() {
            return new HomePageDTO(this);
        }
    }
}
