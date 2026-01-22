package com.nearsplit.domain.user.repository;

import com.nearsplit.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findById(Long id);
    Optional<User> findByEmail(String email);
    Optional<User> findByNickname(String nickname); // 하늘소 만들었다 -> 다른 사용자가 보고 하늘소774 만들었다? -> 하늘소 지웠다? -> 닉네임으로 조회하니 상관없다.

    int countByNicknameStartingWith(String nickname);

    boolean existsByEmail(String email);
    boolean existsByNickname(String nickname);
}
