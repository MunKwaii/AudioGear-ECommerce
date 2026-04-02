package vn.edu.ute.service;

import vn.edu.ute.entity.Address;
import java.util.List;

public interface AddressService {
    List<Address> getAddressesByUserId(Long userId);
    Address getAddressById(Long id);
    Address addAddress(Long userId, Address address);
    Address updateAddress(Long userId, Address address);
    void deleteAddress(Long id);
    void setDefaultAddress(Long userId, Long addressId);
}
