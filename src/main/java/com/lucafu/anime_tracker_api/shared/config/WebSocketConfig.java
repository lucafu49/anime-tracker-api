package com.lucafu.anime_tracker_api.shared.config;

import com.lucafu.anime_tracker_api.shared.security.JwtChannelInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtChannelInterceptor jwtChannelInterceptor;

    @Value("${cors.allowed-origin:http://localhost:5173}")
    private String allowedOrigin;

    /**
     * Configura el broker en memoria.
     * /topic  → destinos de broadcast (uno a muchos)
     * /app    → prefijo para mensajes que pasan por @MessageMapping (no usado aún)
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic");
        registry.setApplicationDestinationPrefixes("/app");
    }

    /**
     * Endpoint de conexión WebSocket: ws://host/ws
     * Se registra el origin permitido para el handshake.
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOrigins(allowedOrigin);
    }

    /**
     * Registra el interceptor que valida el JWT en el frame STOMP CONNECT.
     * Todos los mensajes entrantes pasan por este canal antes de ser procesados.
     */
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(jwtChannelInterceptor);
    }
}
