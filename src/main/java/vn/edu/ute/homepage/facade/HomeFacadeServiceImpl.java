package vn.edu.ute.homepage.facade;

import vn.edu.ute.dao.CategoryDao;
import vn.edu.ute.dao.ProductDao;
import vn.edu.ute.dto.CategoryDTO;
import vn.edu.ute.dto.HomePageDTO;
import vn.edu.ute.dto.ProductDTO;
import vn.edu.ute.entity.Category;
import vn.edu.ute.entity.Product;
import vn.edu.ute.homepage.factory.DaoFactory;

import java.util.List;
import java.util.stream.Collectors;

public class HomeFacadeServiceImpl implements HomeFacadeService {

    // Áp dụng Singleton Pattern
    private static HomeFacadeServiceImpl instance;

    private HomeFacadeServiceImpl() {
    }

    public static synchronized HomeFacadeServiceImpl getInstance() {
        if (instance == null) {
            instance = new HomeFacadeServiceImpl();
        }
        return instance;
    }

    @Override
    public HomePageDTO getHomePageData() {
        ProductDao productDao = DaoFactory.getProductDao();
        CategoryDao categoryDao = DaoFactory.getCategoryDao();

        // Lấy Entity từ Database
        List<Product> featuredProducts = productDao.getFeaturedProducts(8);
        List<Product> newProducts = productDao.getNewestProducts(8);
        List<Category> categories = categoryDao.getAllCategories();

        // Sử dụng Java Streams & Lambdas để ánh xạ Entity -> DTO
        List<ProductDTO> featuredDTOs = featuredProducts.stream()
                .filter(p -> p.getStatus() != null && p.getStatus()) // Lọc chắc chắn active
                .map(this::mapToProductDTO)
                .collect(Collectors.toList());

        List<ProductDTO> newDTOs = newProducts.stream()
                .filter(p -> p.getStatus() != null && p.getStatus())
                .map(this::mapToProductDTO)
                .collect(Collectors.toList());

        List<CategoryDTO> categoryDTOs = categories.stream()
                .map(c -> new CategoryDTO(c.getId(), c.getName(), c.getDescription()))
                .collect(Collectors.toList());

        // Sử dụng Builder Pattern để khởi tạo HomePageDTO
        return new HomePageDTO.Builder()
                .featuredProducts(featuredDTOs)
                .newProducts(newDTOs)
                .categories(categoryDTOs)
                .build();
    }

    /**
     * Helper method thực hiện ánh xạ
     */
    private ProductDTO mapToProductDTO(Product product) {
        String categoryName = product.getCategory() != null ? product.getCategory().getName() : "Khác";
        String brandName = product.getBrand() != null ? product.getBrand().getName() : "Không rõ";
        
        String thumbUrl = vn.edu.ute.util.ImageUtil.resolveImageUrl(product.getThumbnailUrl());

        return new ProductDTO(
                product.getId(),
                product.getName(),
                product.getPrice(),
                thumbUrl,
                categoryName,
                brandName,
                product.getInventory() != null ? product.getInventory().getStockQuantity() : 0
        );
    }
}
