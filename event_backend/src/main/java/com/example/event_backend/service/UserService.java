package com.example.event_backend.service;


import com.example.event_backend.model.User;
import com.example.event_backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;


@Service
public class UserService implements UserDetailsService{

    @Autowired
    private UserRepository repo;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
    User user = repo.findByEmail(email);
    if (user != null) {
        String role = user.getRole().name();
        var springUser = org.springframework.security.core.userdetails.User.withUsername(user.getEmail())
            .password(user.getPassword())
            .roles(role)
            .build();

        return springUser;
    } 

    throw new UsernameNotFoundException("User not found with email: " + email);
}

    public User findByEmail(String email) {
        return repo.findByEmail(email); 
    }

    

}

