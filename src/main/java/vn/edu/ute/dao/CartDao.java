package vn.edu.ute.dao;

import vn.edu.ute.entity.Cart;
import vn.edu.ute.entity.CartItem;

public interface CartDao {
    Cart findByUserId(Long userId);
    Cart saveCart(Cart cart);
    CartItem findCartItemByCartAndProduct(Long cartId, Long productId);
    CartItem saveCartItem(CartItem cartItem);
    void removeCartItem(Long cartItemId);
    void updateCartItemQuantity(Long cartItemId, int newQuantity);
}
