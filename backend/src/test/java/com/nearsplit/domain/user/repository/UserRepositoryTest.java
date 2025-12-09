package com.nearsplit.domain.user.repository;

import com.nearsplit.domain.user.entity.User;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.as;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Test
    void 사용자_저장_및_조회() {
        // given
        User user = User.builder()
                .email("test1@test.com")
                .name("사용자1")
                .nickname("테스터")
                .password(passwordEncoder.encode("test1234"))
                .build();

        // when
        User savedUser = userRepository.save(user);

        // then
        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getEmail()).isEqualTo("test1@test.com");
        assertThat(savedUser.getName()).isEqualTo("사용자1");
        assertThat(passwordEncoder.matches("test1234",savedUser.getPassword())).isTrue();
    }

    @Test
    void 이메일로_사용자_조회() {
        // given
        User user = new User();
        user.setEmail("test1@test.com");
        user.setName("사용자1");
        user.setNickname("테스터");
        user.setPassword(passwordEncoder.encode("test1234"));
        userRepository.save(user);

        // when
        User findUser = userRepository.findByEmail("test1@test.com").
                orElseThrow(() -> new IllegalArgumentException("조회 가능한 사용자가 없습니다."));

        // then
        assertThat(findUser.getEmail()).isEqualTo("test1@test.com");
    }

    @Test
    void 이메일중복확인() {
        // given
        User user = new User();
        user.setEmail("test1@test.com");
        user.setName("사용자1");
        user.setNickname("테스터");
        user.setPassword(passwordEncoder.encode("test1234"));
        userRepository.save(user);

        // when
        boolean exists = userRepository.existsByEmail("test1@test.com");

        // then
        assertThat(exists).isTrue();
    }
}