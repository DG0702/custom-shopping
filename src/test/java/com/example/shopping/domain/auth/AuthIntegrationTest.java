package com.example.shopping.domain.auth;

import com.example.shopping.global.jwt.JwtUtil;
import com.example.shopping.domain.auth.dto.request.LoginRequest;
import com.example.shopping.domain.auth.dto.request.SignupRequest;
import com.example.shopping.domain.auth.dto.request.WithdrawRequest;
import com.example.shopping.domain.user.entity.User;
import com.example.shopping.domain.user.enums.UserRole;
import com.example.shopping.domain.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.transaction.Transactional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class AuthIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    PasswordEncoder passwordEncoder;

    private static final String NAME = "testUser";
    private static final String EMAIL = "test@example.com";
    private static final String PASSWORD = "password";
    private static final String ADDRESS = "서울시 구로구 가리봉동";
    private static final String USER_ROLE = "user";

    @Test
    void 회원가입_성공() throws Exception {
        //given
        SignupRequest request = new SignupRequest(EMAIL, PASSWORD, NAME, ADDRESS, USER_ROLE);

        //when & then
        mockMvc.perform(post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.message").value("sign up"))
            .andExpect(jsonPath("$.data.email").value("test@example.com"));
    }

    @Test
    void 로그인_성공_시_토큰_반환() throws Exception {
        //given
        userRepository.save(new User(EMAIL, passwordEncoder.encode(PASSWORD), NAME, ADDRESS, UserRole.of(USER_ROLE)));
        LoginRequest request = new LoginRequest(EMAIL, PASSWORD);

        //when & then
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("login"))
            .andExpect(jsonPath("$.data.email").value("test@example.com"))
            .andExpect(jsonPath("$.data.accessToken").exists())
            .andExpect(jsonPath("$.data.refreshToken").exists());
    }

    @Test
    void 리프레시_토큰으로_새로운_토큰_발급() throws Exception {
        //given
        User user = new User(EMAIL, passwordEncoder.encode(PASSWORD), NAME, ADDRESS, UserRole.of(USER_ROLE));
        userRepository.save(user);

        String refreshToken = jwtUtil.createRefreshToken(user.getId(), UserRole.of(USER_ROLE));
        redisTemplate.opsForValue().set("refreshToken : " + user.getId(), refreshToken, Duration.ofDays(7));

        //when & then
        mockMvc.perform(post("/auth/refresh")
                .header("Authorization", refreshToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("issue a refreshToken"))
            .andExpect(jsonPath("$.data.accessToken").exists());

    }

    @Test
    void 로그아웃_성공_시_블랙리스트에_토큰_등록() throws Exception {
        //given
        User user = new User(EMAIL, passwordEncoder.encode(PASSWORD), NAME, ADDRESS, UserRole.of(USER_ROLE));
        userRepository.save(user);

        String accessToken = jwtUtil.createAccessToken(user.getId(), UserRole.of(USER_ROLE));

        //when
        mockMvc.perform(post("/auth/logout")
                .header("Authorization", accessToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("logout"));

        //then
        String redisKey = "blacklist : " + jwtUtil.substringToken(accessToken);
        assertThat(redisTemplate.hasKey(redisKey)).isTrue();
    }

    @Test
    void 회원_탈퇴_성공() throws Exception {
        //given
        User user = new User(EMAIL, passwordEncoder.encode(PASSWORD), NAME, ADDRESS, UserRole.of(USER_ROLE));
        userRepository.save(user);
        WithdrawRequest request = new WithdrawRequest(PASSWORD);

        String accessToken = jwtUtil.createAccessToken(user.getId(), UserRole.of(USER_ROLE));
        String refreshToken = jwtUtil.createRefreshToken(user.getId(), UserRole.of(USER_ROLE));
        redisTemplate.opsForValue().set("refreshToken : " + user.getId(), refreshToken, Duration.ofDays(7));

        //when
        mockMvc.perform(post("/auth/withdraw")
                .header("Authorization", accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("withdraw"));

        //then
        assertThat(userRepository.findById(user.getId())).isEmpty();
        assertThat(redisTemplate.hasKey("refreshToken : " + user.getId())).isFalse();
    }
}