package com.nearsplit.domain.user.service;

import com.nearsplit.domain.user.dto.UserUpdateRequest;
import com.nearsplit.domain.user.dto.UserUpdateResponse;
import com.nearsplit.domain.user.entity.User;
import com.nearsplit.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {
    private final UserRepository userRepository;

    @Transactional
    public UserUpdateResponse updateProfile(long userId, UserUpdateRequest request) {
        User target = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("본인 계정을 찾을 수 없습니다."));

        // User.update() 메서드 만드려다가..  그냥 세터 사용..
        if (request.getNickname() != null) target.setNickname(request.getNickname());
        if (request.getAddress() != null) target.setAddress(request.getAddress());
        if (request.getLocation() != null) target.setLocation(request.getLocation());
        if (request.getProfileImage() != null) target.setProfileImage(request.getProfileImage());
        if (request.getPhone() != null) target.setPhone(request.getPhone());


        // @Transactional 내에서는 save() 생략 가능 (Dirty Checking)
        userRepository.save(target);  // 명시적으로 save 호출
        return UserUpdateResponse.from(target);
    }
}
