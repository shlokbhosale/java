package com.example.springssodemo.service;

import com.example.springssodemo.model.User;
import com.example.springssodemo.repo.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        Optional<User> opt = userRepository.findByUsername(usernameOrEmail);
        User user = opt.orElseGet(() ->
                userRepository.findAll().stream()
                        .filter(u -> u.getEmail() != null && u.getEmail().equalsIgnoreCase(usernameOrEmail))
                        .findFirst()
                        .orElse(null)
        );

        if (user == null) {
            throw new UsernameNotFoundException("User not found: " + usernameOrEmail);
        }

        String role = user.getRole();
        if (role == null || role.isEmpty()) {
            role = "USER";
        }

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .roles(role.toUpperCase()) // ADMIN or USER
                .build();
    }
}
