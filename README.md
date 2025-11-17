# Spring SSO JWT Demo (minimal)

Features:
- Login page and registration (stores users in PostgreSQL `newdemo`)
- "Login with SSO (JWT)" button that redirects to configured IDP
- Callback endpoint `/sso/jwt/callback` parses `id_token` (JWT), provisions user, and redirects to `/home`
- Inline CSS and three buttons on login page: JWT, SAML (placeholder), OIDC (placeholder)

Important:
- Set the redirect URL in your IDP to: `http://localhost:8080/sso/jwt/callback`
- The example does **not** verify JWT signatures (for demonstration). In production you *must* verify signatures and claims (aud, iss, exp).
