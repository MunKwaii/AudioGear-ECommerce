package vn.edu.ute.cart;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import vn.edu.ute.dto.CartDTO;
import vn.edu.ute.dto.CartItemDTO;
import vn.edu.ute.entity.Product;
import vn.edu.ute.exception.InsufficientStockException;
import vn.edu.ute.homepage.factory.DaoFactory;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class GuestCartService {

    private static final String COOKIE_NAME = "guest_cart";
    private static final int MAX_AGE_SECONDS = 30 * 24 * 60 * 60; // 30 days
    private static final Gson gson = new Gson();
    private static final Type CART_TYPE = new TypeToken<List<GuestCartItem>>() {}.getType();

    private static GuestCartService instance;

    private GuestCartService() {}

    public static synchronized GuestCartService getInstance() {
        if (instance == null) {
            instance = new GuestCartService();
        }
        return instance;
    }

    public CartDTO getCart(HttpServletRequest request) {
        List<GuestCartItem> items = loadCartFromCookie(request);

        if (items.isEmpty()) {
            return new CartDTO(null, new ArrayList<>(), BigDecimal.ZERO);
        }

        List<CartItemDTO> itemDTOs = items.stream()
                .map(this::toCartItemDTO)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());

        BigDecimal totalAmount = itemDTOs.stream()
                .map(CartItemDTO::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new CartDTO(null, itemDTOs, totalAmount);
    }

    public void addToCart(HttpServletRequest request, HttpServletResponse response, Long productId, int quantity) {
        List<GuestCartItem> items = loadCartFromCookie(request);

        Product product = DaoFactory.getProductDao().findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product không tồn tại!"));

        // Null-safety: sản phẩm thêm thủ công có thể thiếu stockQuantity
        int availableStock = product.getInventory() != null ? product.getInventory().getStockQuantity() : 0;

        Optional<GuestCartItem> existingItem = items.stream()
                .filter(item -> item.productId.equals(productId))
                .findFirst();

        int totalQuantityRequested = quantity;
        if (existingItem.isPresent()) {
            totalQuantityRequested += existingItem.get().quantity;
        }

        if (totalQuantityRequested > availableStock) {
            throw new InsufficientStockException(product.getName(), availableStock);
        }

        if (existingItem.isPresent()) {
            existingItem.get().quantity = totalQuantityRequested;
        } else {
            items.add(new GuestCartItem(productId, quantity));
        }

        saveCartToCookie(response, items);
    }

    public void updateQuantity(HttpServletRequest request, HttpServletResponse response, Long productId, int newQuantity) {
        List<GuestCartItem> items = loadCartFromCookie(request);

        if (newQuantity <= 0) {
            items.removeIf(item -> item.productId.equals(productId));
        } else {
            items.stream()
                    .filter(item -> item.productId.equals(productId))
                    .findFirst()
                    .ifPresentOrElse(
                            item -> {
                                Product product = DaoFactory.getProductDao().findById(productId).orElse(null);
                                if (product != null) {
                                    int availableStock = product.getInventory() != null ? product.getInventory().getStockQuantity() : 0;
                                    if (newQuantity > availableStock) {
                                        throw new InsufficientStockException(product.getName(), availableStock);
                                    }
                                }
                                item.quantity = newQuantity;
                            },
                            () -> {
                                throw new IllegalArgumentException("Sản phẩm không có trong giỏ hàng");
                            }
                    );
        }

        saveCartToCookie(response, items);
    }

    public void removeCartItem(HttpServletRequest request, HttpServletResponse response, Long productId) {
        List<GuestCartItem> items = loadCartFromCookie(request);
        items.removeIf(item -> item.productId.equals(productId));
        saveCartToCookie(response, items);
    }

    public void clearCart(HttpServletResponse response) {
        Cookie cookie = new Cookie(COOKIE_NAME, "");
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }

    public List<vn.edu.ute.dto.request.CheckoutItemRequest> toCheckoutItems(HttpServletRequest request) {
        List<GuestCartItem> items = loadCartFromCookie(request);
        return items.stream()
                .map(item -> new vn.edu.ute.dto.request.CheckoutItemRequest(item.productId, item.quantity))
                .collect(Collectors.toList());
    }

    public boolean hasItems(HttpServletRequest request) {
        return !loadCartFromCookie(request).isEmpty();
    }

    private List<GuestCartItem> loadCartFromCookie(HttpServletRequest request) {
        if (request.getCookies() == null) return new ArrayList<>();

        Cookie cookie = java.util.Arrays.stream(request.getCookies())
                .filter(c -> COOKIE_NAME.equals(c.getName()))
                .findFirst()
                .orElse(null);

        if (cookie == null || cookie.getValue().isEmpty()) {
            return new ArrayList<>();
        }

        try {
            String decoded = URLDecoder.decode(cookie.getValue(), StandardCharsets.UTF_8);
            List<GuestCartItem> items = gson.fromJson(decoded, CART_TYPE);
            return items != null ? items : new ArrayList<>();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private void saveCartToCookie(HttpServletResponse response, List<GuestCartItem> items) {
        String json = gson.toJson(items);
        String encoded = URLEncoder.encode(json, StandardCharsets.UTF_8);

        Cookie cookie = new Cookie(COOKIE_NAME, encoded);
        cookie.setPath("/");
        cookie.setMaxAge(MAX_AGE_SECONDS);
        cookie.setHttpOnly(true);
        response.addCookie(cookie);
    }

    private Optional<CartItemDTO> toCartItemDTO(GuestCartItem guestItem) {
        return DaoFactory.getProductDao().findById(guestItem.productId)
                .map(product -> {
                    String thumb = vn.edu.ute.util.ImageUtil.resolveImageUrl(product.getThumbnailUrl());
                    BigDecimal itemTotal = product.getPrice().multiply(BigDecimal.valueOf(guestItem.quantity));
                    return new CartItemDTO(
                            null,
                            product.getId(),
                            product.getName(),
                            thumb,
                            product.getPrice(),
                            guestItem.quantity,
                            itemTotal,
                            product.getInventory() != null ? product.getInventory().getStockQuantity() : 0
                    );
                });
    }

    private static class GuestCartItem {
        Long productId;
        int quantity;

        GuestCartItem() {}

        GuestCartItem(Long productId, int quantity) {
            this.productId = productId;
            this.quantity = quantity;
        }
    }
}
