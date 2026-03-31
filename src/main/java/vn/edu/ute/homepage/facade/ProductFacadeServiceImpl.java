package vn.edu.ute.service.impl;

import vn.edu.ute.dao.ProductDao;
import vn.edu.ute.dto.PageDTO;
import vn.edu.ute.dto.ProductDTO;
import vn.edu.ute.entity.Product;
import vn.edu.ute.homepage.factory.factory.DaoFactory;
import vn.edu.ute.service.ProductFacadeService;

import java.util.List;
import java.util.stream.Collectors;

public class ProductFacadeServiceImpl implements ProductFacadeService {

    private static ProductFacadeServiceImpl instance;

    private ProductFacadeServiceImpl() {}

    public static synchronized ProductFacadeServiceImpl getInstance() {
        if (instance == null) {
            instance = new ProductFacadeServiceImpl();
        }
        return instance;
    }

    @Override
    public PageDTO<ProductDTO> searchAndPaginate(String keyword, Long categoryId, int page, int pageSize) {
        if (page < 1) page = 1;
        if (pageSize < 1) pageSize = 12;

        int offset = (page - 1) * pageSize;

        ProductDao productDao = DaoFactory.getProductDao();
        
        long totalElements = productDao.countSearchProducts(keyword, categoryId);
        int totalPages = (int) Math.ceil((double) totalElements / pageSize);

        List<Product> products = productDao.searchProducts(keyword, categoryId, offset, pageSize);

        List<ProductDTO> content = products.stream()
                .map(this::mapToProductDTO)
                .collect(Collectors.toList());

        return new PageDTO<>(content, page, totalPages, totalElements, pageSize);
    }

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
                product.getStockQuantity()
        );
    }
}
