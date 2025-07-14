package com.example.shopping.domain.auth;

import com.example.shopping.config.JwtUtil;
import com.example.shopping.config.PasswordEncoder;
import com.example.shopping.domain.auth.dto.request.LoginRequestDto;
import com.example.shopping.domain.auth.dto.request.SignupRequestDto;
import com.example.shopping.domain.auth.dto.request.WithdrawRequestDto;
import com.example.shopping.domain.user.entity.User;
import com.example.shopping.domain.user.enums.UserRole;
import com.example.shopping.domain.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Duration;
import java.util.Date;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "jwt.secret.key=WvG406dyLBEesRAwcYAKSA4PicZkO/iBAFB93NLYF4k=")
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

    private static final String BEARER_PREFIX = "Bearer ";
    private static final Long USER_ID = 1L;
    private static final String NAME = "testUser";
    private static final String EMAIL = "test@example.com";
    private static final String PASSWORD = "password";
    private static final String ADDRESS = "서울시 구로구 가리봉동";
    private static final String USER_ROLE = "user";
//
//    @Test
//    void exception_test_when_TOKEN_expired() {
//        // given
//        String expiredToken = Jwts
//                .builder()
//                .setSubject(NAME)
//                .setIssuedAt(new Date(System.currentTimeMillis() - 1000 * 60 * 10))
//                .setExpiration(new Date(System.currentTimeMillis() - 1000 * 60 * 5))
//                .signWith(key, SignatureAlgorithm.HS256)
//        // when
//        // then
//    }



    @Test
    void 회원가입_성공() throws Exception{
        //given
        SignupRequestDto request = new SignupRequestDto(EMAIL, PASSWORD, NAME, ADDRESS, USER_ROLE);

        //when & then
        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("회원가입이 완료되었습니다"))
                .andExpect(jsonPath("$.data.email").value("test@example.com"));
    }

    @Test
    void 로그인_성공_시_토큰_반환() throws Exception{
        //given
        userRepository.save(new User(EMAIL, passwordEncoder.encode(PASSWORD), NAME, ADDRESS, UserRole.of(USER_ROLE)));
        LoginRequestDto request = new LoginRequestDto(EMAIL, PASSWORD);

        //when & then
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("로그인 했습니다"))
                .andExpect(jsonPath("$.data.email").value("test@example.com"))
                .andExpect(jsonPath("$.data.token").exists())
                .andExpect(jsonPath("$.data.refreshToken").exists());
    }

    @Test
    void 로그아웃_성공_시_블랙리스트에_토큰_등록() throws Exception{
        //given
        String accessToken = jwtUtil.createToken(USER_ID, EMAIL, UserRole.of(USER_ROLE));

        //when
        mockMvc.perform(post("/auth/logout")
                .header("Authorization", accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("로그아웃 했습니다"));

        //then
        String redisKey = "blacklist : " + jwtUtil.substringToken(accessToken);
        assertThat(redisTemplate.hasKey(redisKey)).isTrue();
    }

    @Test
    void 리프레시_토큰으로_새로운_토큰_발급() throws Exception{
        //given
        String refreshToken = jwtUtil.createRefreshToken(USER_ID, EMAIL, UserRole.of(USER_ROLE));
        String bearerRefreshToken = BEARER_PREFIX + refreshToken;
        redisTemplate.opsForValue().set("refreshToken : " + USER_ID, refreshToken, Duration.ofDays(7));

        //when & then
        mockMvc.perform(post("/auth/refresh")
                .header("Authorization", refreshToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("토큰 재발급"))
                .andExpect(jsonPath("$.data.token").exists());

    }

    @Test
    void 회원_탈퇴_성공() throws Exception{
        //given
        User user = new User(EMAIL, passwordEncoder.encode(PASSWORD), NAME, ADDRESS, UserRole.of(USER_ROLE));
        userRepository.save(user);
        WithdrawRequestDto request = new WithdrawRequestDto(PASSWORD);

        String accessToken = jwtUtil.createToken(user.getId(), EMAIL, UserRole.of(USER_ROLE));
        String refreshToken = jwtUtil.createRefreshToken(user.getId(), EMAIL, UserRole.of(USER_ROLE));
        redisTemplate.opsForValue().set("refreshToken : " + user.getId(), refreshToken, Duration.ofDays(7));

        //when
        mockMvc.perform(post("/auth/withdraw")
                .header("Authorization", accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("회원탈퇴가 완료되었습니다"));

        //then
        assertThat(userRepository.findById(user.getId())).isEmpty();
        assertThat(redisTemplate.hasKey("refreshToken : " + user.getId())).isFalse();
    }
}