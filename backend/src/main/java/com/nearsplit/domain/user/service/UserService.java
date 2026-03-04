package com.nearsplit.domain.user.service;

import com.nearsplit.domain.user.dto.UserResponse;
import com.nearsplit.domain.user.dto.UserUpdateRequest;
import com.nearsplit.domain.user.entity.User;
import com.nearsplit.domain.user.repository.UserRepository;
import com.nearsplit.external.vworld.dto.Coordinate;
import com.nearsplit.external.vworld.service.VWorldGeocodingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class UserService {
    private final UserRepository userRepository;
    private final VWorldGeocodingService vWorldGeocodingService;

    private static final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

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

        // 닉네임 중복 검증 (Repository 필요 → 서비스 책임)
        if (request.getNickname() != null) {
            if (!request.getNickname().equals(target.getNickname())
                    && userRepository.existsByNickname(request.getNickname())) {
                throw new IllegalArgumentException("이미 사용 중인 닉네임 입니다.");
            }
        }

        // 도메인 메서드로 프로필 업데이트
        target.updateProfile(
                request.getNickname(),
                request.getAddress(),
                request.getProfileImage(),
                request.getPhone()
        );

        // 주소 변경 시 좌표 업데이트 (외부 API 호출 → 서비스 책임)
        if (request.getAddress() != null) {
            Coordinate coordinate = vWorldGeocodingService.getCoordinate(request.getAddress());
            // VWorld 좌표(경도, 위도)를 PostGIS Point로 변환 (경도, 위도 순서)
            Point location = geometryFactory.createPoint(
                    new org.locationtech.jts.geom.Coordinate(coordinate.getLongitude(), coordinate.getLatitude())
            );
            target.updateCoordinates(location);
        }

        // @Transactional 내에서는 save() 생략 가능 (Dirty Checking)
        userRepository.save(target);  // 명시적으로 save 호출
        return UserResponse.from(target);
    }
}
