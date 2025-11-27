package com.document.aws;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CognitoConfig {

    @Value("${cognito.region}")
    private String region;

    @Value("${cognito.userPoolId}")
    private String userPoolId;

    public String getIssuer() {
        return "https://cognito-idp." + region + ".amazonaws.com/" + userPoolId;
    }

    public String getJwksUrl() {
        return getIssuer() + "/.well-known/jwks.json";
    }
}
