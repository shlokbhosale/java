package com.example.springssodemo.controller;

import com.example.springssodemo.model.User;
import com.example.springssodemo.repo.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class LoginController {

    private final UserRepository userRepository;

    public LoginController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    // debug endpoint to see authorities
    @GetMapping("/whoami")
    @ResponseBody
    public String whoami(Authentication auth) {
        if (auth == null) return "Not authenticated";
        return "User: " + auth.getName() + " | Authorities: " + auth.getAuthorities();
    }

    @GetMapping("/redirect")
    public String redirectAfterLogin(Authentication authentication) {
        if (authentication == null) {
            return "redirect:/login";
        }

        String username = authentication.getName();
        String authorities = authentication.getAuthorities().toString();

        System.out.println("DEBUG → Logged user: " + username);
        System.out.println("DEBUG → Authorities: " + authorities);

        // 1) if authorities contain admin
        if (authorities.contains("ROLE_ADMIN")) {
            System.out.println("DEBUG → Redirecting to admin/dashboard via authorities");
            return "redirect:/admin/dashboard";
        }

        // 2) fallback to DB by username or email
        User user = userRepository.findByUsername(username).orElse(null);

        if (user == null) {
            user = userRepository.findAll().stream()
                    .filter(u -> u.getEmail() != null && u.getEmail().equalsIgnoreCase(username))
                    .findFirst().orElse(null);
        }

        if (user != null) {
            System.out.println("DEBUG → Found user in DB: " + user.getUsername() + " | Role: " + user.getRole());
            String dbRole = user.getRole();
            if (dbRole != null && (dbRole.equalsIgnoreCase("ROLE_ADMIN") || dbRole.equalsIgnoreCase("ADMIN"))) {
                System.out.println("DEBUG → Redirecting to admin/dashboard via DB role");
                return "redirect:/admin/dashboard";
            } else if (dbRole != null && (dbRole.equalsIgnoreCase("ROLE_USER") || dbRole.equalsIgnoreCase("USER"))) {
                return "redirect:/user/home";
            }
        }

        System.out.println("DEBUG → No role found, redirecting to /home");
        return "redirect:/home";
    }
}