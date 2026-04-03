package vn.edu.ute.homepage.factory;

import vn.edu.ute.homepage.facade.HomeFacadeService;
import vn.edu.ute.homepage.facade.HomeFacadeServiceImpl;
import vn.edu.ute.homepage.facade.ProductFacadeService;
import vn.edu.ute.homepage.facade.ProductFacadeServiceImpl;
import vn.edu.ute.cart.CartFacadeService;
import vn.edu.ute.cart.CartFacadeServiceImpl;
import vn.edu.ute.service.CategoryService;
import vn.edu.ute.service.impl.CategoryServiceImpl;

public class ServiceFactory {
    
    private ServiceFactory() {}

    public static HomeFacadeService getHomeFacadeService() {
        return HomeFacadeServiceImpl.getInstance();
    }

    public static ProductFacadeService getProductFacadeService() {
        return ProductFacadeServiceImpl.getInstance();
    }

    public static CartFacadeService getCartFacadeService() {
        return CartFacadeServiceImpl.getInstance();
    }

    public static CategoryService getCategoryService() {
        return CategoryServiceImpl.getInstance();
    }
}
