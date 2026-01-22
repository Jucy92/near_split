package com.nearsplit.domain.user.service;

import com.nearsplit.domain.user.dto.LoginRequest;
import com.nearsplit.domain.user.dto.LoginResponse;
import com.nearsplit.domain.user.dto.RegisterRequest;
import com.nearsplit.domain.user.dto.UserResponse;
import com.nearsplit.domain.user.entity.User;
import com.nearsplit.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final NickNameGenerator nickNameGenerator;

    @Transactional
    public Long register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("이미 등록된 이메일입니다.");
        }
        //if (userRepository.existsByNickname(request.getNickname())) {     // 여긴 필요 없고 수정 할 때 체크
        //    throw new IllegalArgumentException("이미 등록된 닉네임입니다.");
        //}
        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .nickname(nickNameGenerator.generate())
                .build();

        User saved = userRepository.save(user);
        return saved.getId();
    }

    public LoginResponse login(LoginRequest loginRequest) {
        User findUser = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 이메일입니다"));

        if (!passwordEncoder.matches(loginRequest.getPassword(), findUser.getPassword())) {
            throw new IllegalArgumentException("입력하신 패스워드가 틀립니다");
        }

        return LoginResponse.builder().userResponse(UserResponse.fromBasic(findUser)).build();

    }

    public void logout() {
        /**
         * 세션이었으면 SpringSecurity 에서 지워줘야 할 작업이 있겠지만.. JWT라 처리할 게 없네
         * .logout(logout -> logout
         *       .logoutUrl("/logout")
         *       .deleteCookies("JSESSIONID")           // 1️⃣ 쿠키 삭제
         *       .invalidateHttpSession(true)            // 2️⃣ 서버의 세션 무효화 ⭐⭐⭐
         *       .clearAuthentication(true)              // 3️⃣ SecurityContext 초기화
         *       .logoutSuccessHandler(...)              // 4️⃣ 로그아웃 후 처리
         *   )
         *
         *   만약 세션 정보를 DB에 저장 했다면 값을 지웠어야 함
         */
    }

}
