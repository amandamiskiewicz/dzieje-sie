package com.example.event_backend.repository;

import com.example.event_backend.model.Role;
import com.example.event_backend.model.User;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    public User findByEmail(String email);

    Page<User> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
        String firstName, String lastName, String email, Pageable pageable);
        
    Page<User> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseOrEmailContainingIgnoreCaseAndRole(
            String firstName, String lastName, String email, String role, Pageable pageable);
            
    Page<User> findByRole(String role, Pageable pageable);

    Page<User> findByRoleAndFirstNameContainingIgnoreCaseOrRoleAndLastNameContainingIgnoreCaseOrRoleAndEmailContainingIgnoreCase(
    String role1, String firstName, 
    String role2, String lastName, 
    String role3, String email, 
    Pageable pageable);

    Page<User> findByRoleAndFirstNameContainingIgnoreCaseOrRoleAndLastNameContainingIgnoreCaseOrRoleAndEmailContainingIgnoreCase(
    Role role1, String firstName, 
    Role role2, String lastName, 
    Role role3, String email, 
    Pageable pageable);

    Page<User> findByRole(Role role, Pageable pageable);

    long countByRole(Role role);

    @Query("SELECT COUNT(u) FROM User u")
    long countAllUsers();

}
    
