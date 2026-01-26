package com.nearsplit.common.security;

import com.nearsplit.common.exception.ErrorCode;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Slf4j
@Component
public class JwtUtil {
    private final SecretKey secretKey;
    @Getter
    private final int expiration;

    public JwtUtil(@Value("${jwt.secret}") String secret, @Value("${jwt.expiration}") int expiretion) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expiration = expiretion;
    }

    public String generateToken(Long id) {
        Date now = new Date();
        Date expireDate = new Date(now.getTime() + expiration);
//        Claims claims = Jwts.claims().subject(String.valueOf(id)).build();

        return Jwts.builder()
                //.claims(claims)
                .subject(String.valueOf(id))
                //.claim("email", email)
                .issuedAt(now)
                .expiration(expireDate)
                .signWith(secretKey)
                .compact();
    }

    public String generateRefreshToken(Long id) {
        Date now = new Date();
        Date expireDate = new Date(now.getTime() + 604_800_000);    // 7일


        return Jwts.builder()
                .subject(String.valueOf(id))
                .issuedAt(now)
                .expiration(expireDate)
                .signWith(secretKey)
                .compact();
    }

    public String refreshToken(Long id) {
        return generateToken(id);
    }

    public Long getUserId(String token) {
        return Long.parseLong(
                Jwts.parser()
                        .verifyWith(secretKey)
                        .build()
                        .parseSignedClaims(token)
                        .getPayload()
                        .getSubject()
        );
    }


    public TokenStatus validToken(String token) {
        try {
            /*Jws<Claims> claimsJws = */
            Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token);

        } catch (ExpiredJwtException e) {
            log.error("만료된 토큰입니다: {}", e.getMessage());
            return TokenStatus.EXPIRED;
        } catch (SignatureException e) {
            log.error("잘못된 서명입니다: {}", e.getMessage());
            return TokenStatus.INVALID_SIGNATURE;
        } catch (MalformedJwtException e) {
            log.error("잘못된 토큰 형식입니다: {}", e.getMessage());
            return TokenStatus.MALFORMED;
        } catch (Exception e) {
            log.error("토큰 검증 실패: {}", e.getMessage());
            throw new RuntimeException("토큰 검증 실패", e);
        }
        return TokenStatus.VALID;
    }

}
