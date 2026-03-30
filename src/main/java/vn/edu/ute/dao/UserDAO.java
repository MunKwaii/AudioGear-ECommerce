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
    User save(User user);
}
