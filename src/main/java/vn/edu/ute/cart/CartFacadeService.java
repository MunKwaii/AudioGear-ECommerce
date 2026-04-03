package vn.edu.ute.cart;

import vn.edu.ute.dto.CartDTO;
import java.util.List;

public interface CartFacadeService {
    CartDTO getCartDetails(Long userId);
    void addToCart(Long userId, Long productId, int quantity);
    void updateQuantity(Long cartItemId, int newQuantity);
    void removeCartItem(Long cartItemId);
    void mergeCart(Long userId, List<vn.edu.ute.dto.request.CheckoutItemRequest> guestItems);
}
