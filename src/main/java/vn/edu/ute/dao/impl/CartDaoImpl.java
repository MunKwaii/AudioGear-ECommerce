package vn.edu.ute.dao.impl;

import vn.edu.ute.dao.CartDao;
import vn.edu.ute.entity.Cart;
import vn.edu.ute.entity.CartItem;
import vn.edu.ute.config.DatabaseConfig;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import java.util.List;

public class CartDaoImpl implements CartDao {

    private static CartDaoImpl instance;

    private CartDaoImpl() {}

    public static synchronized CartDaoImpl getInstance() {
        if (instance == null) {
            instance = new CartDaoImpl();
        }
        return instance;
    }

    @Override
    public Cart findByUserId(Long userId) {
        EntityManager em = DatabaseConfig.getInstance().getEntityManager();
        try {
            // Dùng FETCH JOIN để giảm rủi ro LazyInitException và n+1 query problem khi build HTML
            List<Cart> carts = em.createQuery("SELECT c FROM Cart c LEFT JOIN FETCH c.items i LEFT JOIN FETCH i.product p LEFT JOIN FETCH p.category LEFT JOIN FETCH p.brand WHERE c.user.id = :userId", Cart.class)
                    .setParameter("userId", userId)
                    .getResultList();
            return carts.isEmpty() ? null : carts.get(0);
        } finally {
            DatabaseConfig.getInstance().closeEntityManager();
        }
    }

    @Override
    public Cart saveCart(Cart cart) {
        EntityManager em = DatabaseConfig.getInstance().getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            if (cart.getId() == null) {
                em.persist(cart);
            } else {
                cart = em.merge(cart);
            }
            tx.commit();
            return cart;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            DatabaseConfig.getInstance().closeEntityManager();
        }
    }

    @Override
    public CartItem findCartItemByCartAndProduct(Long cartId, Long productId) {
        EntityManager em = DatabaseConfig.getInstance().getEntityManager();
        try {
            List<CartItem> items = em.createQuery("SELECT i FROM CartItem i WHERE i.cart.id = :cartId AND i.product.id = :productId", CartItem.class)
                    .setParameter("cartId", cartId)
                    .setParameter("productId", productId)
                    .getResultList();
            return items.isEmpty() ? null : items.get(0);
        } finally {
            DatabaseConfig.getInstance().closeEntityManager();
        }
    }

    @Override
    public CartItem saveCartItem(CartItem cartItem) {
        EntityManager em = DatabaseConfig.getInstance().getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            if (cartItem.getId() == null) {
                em.persist(cartItem);
            } else {
                cartItem = em.merge(cartItem);
            }
            tx.commit();
            return cartItem;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            DatabaseConfig.getInstance().closeEntityManager();
        }
    }

    @Override
    public void removeCartItem(Long cartItemId) {
        EntityManager em = DatabaseConfig.getInstance().getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            CartItem item = em.find(CartItem.class, cartItemId);
            if (item != null) {
                em.remove(item);
            }
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            DatabaseConfig.getInstance().closeEntityManager();
        }
    }

    @Override
    public void updateCartItemQuantity(Long cartItemId, int newQuantity) {
        EntityManager em = DatabaseConfig.getInstance().getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            CartItem item = em.find(CartItem.class, cartItemId);
            if (item != null) {
                item.setQuantity(newQuantity);
                em.merge(item);
            }
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            DatabaseConfig.getInstance().closeEntityManager();
        }
    }
}
