package vn.edu.ute.dao.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import vn.edu.ute.config.DatabaseConfig;
import vn.edu.ute.dao.UserDAO;
import vn.edu.ute.entity.User;

import java.util.Optional;

/**
 * Triển khai DAO thực tế cho User entity
 */
public class UserDAOImpl implements UserDAO {

    @Override
    public Optional<User> findByEmail(String email) {
        EntityManager em = DatabaseConfig.getEntityManager();
        try {
            User user = em.createQuery("SELECT u FROM User u WHERE u.email = :email", User.class)
                    .setParameter("email", email)
                    .getSingleResult();
            return Optional.of(user);
        } catch (NoResultException e) {
            return Optional.empty();
        } finally {
            DatabaseConfig.closeEntityManager();
        }
    }

    @Override
    public Optional<User> findByUsername(String username) {
        EntityManager em = DatabaseConfig.getEntityManager();
        try {
            User user = em.createQuery("SELECT u FROM User u WHERE u.username = :username", User.class)
                    .setParameter("username", username)
                    .getSingleResult();
            return Optional.of(user);
        } catch (NoResultException e) {
            return Optional.empty();
        } finally {
            DatabaseConfig.closeEntityManager();
        }
    }
    
    @Override
    public Optional<User> findByUsernameOrEmail(String identifier) {
        EntityManager em = DatabaseConfig.getEntityManager();
        try {
            User user = em.createQuery("SELECT u FROM User u WHERE u.username = :id OR u.email = :id", User.class)
                    .setParameter("id", identifier)
                    .getSingleResult();
            return Optional.of(user);
        } catch (NoResultException e) {
            return Optional.empty();
        } finally {
            DatabaseConfig.closeEntityManager();
        }
    }

    @Override
    public User save(User user) {
        EntityManager em = DatabaseConfig.getEntityManager();
        try {
            DatabaseConfig.beginTransaction();
            if (user.getId() == null) {
                em.persist(user);
            } else {
                user = em.merge(user);
            }
            DatabaseConfig.commitTransaction();
            return user;
        } catch (Exception e) {
            DatabaseConfig.rollbackTransaction();
            throw new RuntimeException("Lỗi khi lưu User: " + e.getMessage(), e);
        } finally {
            DatabaseConfig.closeEntityManager();
        }
    }
}
