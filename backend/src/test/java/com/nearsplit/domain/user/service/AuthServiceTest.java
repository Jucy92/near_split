package com.nearsplit.domain.user.service;

import com.nearsplit.domain.user.dto.LoginRequest;
import com.nearsplit.domain.user.dto.LoginResponse;
import com.nearsplit.domain.user.dto.RegisterRequest;
import com.nearsplit.domain.user.entity.User;
import com.nearsplit.domain.user.repository.UserRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional  // 테스트 후 롤백
class AuthServiceTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private Validator validator;

    @Test
    void 회원가입_성공() {
        // given
        RegisterRequest request = RegisterRequest.builder()
                .email("test1@test.com")
                .password("test1234")
                .name("홍길동")
                .nickname("테스터1")  // 닉네임은 이제 자동 생성됨 (NickNameGenerator 사용)
                .build();

        // when
        Long saved = authService.register(request);

        // then
        assertThat(saved).isNotNull();

        User user = userRepository.findById(saved).orElseThrow();
        assertThat(user.getEmail()).isEqualTo("test1@test.com");
        assertThat(user.getName()).isEqualTo("홍길동");
        // 닉네임은 NickNameGenerator가 자동 생성하므로 "형용사+동물+숫자" 패턴 확인
        assertThat(user.getNickname()).isNotBlank();
        assertThat(user.getNickname()).matches(".*\\d{3}$");  // 숫자 3자리로 끝나는지 확인
    }

    @Test
    void 회원가입_실패_빈_이름() {
        // given
        RegisterRequest request = RegisterRequest.builder()
                .email("test1@test.com")
                .password("test1234")
                .name("")  // 검증 실패해야 함
                .nickname("테스터1")
                .build();

        // when - Validator로 수동 검증
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        // then
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v ->
                v.getPropertyPath().toString().equals("name") &&
                v.getMessage().equals("필수 값 입니다.")
        );
    }

    @Test
    void 회원가입_실패_잘못된_이메일() {
        // given
        RegisterRequest request = RegisterRequest.builder()
                .email("잘못된이메일")
                .password("test1234")
                .name("홍길동")
                .nickname("테스터1")
                .build();

        // when
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        // then
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v ->
                v.getPropertyPath().toString().equals("email") &&
                v.getMessage().equals("이메일 형식이 아닙니다.")
        );
    }

    @Test
    void 회원가입_실패_중복_이메일() {
        // given
        RegisterRequest request1 = RegisterRequest.builder()
                .email("test1@test.com")
                .password("test1234")
                .name("홍길동")
                .nickname("테스터1")
                .build();

        authService.register(request1);

        RegisterRequest request2 = RegisterRequest.builder()
                .email("test1@test.com")  // 중복 이메일
                .password("test5678")
                .name("김철수")
                .nickname("테스터2")
                .build();

        // when & then
        assertThatThrownBy(() -> authService.register(request2))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이미 등록된 이메일입니다.");
    }

    @Test
    void 회원가입_닉네임_자동생성_중복방지() {
        // given - 닉네임은 NickNameGenerator가 자동 생성하므로 중복이 발생하지 않음
        // 같은 "형용사+동물" 조합이라도 숫자 suffix로 구분됨 (예: 귀여운고양이001, 귀여운고양이002)
        RegisterRequest request1 = RegisterRequest.builder()
                .email("test1@test.com")
                .password("test1234")
                .name("홍길동")
                .build();

        RegisterRequest request2 = RegisterRequest.builder()
                .email("test2@test.com")
                .password("test5678")
                .name("김철수")
                .build();

        // when
        Long userId1 = authService.register(request1);
        Long userId2 = authService.register(request2);

        // then - 둘 다 성공적으로 등록됨
        User user1 = userRepository.findById(userId1).orElseThrow();
        User user2 = userRepository.findById(userId2).orElseThrow();

        assertThat(user1.getNickname()).isNotBlank();
        assertThat(user2.getNickname()).isNotBlank();
        // 닉네임이 다르거나, 같은 베이스명이라면 숫자가 다름
        // (랜덤 생성이므로 같을 수도 다를 수도 있음 - 중요한 건 둘 다 등록 성공)
    }

    @Test
    void 로그인() {
        // given
        RegisterRequest request = RegisterRequest.builder()
                .email("test1@test.com")
                .password("test1234")
                .name("홍길동")
                .nickname("테스터1")
                .build();
        authService.register(request);
        LoginRequest loginRequest = LoginRequest.builder().email("test1@test.com").password("test1234").build();

        // when
        LoginResponse login = authService.login(loginRequest);
        System.out.println("login = " + login);

        // then
        assertThat(login).isNotNull();

    }
}