package org.example.lannister.handler;



import org.example.lannister.presence.RedisPresenceService;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.util.UriComponentsBuilder;


@Component
public class PresenceWebSocketHandler extends TextWebSocketHandler {

    private final RedisPresenceService presence;

    public PresenceWebSocketHandler(RedisPresenceService presence) {
        this.presence = presence;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String userId = get(session, "userId");
        String deviceId = get(session, "deviceId");

        System.out.println("CONNECT → " + userId + " / " + deviceId);
        presence.markOnline(userId, deviceId);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage msg) {
        String userId = get(session, "userId");
        String deviceId = get(session, "deviceId");

        System.out.println("HEARTBEAT → " + userId + " / " + deviceId);
        presence.heartbeat(userId, deviceId);
    }

    private String get(WebSocketSession s, String key) {
        return UriComponentsBuilder
                .fromUri(s.getUri())
                .build()
                .getQueryParams()
                .getFirst(key);
    }

    @Override
    public void afterConnectionClosed(
            WebSocketSession session,
            CloseStatus status
    ) {
        String userId = get(session, "userId");
        String deviceId = get(session, "deviceId");

        System.out.println("DISCONNECT → " + userId + " / " + deviceId);
        presence.markOffline(userId, deviceId);
    }

    private String getUserId(WebSocketSession session) {
        return session.getUri().getQuery().split("=")[1];
    }

}
