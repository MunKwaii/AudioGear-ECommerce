package vn.edu.ute.cart;

import vn.edu.ute.dto.CartDTO;

public interface CartFacadeService {
    CartDTO getCartDetails(Long userId);
    void addToCart(Long userId, Long productId, int quantity);
    void updateQuantity(Long cartItemId, int newQuantity);
    void removeCartItem(Long cartItemId);
}
