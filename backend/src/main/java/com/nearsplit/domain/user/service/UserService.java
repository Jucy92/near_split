package com.nearsplit.domain.user.service;

import com.nearsplit.domain.user.dto.UserResponse;
import com.nearsplit.domain.user.dto.UserUpdateRequest;
import com.nearsplit.domain.user.entity.User;
import com.nearsplit.domain.user.repository.UserRepository;
import com.nearsplit.external.vworld.dto.Coordinate;
import com.nearsplit.external.vworld.service.VWorldGeocodingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class UserService {
    private final UserRepository userRepository;
    private final VWorldGeocodingService vWorldGeocodingService;

    public UserResponse getProfile(long userId) {
        User findUser = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("본인 계정을 찾을 수 없습니다."));
        return UserResponse.from(findUser);
    }

    @Transactional
    public UserResponse updateProfile(long userId, UserUpdateRequest request) {
        log.info("request ={}", request);

        User target = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("계정을 찾을 수 없습니다."));

        // User.update() 메서드 만드려다가..  그냥 세터 사용..
        if (request.getNickname() != null) {
            if (!request.getNickname().equals(target.getNickname()) // 내 닉네임 != 기존 닉네임
                    && userRepository.existsByNickname(request.getNickname())) {
                throw new IllegalArgumentException("이미 사용 중인 닉네임 입니다.");
            }
            target.setNickname(request.getNickname());
        }
        if (request.getAddress() != null){
            target.setAddress(request.getAddress());

            Coordinate coordinate = vWorldGeocodingService.getCoordinate(request.getAddress());
            target.setLatitude(coordinate.getLatitude());
            target.setLongitude(coordinate.getLongitude());
        }

        if (request.getLocation() != null) target.setLocation(request.getLocation());
        if (request.getProfileImage() != null) target.setProfileImage(request.getProfileImage());
        if (request.getPhone() != null) target.setPhone(request.getPhone());


        // @Transactional 내에서는 save() 생략 가능 (Dirty Checking)
        userRepository.save(target);  // 명시적으로 save 호출
        return UserResponse.from(target);
    }
}
