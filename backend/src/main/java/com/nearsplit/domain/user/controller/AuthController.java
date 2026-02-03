package com.nearsplit.domain.user.controller;

import com.nearsplit.common.dto.ApiResponse;
import com.nearsplit.common.exception.ErrorCode;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;
    private final JwtUtil jwtUtil;
    @Value("${cookie.secure:false}")
    private boolean cookieSecure;   // 운영환경(개발/운영)에 따른 쿠키 설정
    @Value("${cookie.same-site:Lax}")
    private String cookieSameSite;


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
        String refreshToken = jwtUtil.generateRefreshToken(loginId);

        ResponseCookie cookie = generateCookie("accessToken", token);
        ResponseCookie refreshCooke = refreshCookie("refreshToken", refreshToken);



        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());          // 30분
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCooke.toString());   // 7일


        return ResponseEntity.ok().body(ApiResponse.success(loginResponse));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<?>> refresh(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = resolveToken(request);
        if (refreshToken != null) {

            switch (jwtUtil.validToken(refreshToken)) {
                case VALID:

                    String token = jwtUtil.generateToken(jwtUtil.getUserId(refreshToken));

                    ResponseCookie cookie = generateCookie("accessToken", token);
                    response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

                    return ResponseEntity.ok().body(ApiResponse.successWithMessage("토큰 갱신 성공"));
                case EXPIRED:
                    deleteCookie(response);
                    return ResponseEntity.status(401).body(ApiResponse.success(ErrorCode.REFRESH_EXPIRED));
                default:
                    break;
            }
        }
        deleteCookie(response);
        return ResponseEntity.status(401).body(ApiResponse.success(ErrorCode.REFRESH_INVALID));
    }
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response /*@AuthenticationPrincipal Long userId*/) { // Spring Security 통과해서 와서 auth 등록은 된 상태라 사용은 가능..
        deleteCookie(response);

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
                if ("refreshToken".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    private ResponseCookie generateCookie(String cookieName, String token) {

        return ResponseCookie.from(cookieName, token)
                .path("/")
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite(cookieSameSite)
                .maxAge(jwtUtil.getExpiration())
                .build();
    }

    private ResponseCookie refreshCookie(String cookieName, String refreshToken) {

        return ResponseCookie.from(cookieName, refreshToken)
                .path("/")
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite(cookieSameSite)
                .maxAge(604_800_000)
                .build();
    }

    private void deleteCookie(HttpServletResponse response) {

        ResponseCookie accessCookie = ResponseCookie.from("accessToken", "")
                .path("/")
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite(cookieSameSite)
                .maxAge(0)  // 삭제!
                .build();

        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", "")
                .path("/")
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite(cookieSameSite)
                .maxAge(0)  // 삭제!
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());
    }

}
