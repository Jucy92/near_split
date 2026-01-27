package com.nearsplit.common.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nearsplit.common.exception.ErrorCode;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;


@RequiredArgsConstructor
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter { // OncePerRequestFilter 인터페이스 = 모든 요청마다 실행되는 보안 필터
    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;

    private final RequestMatcher passingPaths = new OrRequestMatcher(
            new ArrayList<>(Arrays.asList(
                    // 버려도 되는 브라우저 요청
                    new AntPathRequestMatcher("/.well-known/appspecific/com.chrome.devtools.json"),
                    new AntPathRequestMatcher("/favicon.ico")
            ))
    );
    private final RequestMatcher skipPaths = new OrRequestMatcher(
            new ArrayList<>(Arrays.asList(
                    new AntPathRequestMatcher("/api/auth/**"),
                    new AntPathRequestMatcher(("/ws/**"))
            ))
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        if (passingPaths.matches(request)) {    // .well-known, favicon 요청은 그냥 버리기
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
            return; // 그냥 리턴을 해버리면 브라우저에서 재요청/오류 처리 => 상태 메시지 담아주고 리턴
        }
        if (skipPaths.matches(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = resolveToken(request);

        // 토큰이 없으면 다음 필터로
        if (token == null ) {
            filterChain.doFilter(request, response);
            return;
        }

        // 필터는 인증 처리만!
        TokenStatus tokenStatus = jwtUtil.validToken(token);

        switch (tokenStatus) {
            case VALID:
                // 토큰에 정보가 있으면, userId 추출하고 인증 정보 설정
                Long userId = jwtUtil.getUserId(token);
                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(userId, null, Collections.emptyList());

                //SecurityContext ctx = SecurityContextHolder.getContext();
                //ctx.setAuthentication(auth);  // 이 과정을 축약해서 아래처럼.. 체이닝..
                SecurityContextHolder.getContext().setAuthentication(auth); // 사용자 정보 => 설정 없이 doFilter 되더라도 다음 필터에서 null 값인 익명사용자 자동 생성함

                filterChain.doFilter(request, response);
                break;
            case EXPIRED:
                response.setStatus(401);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write(objectMapper.writeValueAsString(ErrorCode.ACCESS_EXPIRED));
                return;
            case INVALID_SIGNATURE, MALFORMED:
                response.setStatus(401);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write(objectMapper.writeValueAsString(ErrorCode.INVALID_TOKEN));
                return;
        }
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
