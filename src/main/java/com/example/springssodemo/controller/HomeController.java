package com.example.springssodemo.controller;

import com.example.springssodemo.model.User;
import com.example.springssodemo.repo.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.saml2.provider.service.authentication.Saml2AuthenticatedPrincipal;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpSession;
import java.util.Map;
import java.util.Optional;

@Controller
public class HomeController {

    private final UserRepository userRepository;

    public HomeController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/home")
    public String home(HttpSession session, Model model) {
        Object u = session.getAttribute("username");
        if (u == null) {
            // try to obtain username from Spring Security context (covers SAML login)
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
                String username = null;
                Object principal = auth.getPrincipal();
                if (principal instanceof Saml2AuthenticatedPrincipal) {
                    Saml2AuthenticatedPrincipal p = (Saml2AuthenticatedPrincipal) principal;
                    // common possible attribute names
                    if (p.getFirstAttribute("email") != null) username = p.getFirstAttribute("email");
                    if (username == null && p.getFirstAttribute("username") != null) username = p.getFirstAttribute("username");
                    if (username == null && p.getFirstAttribute("user") != null) username = p.getFirstAttribute("user");
                    if (username == null) username = p.getName();
                }
                // ✅ Added block starts here
                else if (auth instanceof OAuth2AuthenticationToken oauth) {
                    Map<String,Object> attr = oauth.getPrincipal().getAttributes();
                    if (attr.get("preferred_username") != null) username = attr.get("preferred_username").toString();
                    else if (attr.get("name") != null) username = attr.get("name").toString();
                    else if (attr.get("email") != null) username = attr.get("email").toString().split("@")[0];
                    else if (attr.get("sub") != null) username = attr.get("sub").toString();
                }
                // ✅ Added block ends here
                else {
                    username = auth.getName();
                }

                if (username != null) {
                    // provision user if not exists
                    Optional<User> opt = userRepository.findByUsername(username);
                    if (opt.isEmpty()) {
                        User newU = new User(username, "<sso>", username);
                        userRepository.save(newU);
                    }
                    session.setAttribute("username", username);
                    u = username;
                }
            }
        }

        if (u == null) return "redirect:/login";
        model.addAttribute("username", u.toString());
        return "home";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}
