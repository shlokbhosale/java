package com.example.springssodemo.controller;

import com.example.springssodemo.model.User;
import com.example.springssodemo.repo.UserRepository;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.jwt.JWTClaimsSet;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpSession;
import java.text.ParseException;
import java.util.Optional;

@Controller
public class SsoController {

    private final UserRepository userRepository;

    @Value("${sso.client-id:}")
    private String ssoClientId;

    @Value("${sso.idp.authorize:}")
    private String ssoAuthorizeUrl;

    public SsoController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // Redirect user to IdP authorize endpoint for JWT-based SSO (IdP should return id_token or token)
    @GetMapping("/sso/login")
    public String ssoLogin() {
        if (ssoAuthorizeUrl == null || ssoAuthorizeUrl.isBlank()) {
            // fallback to home if not configured
            return "redirect:/login";
        }
        // build redirect URL (IdP parameter names may vary; adjust if your IdP expects different params)
        String redirect = ssoAuthorizeUrl + "?client_id=" + ssoClientId + "&response_type=id_token&redirect_uri=http://localhost:8080/sso/jwt/callback&scope=openid email";
        return "redirect:" + redirect;
    }

    // Callback endpoint used by JWT-based SSO (IdP should redirect here with id_token or token)
    @GetMapping("/sso/jwt/callback")
    public String ssoJwtCallback(@RequestParam(required = false) String id_token,
                                 @RequestParam(required = false) String token,
                                 HttpSession session) throws ParseException {

        String jwtRaw = id_token != null ? id_token : token;
        if (jwtRaw == null) {
            return "redirect:/login";
        }

        SignedJWT signedJWT = SignedJWT.parse(jwtRaw);
        JWTClaimsSet claims = signedJWT.getJWTClaimsSet();

        // try common claim names for username/email
        String username = null;
        if (claims.getStringClaim("preferred_username") != null) username = claims.getStringClaim("preferred_username");
        if (username == null && claims.getStringClaim("email") != null) username = claims.getStringClaim("email");
        if (username == null && claims.getSubject() != null) username = claims.getSubject();
        if (username == null) username = "sso-user";

        String email = claims.getStringClaim("email");

        // provision user (JIT provisioning)
        Optional<User> opt = userRepository.findByUsername(username);
        if (opt.isEmpty()) {
            User u = new User(username, "<sso>", email == null ? username : email);
            userRepository.save(u);
        }
        session.setAttribute("username", username);
        return "redirect:/home";
    }
}
