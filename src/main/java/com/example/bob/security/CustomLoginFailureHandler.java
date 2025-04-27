package com.example.bob.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomLoginFailureHandler implements AuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {

        if (exception instanceof UsernameNotFoundException) {
            // 아이디가 틀렸을 때
            request.getSession().setAttribute("SPRING_SECURITY_LAST_EXCEPTION",
                    new BadCredentialsException("존재하지 않는 아이디입니다."));
        } else if (exception instanceof BadCredentialsException) {
            // 비밀번호가 틀렸을 때
            request.getSession().setAttribute("SPRING_SECURITY_LAST_EXCEPTION",
                    new BadCredentialsException("비밀번호가 틀립니다."));
        } else {
            // 그 외의 경우
            request.getSession().setAttribute("SPRING_SECURITY_LAST_EXCEPTION",
                    new BadCredentialsException("로그인 실패. 다시 시도해주세요."));
        }

        response.sendRedirect("/login?error");
    }
}
