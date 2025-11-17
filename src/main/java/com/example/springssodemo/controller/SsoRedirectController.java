package com.example.springssodemo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SsoRedirectController {

    @GetMapping("/sso/saml")
    public String redirectToSaml() {
        // ✅ must match your registration name from application.properties
        return "redirect:/saml2/authenticate/sso-saml-demo";
    }
}
