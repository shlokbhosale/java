package com.example.springssodemo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistrationRepository;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           RelyingPartyRegistrationRepository relyingPartyRegistrationRepository) throws Exception {

        http
                // 🔐 Authorization rules
                .authorizeHttpRequests(auth -> auth
                        // ✅ Role-based access control
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/user/**").hasAnyRole("USER", "ADMIN")

                        // ✅ Public endpoints (accessible without login)
                        .requestMatchers("/", "/login", "/register", "/doLogin",
                                "/sso/**", "/jwt/**", "/oidc/**", "/home", "/error",
                                "/css/**", "/js/**").permitAll()

                        // ✅ Any other route needs authentication
                        .anyRequest().authenticated()
                )

                // 🧩 Normal form login configuration
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/redirect", true)
                        .failureUrl("/login?error")
                        .permitAll()
                )

                // 🔑 SAML2 SSO configuration
                .saml2Login(saml2 -> saml2
                        .loginPage("/login")
                        .defaultSuccessUrl("/home", true)
                        .relyingPartyRegistrationRepository(relyingPartyRegistrationRepository)
                )

                // 🌐 OIDC (OAuth2) SSO configuration
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/login")
                        .defaultSuccessUrl("/home", true)
                )

                // 🔓 Logout setup
                .logout(logout -> logout
                        .logoutSuccessUrl("/login?logout")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .permitAll()
                )

                // ⚙️ Disable CSRF for APIs (optional)
                .csrf(csrf -> csrf.disable());

        return http.build();
    }

    // 🔒 Password encoder for hashing and matching
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
