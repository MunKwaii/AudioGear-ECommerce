package vn.edu.ute.homepage.factory;

import vn.edu.ute.dao.CategoryDao;
import vn.edu.ute.dao.ProductDao;
import vn.edu.ute.dao.CartDao;
import vn.edu.ute.dao.impl.CategoryDaoImpl;
import vn.edu.ute.dao.impl.ProductDaoImpl;
import vn.edu.ute.dao.impl.CartDaoImpl;

public class DaoFactory {

    private DaoFactory() {}

    public static ProductDao getProductDao() {
        return ProductDaoImpl.getInstance();
    }

    public static CategoryDao getCategoryDao() {
        return CategoryDaoImpl.getInstance();
    }

    public static CartDao getCartDao() {
        return CartDaoImpl.getInstance();
    }
}
