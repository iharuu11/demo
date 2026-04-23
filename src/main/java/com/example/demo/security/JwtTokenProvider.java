package com.example.demo.security;

import com.example.demo.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {
    private final JwtProperties jwtProperties;
    private final SecretKey key;

    public JwtTokenProvider(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        // JWT 的签名密钥（HMAC-SHA）：
        // - secret 配在配置文件里（base64）
        // - 后端用它来签名 token；解析 token 时也用它来校验签名是否可信
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtProperties.secret()));
    }

    public String createToken(String username, String role, List<String> permissions) {
        // 生成 JWT：
        // - subject: 用户名（谁登录了）
        // - role/perms: 角色与权限点（后端鉴权用）
        // - issuedAt/expiration: 签发时间与过期时间（过期后需要重新登录）
        Instant now = Instant.now();
        Instant expireAt = now.plusSeconds(jwtProperties.expireSeconds());
        return Jwts.builder()
                .subject(username)
                .claim("role", role)
                .claim("perms", permissions)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expireAt))
                .signWith(key)
                .compact();
    }

    public Claims parseToken(String token) {
        // 解析并校验 JWT（签名校验 + 过期校验等）
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }
}
