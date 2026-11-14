package com.pinkdoor.security;

import com.pinkdoor.config.properties.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.io.DecodingException;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

/**
 * JWT 的生成与校验。
 */
@Component
public class JwtTokenProvider {

    private static final String CLAIM_NICKNAME = "nickname";
    private static final String CLAIM_COUPLE_ID = "couple_id";
    private static final int MIN_SECRET_BYTES = 32;

    private final SecretKey secretKey;
    private final long expireHours;

    public JwtTokenProvider(JwtProperties properties) {
        if (!StringUtils.hasText(properties.getSecret())) {
            throw new IllegalStateException("pink-door.jwt.secret must be configured");
        }
        byte[] keyBytes;
        try {
            keyBytes = Decoders.BASE64.decode(properties.getSecret());
        } catch (DecodingException ex) {
            throw new IllegalStateException("pink-door.jwt.secret must be valid Base64", ex);
        }
        if (keyBytes.length < MIN_SECRET_BYTES) {
            throw new IllegalStateException("pink-door.jwt.secret must decode to at least 32 bytes");
        }
        if (properties.getExpireHours() <= 0) {
            throw new IllegalStateException("pink-door.jwt.expire-hours must be greater than zero");
        }
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
        this.expireHours = properties.getExpireHours();
    }

    public String generateToken(Long userId, String nickname, Long coupleId) {
        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plus(expireHours, ChronoUnit.HOURS);
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim(CLAIM_NICKNAME, nickname)
                // 租户标识随 token 下发，后续每个请求都能直接拿到空间 ID。
                .claim(CLAIM_COUPLE_ID, coupleId)
                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(expiresAt))
                .signWith(secretKey)
                .compact();
    }

    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public long getExpireSeconds() {
        return expireHours * 60 * 60;
    }
}
