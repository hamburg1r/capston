package com.document.security;



import com.auth0.jwk.*;
import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.document.aws.CognitoConfig;
import com.auth0.jwt.algorithms.Algorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.security.interfaces.RSAPublicKey;

@Component
public class JwtUtil {

    private final CognitoConfig cognitoConfig;
    private final String clientId;

    public JwtUtil(CognitoConfig cognitoConfig,
                   @Value("${cognito.clientId}") String clientId) {
        this.cognitoConfig = cognitoConfig;
        this.clientId = clientId;
    }

    public String extractUserId(String token) throws Exception {
        return verifyToken(token).getClaim("sub").asString();
    }

    public DecodedJWT verifyToken(String token) throws Exception {

        DecodedJWT jwt = JWT.decode(token);

        // Load JWKS
        JwkProvider provider = new UrlJwkProvider(new URL(cognitoConfig.getJwksUrl()));
        Jwk jwk = provider.get(jwt.getKeyId());

        Algorithm algorithm = Algorithm.RSA256((RSAPublicKey) jwk.getPublicKey(), null);

        // Signature verify
        algorithm.verify(jwt);

        // Issuer verify
        if (!jwt.getIssuer().equals(cognitoConfig.getIssuer())) {
            throw new RuntimeException("Invalid Issuer");
        }

        // Audience verify
        if (!jwt.getAudience().contains(clientId)) {
            throw new RuntimeException("Invalid ClientId / Audience");
        }

        return jwt;
    }
}
