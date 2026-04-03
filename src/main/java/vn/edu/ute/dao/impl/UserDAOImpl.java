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

    private static UserDAOImpl instance;

    public UserDAOImpl() {}

    public static synchronized UserDAOImpl getInstance() {
        if (instance == null) {
            instance = new UserDAOImpl();
        }
        return instance;
    }

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
    public Optional<User> findByPhoneNumber(String phoneNumber) {
        EntityManager em = DatabaseConfig.getEntityManager();
        try {
            User user = em.createQuery("SELECT u FROM User u WHERE u.phoneNumber = :phoneNumber", User.class)
                    .setParameter("phoneNumber", phoneNumber)
                    .getSingleResult();
            return Optional.of(user);
        } catch (NoResultException e) {
            return Optional.empty();
        } finally {
            DatabaseConfig.closeEntityManager();
        }
    }

    @Override
    public Optional<User> findById(Long id) {
        EntityManager em = DatabaseConfig.getEntityManager();
        try {
            User user = em.find(User.class, id);
            return Optional.ofNullable(user);
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

    @Override
    public java.util.List<User> findAll() {
        EntityManager em = DatabaseConfig.getEntityManager();
        try {
            return em.createQuery("SELECT u FROM User u ORDER BY u.createdAt DESC", User.class)
                    .getResultList();
        } finally {
            DatabaseConfig.closeEntityManager();
        }
    }

    @Override
    public java.util.List<User> search(String keyword, vn.edu.ute.entity.enums.UserRole role, vn.edu.ute.entity.enums.UserStatus status) {
        return search(keyword, role, status, 0, Integer.MAX_VALUE);
    }

    @Override
    public java.util.List<User> search(String keyword, vn.edu.ute.entity.enums.UserRole role, vn.edu.ute.entity.enums.UserStatus status, int offset, int limit) {
        EntityManager em = DatabaseConfig.getEntityManager();
        try {
            StringBuilder jpql = new StringBuilder("SELECT u FROM User u WHERE 1=1");
            if (keyword != null && !keyword.trim().isEmpty()) {
                jpql.append(" AND (LOWER(u.username) LIKE :kw OR LOWER(u.email) LIKE :kw OR LOWER(u.fullName) LIKE :kw)");
            }
            if (role != null) {
                jpql.append(" AND u.role = :role");
            }
            if (status != null) {
                jpql.append(" AND u.status = :status");
            }
            jpql.append(" ORDER BY u.createdAt DESC");

            jakarta.persistence.TypedQuery<User> query = em.createQuery(jpql.toString(), User.class);
            if (keyword != null && !keyword.trim().isEmpty()) {
                query.setParameter("kw", "%" + keyword.trim().toLowerCase() + "%");
            }
            if (role != null) {
                query.setParameter("role", role);
            }
            if (status != null) {
                query.setParameter("status", status);
            }
            query.setFirstResult(offset);
            query.setMaxResults(limit);
            return query.getResultList();
        } finally {
            DatabaseConfig.closeEntityManager();
        }
    }

    @Override
    public long countSearch(String keyword, vn.edu.ute.entity.enums.UserRole role, vn.edu.ute.entity.enums.UserStatus status) {
        EntityManager em = DatabaseConfig.getEntityManager();
        try {
            StringBuilder jpql = new StringBuilder("SELECT COUNT(u) FROM User u WHERE 1=1");
            if (keyword != null && !keyword.trim().isEmpty()) {
                jpql.append(" AND (LOWER(u.username) LIKE :kw OR LOWER(u.email) LIKE :kw OR LOWER(u.fullName) LIKE :kw)");
            }
            if (role != null) {
                jpql.append(" AND u.role = :role");
            }
            if (status != null) {
                jpql.append(" AND u.status = :status");
            }

            jakarta.persistence.TypedQuery<Long> query = em.createQuery(jpql.toString(), Long.class);
            if (keyword != null && !keyword.trim().isEmpty()) {
                query.setParameter("kw", "%" + keyword.trim().toLowerCase() + "%");
            }
            if (role != null) {
                query.setParameter("role", role);
            }
            if (status != null) {
                query.setParameter("status", status);
            }
            return query.getSingleResult();
        } finally {
            DatabaseConfig.closeEntityManager();
        }
    }
}
