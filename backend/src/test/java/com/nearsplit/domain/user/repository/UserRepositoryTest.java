package com.nearsplit.domain.user.repository;

import com.nearsplit.config.QueryDslConfig;
import com.nearsplit.domain.user.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest    // 엔티티 기반으로 데이터 베이스 빈만 등록
@Import(QueryDslConfig.class)  // QueryDSL의 JPAQueryFactory 빈 로드
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Test
    void 사용자_저장_및_조회() {
        // given
        User user = User.createUser(
                "test1@test.com",
                passwordEncoder.encode("test1234"),
                "사용자1",
                "테스터"
        );

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
        User user = User.createUser(
                "test1@test.com",
                passwordEncoder.encode("test1234"),
                "사용자1",
                "테스터"
        );
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
        User user = User.createUser(
                "test1@test.com",
                passwordEncoder.encode("test1234"),
                "사용자1",
                "테스터"
        );
        userRepository.save(user);

        // when
        boolean exists = userRepository.existsByEmail("test1@test.com");

        // then
        assertThat(exists).isTrue();
    }
}
