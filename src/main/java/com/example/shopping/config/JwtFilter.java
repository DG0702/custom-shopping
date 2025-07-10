package com.example.shopping.config;

import com.example.shopping.domain.common.dto.AuthUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
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

@Slf4j
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final RedisTemplate<String, String> redisTemplate;
    private final JwtUtil jwtUtil;

    @Override
    public void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String url = httpRequest.getRequestURI();
        request.setAttribute("URI", url);

        if (url.startsWith("/auth/register") || url.startsWith("/auth/login") || url.startsWith("/auth/refresh")) {
            chain.doFilter(request, response);
            return;
        }

        String bearerJwt = httpRequest.getHeader("Authorization");

        if (bearerJwt == null) {
            // 토큰이 없는 경우 400을 반환합니다.
            request.setAttribute("exceptionMessage", "JWT 토큰이 필요합니다.");
            throw new InsufficientAuthenticationException("JWT 토큰이 필요합니다.");
        }

        String jwt = jwtUtil.substringToken(bearerJwt);

        //로그아웃 후 블랙리스트에 등록된 토큰인지 검사
        String blackListToken = "blacklist : " + jwt;
        if(redisTemplate.hasKey(blackListToken)){
            request.setAttribute("exceptionMessage", "로그아웃한 유저입니다.");
            throw new InsufficientAuthenticationException("로그아웃한 유저입니다.");
        }

        try {
            // JWT 유효성 검사와 claims 추출
            Claims claims = jwtUtil.extractClaims(jwt);
            if (claims == null) {
                request.setAttribute("exceptionMessage", "잘못된 JWT 토큰입니다.");
                throw new BadCredentialsException("잘못된 JWT 토큰입니다.");
            }

            Long userId = Long.parseLong(claims.getSubject());
            String email = claims.get("email", String.class);
            UserRole userRole = UserRole.valueOf(claims.get("userRole", String.class));

            AuthUser authUser = new AuthUser(userId, email, userRole);
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    authUser, null, List.of(new SimpleGrantedAuthority("ROLE_" + userRole.name())));

            SecurityContextHolder.getContext().setAuthentication(authentication);

            chain.doFilter(request, response);
        }
        catch (SecurityException | MalformedJwtException e) {
            logAndSend(request, "Invalid JWT token", "유효하지 않는 JWT 서명입니다.", e);
        }
        catch (ExpiredJwtException e) {
            logAndSend(request, "Expired JWT token", "만료된 JWT 토큰입니다.", e);
        }
        catch (UnsupportedJwtException e) {
            logAndSend(request, "Unsupported JWT token" , "지원되지 않는 JWT 토큰입니다.", e);
        }
        catch (Exception e) {
            log.error("Internal server error", e);
            throw new InsufficientAuthenticationException("Exception e");
        }
    }

    private void logAndSend(HttpServletRequest request, String logMessage, String message, Exception e){
        log.error("{} : {}", logMessage, e.getMessage());
        request.setAttribute("exceptionMessage", message);
        throw new BadCredentialsException(message);
    }
}