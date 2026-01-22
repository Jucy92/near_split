package com.nearsplit.domain.user.service;

import com.nearsplit.common.exception.BusinessException;
import com.nearsplit.common.exception.ErrorCode;
import com.nearsplit.domain.user.entity.User;
import com.nearsplit.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Random;

/**
 * packageName  : com.nearsplit.domain.user.service
 * fileName     : NickNameGenerator
 * author       : user
 * date         : 2026-01-22(목)
 * description   : 회원 가입 시 닉네임 입력x -> 자동 생성을 위한 서비스
 * ===================================================
 * DATE                   AUTHOR          NOTE
 * ---------------------------------------------------
 * 2026-01-22(목)                user            최초 생성
 */

@Service
@RequiredArgsConstructor
public class NickNameGenerator {
    private static final List<String> ADJECTIVES = List.of(     // static 을 붙이면 메모리에 1회만 생성(공통 상수) 없으면 매 인스턴스 생성할 때 마다 그만큼 생김
            "귀여운", "깜찍한", "용감한", "자상한", "느긋한",
            "똑똑한", "졸린", "행복한", "수줍은", "대담한"
    );
    private static final List<String> ANIMALS = List.of(
            "고양이", "강아지", "토끼", "여우", "곰",
            "원숭이", "판다", "햄스터", "다람쥐", "사자"
    );

    private static final Random random = new Random();
    private final UserRepository userRepository;

    public String generate() {
        String adjective = ADJECTIVES.get(random.nextInt(ADJECTIVES.size()));
        String animal = ANIMALS.get(random.nextInt(ANIMALS.size()));
        String generateName = adjective + animal;

        int count = userRepository.countByNicknameStartingWith(generateName);
        if (count == 0) {
            return generateName + "001";
        }
        // 001, 002, 003 ~ 형식으로 숫자 추가
        String suffix = String.format("%03d", count + 1);
        return generateName + suffix;
    }
}
