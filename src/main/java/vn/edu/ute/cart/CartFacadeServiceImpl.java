package vn.edu.ute.cart;

import vn.edu.ute.dao.CartDao;
import vn.edu.ute.dto.CartDTO;
import vn.edu.ute.dto.CartItemDTO;
import vn.edu.ute.entity.Cart;
import vn.edu.ute.entity.CartItem;
import vn.edu.ute.entity.Product;
import vn.edu.ute.entity.User;
import vn.edu.ute.exception.InsufficientStockException;
import vn.edu.ute.entity.Inventory;
import vn.edu.ute.homepage.factory.DaoFactory;

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
                    itemTotal,
                    DaoFactory.getInventoryDao().findByProductId(product.getId()).map(Inventory::getStockQuantity).orElse(0)
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
            jakarta.persistence.EntityManager em = vn.edu.ute.config.DatabaseConfig.getInstance().getEntityManager();
            try {
                User user = em.find(User.class, userId);
                if (user == null) throw new IllegalArgumentException("User không tồn tại!");
                
                cart = new Cart(user);
                cart = cartDao.saveCart(cart);
            } finally {
                vn.edu.ute.config.DatabaseConfig.getInstance().closeEntityManager();
            }
        }

        Product product = DaoFactory.getProductDao().findById(productId).orElse(null);
                          
        if (product == null) throw new IllegalArgumentException("Product không tồn tại!");

        // Null-safety: sản phẩm thêm thủ công có thể thiếu stockQuantity
        int availableStock = DaoFactory.getInventoryDao().findByProductId(productId).map(Inventory::getStockQuantity).orElse(0);

        CartItem existingItem = cartDao.findCartItemByCartAndProduct(cart.getId(), productId);
        int totalQuantityRequested = quantity;
        if (existingItem != null) {
            totalQuantityRequested += existingItem.getQuantity();
        }

        if (totalQuantityRequested > availableStock) {
            throw new InsufficientStockException(product.getName(), availableStock);
        }

        if (existingItem != null) {
            // Cộng dồn số lượng
            cartDao.updateCartItemQuantity(existingItem.getId(), totalQuantityRequested);
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
            jakarta.persistence.EntityManager em = vn.edu.ute.config.DatabaseConfig.getInstance().getEntityManager();
            try {
                CartItem item = em.find(CartItem.class, cartItemId);
                if (item != null) {
                    int availableStock = DaoFactory.getInventoryDao().findByProductId(item.getProduct().getId()).map(Inventory::getStockQuantity).orElse(0);
                    if (newQuantity > availableStock) {
                        throw new InsufficientStockException(item.getProduct().getName(), availableStock);
                    }
                }
            } finally {
                vn.edu.ute.config.DatabaseConfig.getInstance().closeEntityManager();
            }
            DaoFactory.getCartDao().updateCartItemQuantity(cartItemId, newQuantity);
        }
    }

    @Override
    public void removeCartItem(Long cartItemId) {
        DaoFactory.getCartDao().removeCartItem(cartItemId);
    }

    @Override
    public void mergeCart(Long userId, List<vn.edu.ute.dto.request.CheckoutItemRequest> guestItems) {
        if (guestItems == null || guestItems.isEmpty()) {
            return;
        }

        CartDao cartDao = DaoFactory.getCartDao();
        Cart cart = getOrCreateCart(userId, cartDao);

        guestItems.stream()
                .filter(item -> item.getProductId() != null && item.getQuantity() != null && item.getQuantity() > 0)
                .forEach(guestItem -> {
                    Product product = DaoFactory.getProductDao().findById(guestItem.getProductId()).orElse(null);
                    if (product == null) return;

                    CartItem existingItem = cartDao.findCartItemByCartAndProduct(cart.getId(), guestItem.getProductId());
                    int totalQuantity = guestItem.getQuantity();
                    if (existingItem != null) {
                        totalQuantity += existingItem.getQuantity();
                    }

                    int availableStock = DaoFactory.getInventoryDao().findByProductId(product.getId()).map(Inventory::getStockQuantity).orElse(0);
                    int cappedQuantity = Math.min(totalQuantity, availableStock);

                    if (existingItem != null) {
                        if (cappedQuantity > 0) {
                            cartDao.updateCartItemQuantity(existingItem.getId(), cappedQuantity);
                        } else {
                            cartDao.removeCartItem(existingItem.getId());
                        }
                    } else if (cappedQuantity > 0) {
                        CartItem newItem = new CartItem(cart, product, cappedQuantity);
                        cartDao.saveCartItem(newItem);
                    }
                });
    }

    private Cart getOrCreateCart(Long userId, CartDao cartDao) {
        Cart cart = cartDao.findByUserId(userId);
        if (cart != null) {
            return cart;
        }

        jakarta.persistence.EntityManager em = vn.edu.ute.config.DatabaseConfig.getInstance().getEntityManager();
        try {
            User user = em.find(User.class, userId);
            if (user == null) {
                throw new IllegalArgumentException("User không tồn tại!");
            }
            cart = new Cart(user);
            return cartDao.saveCart(cart);
        } finally {
            vn.edu.ute.config.DatabaseConfig.getInstance().closeEntityManager();
        }
    }
}
