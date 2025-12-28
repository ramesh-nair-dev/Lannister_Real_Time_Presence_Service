package org.example.lannister.manager;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class PresenceManager {

    private static final long TIMEOUT_MS = 30_000;


    private final Map<String, Set<WebSocketSession>> sessions =
            new ConcurrentHashMap<>();

    private final Map<String, Long> lastHeartbeat =
            new ConcurrentHashMap<>();


    public void userConnected(String userId, WebSocketSession session) {
        sessions
                .computeIfAbsent(userId, k -> ConcurrentHashMap.newKeySet())
                .add(session);

        lastHeartbeat.put(userId, System.currentTimeMillis());
        System.out.println(userId + " is ONLINE");
    }

    public void userDisconnected(String userId, WebSocketSession session) {
        Set<WebSocketSession> userSessions = sessions.get(userId);
        if (userSessions == null) return;

        userSessions.remove(session);

        if (userSessions.isEmpty()) {
            sessions.remove(userId);
            //
        }
    }
    public boolean isOnline(String userId) {
        Long lastSeen = lastHeartbeat.get(userId);
        if (lastSeen == null) return false;

        return System.currentTimeMillis() - lastSeen <= TIMEOUT_MS;
    }

    public void heartbeat(String userId) {
        boolean wasOnline = isOnline(userId);

        lastHeartbeat.put(userId, System.currentTimeMillis());

        if (!wasOnline) {
            System.out.println(userId + " RECOVERED → ONLINE");
        }
    }

    public void expireOfflineUsers() {
        long now = System.currentTimeMillis();

        for (String userId : lastHeartbeat.keySet()) {
            long lastSeen = lastHeartbeat.get(userId);

            if (now - lastSeen > TIMEOUT_MS) {
                sessions.remove(userId);
                lastHeartbeat.remove(userId);
                System.out.println(userId + " TIMED OUT → OFFLINE");
            }
        }
    }


}
