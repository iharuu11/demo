package com.example.demo.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtTokenProvider jwtTokenProvider;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        // 这个过滤器会在每个请求进来时执行一次：
        // 目标：从请求头 Authorization 里解析 JWT，解析成功就把“当前登录用户”写入 Spring Security 上下文
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
            // Authorization: Bearer <token>
            String token = authHeader.substring(7);
            try {
                // 解析 token 得到 claims（token 里的用户信息）
                Claims claims = jwtTokenProvider.parseToken(token);
                String username = claims.getSubject();
                String role = claims.get("role", String.class);

                // authorities 就是“权限点/角色”，用来支持 @PreAuthorize 等权限注解
                List<SimpleGrantedAuthority> authorities = new ArrayList<>();
                authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
                Object perms = claims.get("perms");
                if (perms instanceof List<?> permissionList) {
                    for (Object permission : permissionList) {
                        if (permission instanceof String code && !code.isBlank()) {
                            authorities.add(new SimpleGrantedAuthority(code));
                        }
                    }
                }

                // 写入当前请求的登录态（只对本次请求有效，因为是无状态）
                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                        username, null, authorities);
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            } catch (Exception ignored) {
                // token 解析失败（过期/伪造/格式不对等）：清空上下文，让后续鉴权按“未登录”处理
                SecurityContextHolder.clearContext();
            }
        }
        filterChain.doFilter(request, response);
    }
}
