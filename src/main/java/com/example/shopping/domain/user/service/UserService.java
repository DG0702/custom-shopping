package com.example.shopping.domain.user.service;

import com.example.shopping.domain.common.dto.AuthUser;
import com.example.shopping.domain.user.dto.ProfileResponseDto;
import com.example.shopping.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserQueryService userQueryService;

    public ProfileResponseDto profile(AuthUser Authuser) {

        User user = userQueryService.findByIdOrElseThrow(Authuser.getId());

        return new ProfileResponseDto(user);
    }
}
