package com.eduScale.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "edu-scale.jwt")
public class JwtProperties {

    /**
     * HMAC secret (UTF-8); must be at least 256 bits for HS256.
     */
    private String secret = "eduScaleDevSecretKeyMustBeAtLeast256BitsForHS256UseEnvInProd!!";

    private long accessExpirationMs = 900_000L;

    private int refreshExpirationDays = 30;
}
