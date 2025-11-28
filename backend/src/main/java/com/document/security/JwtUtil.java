package com.document.security;

import com.auth0.jwk.*;
import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.document.aws.CognitoConfig;
import com.auth0.jwt.algorithms.Algorithm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.security.interfaces.RSAPublicKey;

@Component
public class JwtUtil {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

    private final CognitoConfig cognitoConfig;
    private final String clientId;

    public JwtUtil(CognitoConfig cognitoConfig,
                   @Value("${cognito.clientId}") String clientId) {
        this.cognitoConfig = cognitoConfig;
        this.clientId = clientId;
    }

    public String extractUserId(String token) throws Exception {
        logger.debug("Extracting userId from JWT token");
        return verifyToken(token).getClaim("sub").asString();
    }

    public DecodedJWT verifyToken(String token) throws Exception {
        logger.info("JWT verification started");

        DecodedJWT jwt = JWT.decode(token);
        logger.debug("Token decoded. kid: {}", jwt.getKeyId());

        JwkProvider provider = new UrlJwkProvider(new URL(cognitoConfig.getJwksUrl()));
        Jwk jwk = provider.get(jwt.getKeyId());

        logger.debug("JWKS key loaded successfully");

        Algorithm algorithm = Algorithm.RSA256((RSAPublicKey) jwk.getPublicKey(), null);

        try {
            algorithm.verify(jwt);
            logger.info("JWT signature verified");
        } catch (Exception e) {
            logger.error("Signature verification failed: {}", e.getMessage());
            throw new RuntimeException("Invalid token signature");
        }

        if (!jwt.getIssuer().equals(cognitoConfig.getIssuer())) {
            logger.error("Issuer mismatch: expected {}, found {}", cognitoConfig.getIssuer(), jwt.getIssuer());
            throw new RuntimeException("Invalid Issuer");
        }

        if (!jwt.getAudience().contains(clientId)) {
            logger.error("Audience mismatch: expected {}, found {}", clientId, jwt.getAudience());
            throw new RuntimeException("Invalid ClientId / Audience");
        }

        logger.info("JWT verified successfully!");
        return jwt;
    }
}
