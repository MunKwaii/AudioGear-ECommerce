package vn.edu.ute.dao;

import vn.edu.ute.entity.Address;
import java.util.List;
import java.util.Optional;

public interface AddressDao {
    List<Address> findByUserId(Long userId);
    Optional<Address> findById(Long id);
    Address save(Address address);
    void delete(Address address);
    void resetDefaultAddress(Long userId);
}
