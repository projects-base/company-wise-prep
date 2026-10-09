**Short answer:** OAuth 2.0 is an authorization framework: it lets a client get an access token from an authorization server (Keycloak, Okta, Azure AD) to call a resource server on a user's behalf, without ever seeing the user's password. A JWT is a token format: a Base64URL-encoded header, payload of claims, and a signature. Services verify the signature with the issuer's public key and check expiry, so they can authorize a request without calling a session store. OAuth often uses JWTs as access tokens, but they are different things.

## Explanation

**OAuth 2.0 roles:** resource owner (user), client (the app), authorization server (issues tokens), resource server (your API).

**Main flows (grants):**

- **Authorization Code + PKCE:** for web and mobile apps. The user logs in at the auth server, the app gets a short-lived code, and exchanges it (with the PKCE verifier) for tokens. PKCE is now recommended for all clients.
- **Client Credentials:** service-to-service, no user. The service authenticates with its own id/secret and gets a token.
- **Refresh token:** gets a new access token when the short-lived one expires.
- Implicit and Password grants are discouraged and are dropped in the OAuth 2.1 draft.

**OpenID Connect (OIDC)** adds authentication on top of OAuth: an **ID token** (a JWT) that says who the user is. OAuth alone answers "what may this client access", not "who is the user".

**JWT structure:** `header.payload.signature`.

- Header: `{"alg":"RS256","kid":"key-1","typ":"JWT"}`
- Payload claims: `iss` (issuer), `sub` (subject), `aud` (audience), `exp` (expiry), `iat`, plus custom ones like `scope` or `roles`.
- Signature: HMAC (HS256, shared secret) or RSA/ECDSA (RS256/ES256, private key signs, public key verifies). Resource servers fetch public keys from the issuer's JWKS endpoint.

The payload is **signed, not encrypted**. Anyone can decode it, so never put secrets in it.

## Example

```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: https://auth.example.com/realms/shop
```

```java
@Bean
SecurityFilterChain api(HttpSecurity http) throws Exception {
    http.authorizeHttpRequests(a -> a
            .requestMatchers("/actuator/health").permitAll()
            .requestMatchers(HttpMethod.POST, "/orders/**").hasAuthority("SCOPE_orders.write")
            .anyRequest().authenticated())
        .oauth2ResourceServer(o -> o.jwt(Customizer.withDefaults()))
        .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
    return http.build();
}
```

Spring's `BearerTokenAuthenticationFilter` reads `Authorization: Bearer <token>`, the `JwtDecoder` checks signature, `exp` and `iss`, and the scopes become `SCOPE_` authorities.

## Pitfalls and follow-ups

- **How do you revoke a JWT?** You can't directly; it's valid until `exp`. Keep access tokens short (minutes), use refresh tokens, and keep a deny-list of token ids (`jti`) if you need instant logout. Opaque tokens with introspection are the alternative.
- **Where to store tokens in a browser?** An HttpOnly, Secure, SameSite cookie protects against XSS reading it; `localStorage` is readable by any injected script. Many teams use a backend-for-frontend so the browser never holds the token.
- **Validate `aud` and `iss`,** reject `alg: none`, and pin the expected algorithm.
- **Authentication vs authorization:** who you are (401 when missing) vs what you may do (403 when denied).
- **Docker and sidecars (asked together):** an image is a read-only template built in layers from a Dockerfile; a container is a running instance of an image with its own writable layer. A sidecar is a helper container in the same pod as the app (shared network and lifecycle) that adds cross-cutting features: a service-mesh proxy for mTLS and retries, a log shipper, or an auth proxy.

Deeper: [D7 · Spring Security: filter chain and JWT](../academy/lessons/D7.md).
