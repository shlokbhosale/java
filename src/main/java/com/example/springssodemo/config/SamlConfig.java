package com.example.springssodemo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.saml2.provider.service.registration.*;

@Configuration
public class SamlConfig {

    @Bean
    public RelyingPartyRegistrationRepository relyingPartyRegistrationRepository() {
        // Replace this URL with your actual IDP metadata endpoint
        RelyingPartyRegistration registration = RelyingPartyRegistrations
                .fromMetadataLocation("https://patidar.xecurify.com/moas/metadata/saml/379431/432750")
                .registrationId("sso-saml-demo")
                .build();

        return new InMemoryRelyingPartyRegistrationRepository(registration);
    }
}
