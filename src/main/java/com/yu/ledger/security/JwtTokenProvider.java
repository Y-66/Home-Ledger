package com.yu.ledger.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * JWT令牌提供者
 */
@Component
public class JwtTokenProvider {
    
    @Value("${app.jwt.secret:defaultSecretKey}")
    private String jwtSecret;
    
    @Value("${app.jwt.expiration:86400000}")
    private int jwtExpirationMs;
    
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }
    
    public String generateToken(Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        
        return Jwts.builder()
                .setSubject(userPrincipal.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs))
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }
    
    public String getUsernameFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }
    
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (SecurityException ex) {
            // 无效的JWT签名
            return false;
        } catch (MalformedJwtException ex) {
            // 无效的JWT令牌
            return false;
        } catch (ExpiredJwtException ex) {
            // 过期的JWT令牌
            return false;
        } catch (UnsupportedJwtException ex) {
            // 不支持的JWT令牌
            return false;
        } catch (IllegalArgumentException ex) {
            // JWT声明字符串为空
            return false;
        }
    }
} 