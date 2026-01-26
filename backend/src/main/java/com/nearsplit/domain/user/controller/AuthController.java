package com.nearsplit.domain.user.controller;

import com.nearsplit.common.dto.ApiResponse;
import com.nearsplit.common.security.JwtUtil;
import com.nearsplit.common.security.TokenStatus;
import com.nearsplit.domain.user.dto.LoginRequest;
import com.nearsplit.domain.user.dto.LoginResponse;
import com.nearsplit.domain.user.dto.RegisterRequest;
import com.nearsplit.domain.user.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtUtil jwtUtil;


    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        Long userId = authService.register(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request, HttpServletResponse response) {
        LoginResponse loginResponse = authService.login(request);

        Long loginId = loginResponse.getUserResponse().getId();
        String token = jwtUtil.generateToken(loginId);

        Cookie cookie = new Cookie("accessToken", token);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setSecure(false);    // https 인지 체크 -> 개발: false, 프로덕션: true (HTTPS에서만)
        cookie.setMaxAge(jwtUtil.getExpiration());

        String refreshToken = jwtUtil.generateRefreshToken(loginId);

        Cookie refreshCooke = new Cookie("refreshToken", refreshToken);
        refreshCooke.setPath("/");
        refreshCooke.setHttpOnly(true);
        refreshCooke.setSecure(false);
        refreshCooke.setMaxAge(604_800_000);    // 7일

        response.addCookie(cookie);
        response.addCookie(refreshCooke);


        return ResponseEntity.ok().body(ApiResponse.success(loginResponse));
        //return ResponseEntity.ok(ApiResponse.success(loginResponse));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<?>> refresh(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = resolveToken(request);
        if (TokenStatus.VALID.equals(jwtUtil.validToken(refreshToken))) {
            String token = jwtUtil.generateToken(jwtUtil.getUserId(refreshToken));
            Cookie cookie = new Cookie("accessToken", token);
            cookie.setPath("/");
            cookie.setHttpOnly(true);
            cookie.setSecure(false);
            cookie.setMaxAge(jwtUtil.getExpiration());

            response.addCookie(cookie);
        } else {
            // 분기 처리 하는거에 따라서 else if로 상태값 다르게 리턴
        }
        return ResponseEntity.ok().build();
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response /*@AuthenticationPrincipal Long userId*/) { // Spring Security 통과해서 와서 auth 등록은 된 상태라 사용은 가능..
        Cookie cookie = new Cookie("accessToken", null);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setSecure(false);

        response.addCookie(cookie);
        // 지금 생각해보니깐.. 로그인 할 때는 쿠키 값을 컨트롤러에서 생성해줬는데 /*필터에서 인증 생성할 때(기존로직 OK),*/ 토큰 갱신 할 때(필터쪽에 추가해야함)는 필터에서 쿠키 처리를 해줘야 할 거 같은데..
        return ResponseEntity.ok("로그아웃 성공");

    }


    private String resolveToken(HttpServletRequest request) {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        // 1. Authorization 헤더에서 토큰 추출
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }

        // 2. Authorization 헤더에 없으면 Cookie에서 토큰 추출
        if (request.getCookies() != null) {
            for (var cookie : request.getCookies()) {
                if ("accessToken".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

}
