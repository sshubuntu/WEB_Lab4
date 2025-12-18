package com.sshubuntu.weblab4.service;

import com.sshubuntu.weblab4.entity.UserAccount;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.mindrot.jbcrypt.BCrypt;

import java.util.Optional;

@Stateless
public class AuthService {

    @PersistenceContext(unitName = "PointsPU")
    private EntityManager entityManager;

    public Optional<UserAccount> findByUsername(String username) {
        if (username == null || username.isBlank()) {
            return Optional.empty();
        }
        return entityManager.createQuery("SELECT u FROM UserAccount u WHERE LOWER(u.username) = :username", UserAccount.class)
                .setParameter("username", username.trim().toLowerCase())
                .getResultStream()
                .findFirst();
    }

    public UserAccount createUser(String username, String rawPassword) {
        String hashedPassword = hashPassword(rawPassword);
        String normalizedUsername = username.trim().toLowerCase();

        entityManager.createNativeQuery(
            "INSERT INTO users (id, username, password_hash, created_at) " +
            "VALUES (nextval('user_seq'), :username, :password, :createdAt)"
        )
        .setParameter("username", normalizedUsername)
        .setParameter("password", hashedPassword)
        .setParameter("createdAt", java.time.LocalDateTime.now())
        .executeUpdate();
        
        entityManager.flush();
        
        return findByUsername(normalizedUsername).orElseThrow(() ->
            new RuntimeException("Не удалось создать пользователя"));
    }

    public Optional<UserAccount> findById(Long id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(entityManager.find(UserAccount.class, id));
    }

    public boolean passwordMatches(UserAccount user, String rawPassword) {
        if (user == null || rawPassword == null) return false;
        try {
            return BCrypt.checkpw(rawPassword, user.getPasswordHash());
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public String hashPassword(String rawPassword) {
        return BCrypt.hashpw(rawPassword, BCrypt.gensalt(10));
    }
}


