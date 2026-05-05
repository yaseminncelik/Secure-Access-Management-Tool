package com.secureaccess.repository;

import com.secureaccess.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * UserRepository - Database işlemleri (Abstraction)
 * Spring Data JPA sayesinde temel CRUD işlemleri otomatik olarak sağlanır
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Optional<User> findByUsernameAndEmail(String username, String email);
    Optional<User> findByUsernameAndPassword(String username, String password);
}
