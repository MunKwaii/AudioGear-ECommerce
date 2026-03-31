package vn.edu.ute.homepage.factory.factory;

import vn.edu.ute.homepage.facade.HomeFacadeService;
import vn.edu.ute.homepage.facade.HomeFacadeServiceImpl;
import vn.edu.ute.service.ProductFacadeService;
import vn.edu.ute.service.impl.ProductFacadeServiceImpl;
import vn.edu.ute.cart.CartFacadeService;
import vn.edu.ute.cart.CartFacadeServiceImpl;

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
}
