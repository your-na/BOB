package com.example.bob.Config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.*;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.socket.config.annotation.*;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final UserDetailsService userDetailsService; // ✅ 주입 필요

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

                System.out.println("🟨 accessor.getUser() = " + accessor.getUser());

                Authentication auth = (Authentication) accessor.getUser();
                if (auth != null) {
                    System.out.println("🟩 인증 주체: " + auth.getName());
                    SecurityContextHolder.getContext().setAuthentication(auth);
                } else {
                    System.out.println("❌ accessor.getUser()에서 사용자 인증 정보 없음");
                }

                return message;
            }

        });
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.setApplicationDestinationPrefixes("/app"); // 서버에서 받을 prefix
        registry.enableSimpleBroker("/topic"); // 클라이언트가 구독할 prefix
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws-chat")
                .addInterceptors(new HttpSessionHandshakeInterceptor()) // ✅ 요거 추가
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }


}
