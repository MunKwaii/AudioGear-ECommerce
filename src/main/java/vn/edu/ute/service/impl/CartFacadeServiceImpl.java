package vn.edu.ute.service.impl;

import vn.edu.ute.dao.CartDao;
import vn.edu.ute.dto.CartDTO;
import vn.edu.ute.dto.CartItemDTO;
import vn.edu.ute.entity.Cart;
import vn.edu.ute.entity.CartItem;
import vn.edu.ute.entity.Product;
import vn.edu.ute.entity.User;
import vn.edu.ute.factory.DaoFactory;
import vn.edu.ute.service.CartFacadeService;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CartFacadeServiceImpl implements CartFacadeService {

    private static CartFacadeServiceImpl instance;

    private CartFacadeServiceImpl() {}

    public static synchronized CartFacadeServiceImpl getInstance() {
        if (instance == null) {
            instance = new CartFacadeServiceImpl();
        }
        return instance;
    }

    @Override
    public CartDTO getCartDetails(Long userId) {
        CartDao cartDao = DaoFactory.getCartDao();
        Cart cart = cartDao.findByUserId(userId);

        if (cart == null || cart.getItems().isEmpty()) {
            return new CartDTO(null, new ArrayList<>(), BigDecimal.ZERO);
        }

        List<CartItemDTO> itemDTOs = cart.getItems().stream().map(item -> {
            Product product = item.getProduct();
            String thumb = vn.edu.ute.util.ImageUtil.resolveImageUrl(product.getThumbnailUrl());
            
            BigDecimal itemTotal = product.getPrice().multiply(new BigDecimal(item.getQuantity()));
            return new CartItemDTO(
                    item.getId(),
                    product.getId(),
                    product.getName(),
                    thumb,
                    product.getPrice(),
                    item.getQuantity(),
                    itemTotal
            );
        }).collect(Collectors.toList());

        BigDecimal totalAmount = itemDTOs.stream()
                .map(CartItemDTO::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new CartDTO(cart.getId(), itemDTOs, totalAmount);
    }

    @Override
    public void addToCart(Long userId, Long productId, int quantity) {
        CartDao cartDao = DaoFactory.getCartDao();
        Cart cart = cartDao.findByUserId(userId);

        if (cart == null) {
            jakarta.persistence.EntityManager em = vn.edu.ute.config.DatabaseConfig.getEntityManager();
            try {
                User user = em.find(User.class, userId);
                if (user == null) throw new IllegalArgumentException("User không tồn tại!");
                
                cart = new Cart(user);
                cart = cartDao.saveCart(cart);
            } finally {
                vn.edu.ute.config.DatabaseConfig.closeEntityManager();
            }
        }

        Product product = DaoFactory.getProductDao().searchProducts("", null, 0, 1000).stream()
                          .filter(p -> p.getId().equals(productId)).findFirst().orElse(null);
                          
        if (product == null) throw new IllegalArgumentException("Product không tồn tại!");

        CartItem existingItem = cartDao.findCartItemByCartAndProduct(cart.getId(), productId);
        if (existingItem != null) {
            // Cộng dồn số lượng
            cartDao.updateCartItemQuantity(existingItem.getId(), existingItem.getQuantity() + quantity);
        } else {
            // Thêm mới
            CartItem newItem = new CartItem(cart, product, quantity);
            cartDao.saveCartItem(newItem);
        }
    }

    @Override
    public void updateQuantity(Long cartItemId, int newQuantity) {
        if (newQuantity <= 0) {
            removeCartItem(cartItemId);
        } else {
            DaoFactory.getCartDao().updateCartItemQuantity(cartItemId, newQuantity);
        }
    }

    @Override
    public void removeCartItem(Long cartItemId) {
        DaoFactory.getCartDao().removeCartItem(cartItemId);
    }
}
