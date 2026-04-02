package vn.edu.ute.service.impl;

import vn.edu.ute.dao.AddressDao;
import vn.edu.ute.dao.impl.AddressDaoImpl;
import vn.edu.ute.entity.Address;
import vn.edu.ute.entity.User;
import vn.edu.ute.service.AddressService;
import vn.edu.ute.service.UserService;

import java.util.List;

public class AddressServiceImpl implements AddressService {

    private final AddressDao addressDao;
    private final UserService userService;

    public AddressServiceImpl() {
        this.addressDao = new AddressDaoImpl();
        this.userService = new UserServiceImpl();
    }

    @Override
    public List<Address> getAddressesByUserId(Long userId) {
        return addressDao.findByUserId(userId);
    }

    @Override
    public Address getAddressById(Long id) {
        return addressDao.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy địa chỉ!"));
    }

    @Override
    public Address addAddress(Long userId, Address address) {
        User user = userService.getUserById(userId);
        address.setUser(user);
        
        // Nếu là địa chỉ đầu tiên hoặc được đánh dấu mặc định
        List<Address> existing = addressDao.findByUserId(userId);
        if (existing.isEmpty() || address.getIsDefault()) {
            address.setIsDefault(true);
            addressDao.resetDefaultAddress(userId);
        }
        
        return addressDao.save(address);
    }

    @Override
    public Address updateAddress(Long userId, Address address) {
        Address existing = getAddressById(address.getId());
        
        // Xác thực địa chỉ này có thuộc về user hay không
        if (!existing.getUser().getId().equals(userId)) {
            throw new RuntimeException("Không có quyền cập nhật địa chỉ này!");
        }

        existing.setRecipientName(address.getRecipientName());
        existing.setPhoneNumber(address.getPhoneNumber());
        existing.setStreetAddress(address.getStreetAddress());
        existing.setProvince(address.getProvince());
        existing.setProvinceCode(address.getProvinceCode());
        existing.setDistrict(address.getDistrict());
        existing.setDistrictCode(address.getDistrictCode());
        existing.setWard(address.getWard());
        existing.setWardCode(address.getWardCode());

        if (address.getIsDefault() && !existing.getIsDefault()) {
            addressDao.resetDefaultAddress(userId);
            existing.setIsDefault(true);
        }

        return addressDao.save(existing);
    }

    @Override
    public void deleteAddress(Long id) {
        Address address = getAddressById(id);
        
        if (address.getIsDefault()) {
            throw new RuntimeException("Không thể xoá địa chỉ mặc định!");
        }
        
        addressDao.delete(address);
    }

    @Override
    public void setDefaultAddress(Long userId, Long addressId) {
        Address address = getAddressById(addressId);
        
        if (!address.getUser().getId().equals(userId)) {
            throw new RuntimeException("Không có quyền thực hiện hành động này!");
        }
        
        addressDao.resetDefaultAddress(userId);
        address.setIsDefault(true);
        addressDao.save(address);
    }
}
