package vn.edu.ute.auth.adapter;

import vn.edu.ute.entity.User;

/**
 * Target Interface cho Adapter Pattern.
 * Đóng vai trò là interface mong muốn (Target) mà Client (GoogleLoginStrategy) sử dụng.
 */
public interface OAuthUserAdapter {
    /**
     * Chuyển đổi dữ liệu từ Adaptee sang đối tượng User
     *
     * @return Đối tượng User
     */
    User adaptToUser();
}
