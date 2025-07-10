package com.example.shopping.domain.user.service;

import com.example.shopping.domain.common.exception.CustomException;
import com.example.shopping.domain.common.exception.ExceptionCode;
import com.example.shopping.domain.user.entity.User;
import com.example.shopping.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserQueryService {
    private final UserRepository userRepository;

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public User findByEmailOrElseThrow(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ExceptionCode.EMAIL_NOT_FOUND));
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public User findByIdOrElseThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new CustomException(ExceptionCode.USER_NOT_FOUND));
    }
}