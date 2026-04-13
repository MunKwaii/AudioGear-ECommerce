package vn.edu.ute.controller.profile.facade.impl;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Part;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import vn.edu.ute.controller.profile.facade.UserProfileFacade;
import vn.edu.ute.entity.Address;
import vn.edu.ute.entity.Order;
import vn.edu.ute.entity.User;
import vn.edu.ute.service.AddressService;
import vn.edu.ute.service.OrderService;
import vn.edu.ute.service.UserService;
import vn.edu.ute.service.impl.AddressServiceImpl;
import vn.edu.ute.service.impl.OrderServiceImpl;
import vn.edu.ute.service.impl.UserServiceImpl;
import vn.edu.ute.util.storage.CloudinaryStorageStrategy;
import vn.edu.ute.util.storage.PathResolver;
import vn.edu.ute.util.storage.StorageStrategy;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

/**
 * Facade Pattern Implementation: Kết nối và quản lý logic cho Profile.
 */
public class UserProfileFacadeImpl implements UserProfileFacade {

    private static final Logger logger = LogManager.getLogger(UserProfileFacadeImpl.class);

    private final UserService userService;
    private final AddressService addressService;
    private final OrderService orderService;
    private final StorageStrategy storageStrategy;

    public UserProfileFacadeImpl(ServletContext servletContext) {
        this.userService = new UserServiceImpl();
        this.addressService = new AddressServiceImpl();
        this.orderService = new OrderServiceImpl();

        // Cấu hình Storage Strategy (Chuyển sang dùng Cloudinary)
        this.storageStrategy = new CloudinaryStorageStrategy();
    }

    @Override
    public User getUserDetails(Long userId) {
        return userService.getUserById(userId);
    }

    @Override
    public void updateProfile(Long userId, String fullName, String phoneNumber) {
        userService.updateUserProfile(userId, fullName, phoneNumber);
    }

    @Override
    public void updateAvatar(Long userId, Part avatarPart) throws IOException {
        String fileName = Paths.get(avatarPart.getSubmittedFileName()).getFileName().toString();
        String extension = fileName.substring(fileName.lastIndexOf("."));
        String newFileName = "avatar_" + userId + "_" + UUID.randomUUID() + extension;

        // Sử dụng Strategy Pattern để thực hiện lưu trữ
        String avatarUrl = storageStrategy.store(avatarPart, newFileName, "avatars");
        userService.updateAvatar(userId, avatarUrl);
    }

    @Override
    public void changePassword(Long userId, String currentPassword, String newPassword, String confirmNewPassword) {
        userService.changePassword(userId, currentPassword, newPassword, confirmNewPassword);
    }

    @Override
    public List<Address> getUserAddresses(Long userId) {
        return addressService.getAddressesByUserId(userId);
    }

    @Override
    public void addAddress(Long userId, Address address) {
        addressService.addAddress(userId, address);
    }

    @Override
    public void updateAddress(Long userId, Address address) {
        addressService.updateAddress(userId, address);
    }

    @Override
    public void deleteAddress(Long addressId) {
        addressService.deleteAddress(addressId);
    }

    @Override
    public void setDefaultAddress(Long userId, Long addressId) {
        addressService.setDefaultAddress(userId, addressId);
    }

    @Override
    public List<Order> getUserOrders(Long userId) {
        return orderService.getOrdersByUserId(userId);
    }

    @Override
    public Address extractAddressFromRequest(HttpServletRequest req) {
        Address address = new Address();
        String idStr = req.getParameter("id");
        if (idStr != null && !idStr.trim().isEmpty()) {
            address.setId(Long.parseLong(idStr.trim()));
        }
        address.setRecipientName(req.getParameter("recipientName"));
        address.setPhoneNumber(req.getParameter("phoneNumber"));
        address.setStreetAddress(req.getParameter("streetAddress"));
        address.setProvince(req.getParameter("province"));
        address.setProvinceCode(req.getParameter("provinceCode"));
        address.setDistrict(req.getParameter("district"));
        address.setDistrictCode(req.getParameter("districtCode"));
        address.setWard(req.getParameter("ward"));
        address.setWardCode(req.getParameter("wardCode"));
        address.setIsDefault("on".equals(req.getParameter("isDefault")));
        return address;
    }
}
