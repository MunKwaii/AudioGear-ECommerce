package vn.edu.ute.dao;

import vn.edu.ute.entity.User;
import java.util.Optional;

/**
 * Data Access Object Interface cho User entity
 */
public interface UserDAO {
    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);
    Optional<User> findByUsernameOrEmail(String identifier);
    Optional<User> findByPhoneNumber(String phoneNumber);
    Optional<User> findById(Long id);
    User save(User user);
    java.util.List<User> findAll();
    java.util.List<User> search(String keyword, vn.edu.ute.entity.enums.UserRole role, vn.edu.ute.entity.enums.UserStatus status);
    java.util.List<User> search(String keyword, vn.edu.ute.entity.enums.UserRole role, vn.edu.ute.entity.enums.UserStatus status, int offset, int limit);
    long countSearch(String keyword, vn.edu.ute.entity.enums.UserRole role, vn.edu.ute.entity.enums.UserStatus status);
}
