package vn.edu.ute.factory;

import vn.edu.ute.service.HomeFacadeService;
import vn.edu.ute.service.impl.HomeFacadeServiceImpl;
import vn.edu.ute.service.ProductFacadeService;
import vn.edu.ute.service.impl.ProductFacadeServiceImpl;
import vn.edu.ute.service.CartFacadeService;
import vn.edu.ute.service.impl.CartFacadeServiceImpl;

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
