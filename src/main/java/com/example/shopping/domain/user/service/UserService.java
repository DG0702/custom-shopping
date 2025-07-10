package com.example.shopping.domain.user.service;

import com.example.shopping.config.JwtUtil;
import com.example.shopping.domain.auth.dto.response.LoginResponseDto;
import com.example.shopping.domain.auth.service.AuthService;
import com.example.shopping.domain.common.dto.AuthUser;
import com.example.shopping.domain.common.exception.CustomException;
import com.example.shopping.domain.common.exception.ExceptionCode;
import com.example.shopping.domain.user.dto.ChangeUserRoleRequestDto;
import com.example.shopping.domain.user.dto.ProfileResponseDto;
import com.example.shopping.domain.user.entity.User;
import com.example.shopping.domain.user.enums.UserRole;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserQueryService userQueryService;
    private final JwtUtil jwtUtil;
    private final AuthService authService;

    public ProfileResponseDto profile(AuthUser Authuser) {

        User user = userQueryService.findByIdOrElseThrow(Authuser.getId());

        return new ProfileResponseDto(user);
    }

    public LoginResponseDto changeRole(String bearerAccessToken, ChangeUserRoleRequestDto request){

        UserRole newUserRole = UserRole.of(request.getUserRole());

        String accessToken = jwtUtil.substringToken(bearerAccessToken);

        Claims claims = jwtUtil.extractClaims(accessToken);
        Long userId = Long.parseLong(claims.getSubject());
        String email = claims.get("email", String.class);
        UserRole userRole = UserRole.valueOf(claims.get("userRole", String.class));

        if(userRole.equals(newUserRole)){
            throw new CustomException(ExceptionCode.INVALID_USER_ROLE, "요청한 사용자 권한이 현재 권한과 동일합니다.");
        }

        authService.addToBlacklist(accessToken);

        return authService.createNewTokensAndSaveRefresh(userId,email,newUserRole);

    }

}
