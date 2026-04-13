package vn.edu.ute.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import vn.edu.ute.dao.CategoryDao;
import vn.edu.ute.dao.ProductDao;
import vn.edu.ute.dto.HomePageDTO;
import vn.edu.ute.entity.Category;
import vn.edu.ute.entity.Product;
import vn.edu.ute.homepage.factory.DaoFactory;
import vn.edu.ute.homepage.factory.ServiceFactory;
import vn.edu.ute.homepage.facade.HomeFacadeService;

import java.math.BigDecimal;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class HomeFacadeServiceTest {

    private MockedStatic<DaoFactory> daoFactoryMockedStatic;
    private ProductDao mockProductDao;
    private CategoryDao mockCategoryDao;
    private HomeFacadeService homeFacadeService;

    @BeforeEach
    void setUp() {
        // 1. Tạo các Object Mock thay thế cho DAO thật kết nối DB
        mockProductDao = mock(ProductDao.class);
        mockCategoryDao = mock(CategoryDao.class);

        // 2. Mock static method của DaoFactory để ép nó trả về Mock thay vì DAO thật
        daoFactoryMockedStatic = mockStatic(DaoFactory.class);
        daoFactoryMockedStatic.when(DaoFactory::getProductDao).thenReturn(mockProductDao);
        daoFactoryMockedStatic.when(DaoFactory::getCategoryDao).thenReturn(mockCategoryDao);

        // 3. Khởi tạo service mình đem đi test
        homeFacadeService = ServiceFactory.getHomeFacadeService();
    }

    @AfterEach
    void tearDown() {
        // Bắt buộc phải đóng MockedStatic sau mỗi lần chạy test
        if (daoFactoryMockedStatic != null && !daoFactoryMockedStatic.isClosed()) {
            daoFactoryMockedStatic.close();
        }
    }

    @Test
    void testGetHomePageData() {
        // Chuẩn bị Mock Data (DB giả mạo)
        // Lưu ý: Sản phẩm 2 bị tắt status (trạng thái kinh doanh)
        Product p1 = new Product("Tai nghe Sony XM5", new BigDecimal("8000000"), new Category("Headphones"), null);
        p1.setId(1L);
        p1.setStatus(true);
        vn.edu.ute.entity.Inventory inv1 = new vn.edu.ute.entity.Inventory();
        inv1.setStockQuantity(10);
        p1.setInventory(inv1);

        Product p2 = new Product("Loa Marshall Cũ", new BigDecimal("3500000"), null, null);
        p2.setId(2L);
        p2.setStatus(false); // Inactive
        vn.edu.ute.entity.Inventory inv2 = new vn.edu.ute.entity.Inventory();
        inv2.setStockQuantity(5);
        p2.setInventory(inv2);

        Category c1 = new Category("Amplyifier");
        c1.setId(1L);

        // Yêu cầu Mock DAO trả về data giả khi bị Facade gọi tới
        when(mockProductDao.getFeaturedProducts(8)).thenReturn(Arrays.asList(p1, p2));
        when(mockProductDao.getNewestProducts(8)).thenReturn(Arrays.asList(p1));
        when(mockCategoryDao.getAllCategories()).thenReturn(Arrays.asList(c1));

        // ACTION: Thực thi function lấy Data cho Trang chủ
        HomePageDTO result = homeFacadeService.getHomePageData();

        // ASSERT: Kiểm tra lỗi logic của lập trình viên
        assertNotNull(result, "Dữ liệu trả về không được null");

        // Featured products CHỈ ĐƯỢC có 1 sản phẩm, vì sản phẩm 2 bị status = false (phải lọt qua Filter Stream)
        assertEquals(1, result.getFeaturedProducts().size(), "Logic stream filter bị sai, không loại bỏ Inactive!");
        assertEquals("Tai nghe Sony XM5", result.getFeaturedProducts().get(0).getName());

        // Test danh mục
        assertEquals(1, result.getCategories().size());
        assertEquals("Amplyifier", result.getCategories().get(0).getName());
    }
}
