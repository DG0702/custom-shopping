package com.example.shopping.config;

import com.example.shopping.domain.common.dto.AuthUser;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.example.shopping.domain.user.enums.UserRole;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

import javax.security.sasl.AuthenticationException;

@Slf4j
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final RedisTemplate<String, String> redisTemplate;
    private final JwtUtil jwtUtil;

    @Override
    public void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws
        IOException,
        ServletException {

        String token = resolveToken(request);
        String bearerToken = request.getHeader("Authorization");

        // 토큰이 없는 요청(회원가입, 로그인, OAuth) → 다음 필터로 이동
        if (bearerToken == null) {
            chain.doFilter(request, response);
            return;
        }

        // 로그아웃 유저 확인
        String blackListToken = "blacklist : " + token;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(blackListToken))) {
            request.setAttribute("exceptionMessage", "logout user → please login");
            throw new InsufficientAuthenticationException("logout user → please login");
        }

        // 토큰 유효 검증
        try {
            if (token != null && jwtUtil.validateToken(token)) {
                Claims claims = jwtUtil.extractClaims(token);

                Long userId = Long.parseLong(claims.getSubject());
                UserRole userRole = UserRole.valueOf(claims.get("userRole", String.class));

                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    userId, null, List.of(new SimpleGrantedAuthority("ROLE_" + userRole.name())));

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }
        catch(JwtException e) {
            log.error("Jwt validation failed {}", e.getMessage());
            throw new AuthenticationException("Jwt validation failed {}",e);
        }

        chain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");

        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
