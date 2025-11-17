package com.example.springssodemo.controller;

import com.example.springssodemo.model.User;
import com.example.springssodemo.repo.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // ✅ Redirect root to login
    @GetMapping("/")
    public String rootRedirect() {
        return "redirect:/login";
    }

    // ✅ Show registration form
    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    // ✅ Handle registration form submission
    @PostMapping("/register")
    public String doRegister(@RequestParam String username,
                             @RequestParam String password,
                             @RequestParam String email,
                             Model model) {

        // Check if username already exists
        if (userRepository.findByUsername(username).isPresent()) {
            model.addAttribute("error", "⚠️ Username already exists!");
            return "register";
        }

        // ✅ Encode password properly
        String encodedPassword = passwordEncoder.encode(password);

        // ✅ Create new user with default role USER
        User newUser = new User();
        newUser.setUsername(username);
        newUser.setPassword(encodedPassword);
        newUser.setEmail(email);
        newUser.setRole("USER"); // plain, DB me "USER" store hoga

        userRepository.save(newUser);

        model.addAttribute("message", "✅ Registered successfully! Please login.");
        return "login";
    }
}
