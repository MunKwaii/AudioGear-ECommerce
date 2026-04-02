package vn.edu.ute.controller.profile.facade;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Part;
import vn.edu.ute.entity.Address;
import vn.edu.ute.entity.Order;
import vn.edu.ute.entity.User;

import java.io.IOException;
import java.util.List;

/**
 * Facade Pattern: Cung cấp một giao diện đơn giản nhất cho Module Profile.
 * Gom nhóm logic từ nhiều Service.
 */
public interface UserProfileFacade {
    
    User getUserDetails(Long userId);
    
    void updateProfile(Long userId, String fullName, String phoneNumber);
    
    void updateAvatar(Long userId, Part avatarPart) throws IOException;
    
    List<Address> getUserAddresses(Long userId);
    
    void addAddress(Long userId, Address address);
    
    void updateAddress(Long userId, Address address);
    
    void deleteAddress(Long addressId);
    
    void setDefaultAddress(Long userId, Long addressId);
    
    List<Order> getUserOrders(Long userId);
    
    // Helper to extract address from request
    Address extractAddressFromRequest(HttpServletRequest req);
}
