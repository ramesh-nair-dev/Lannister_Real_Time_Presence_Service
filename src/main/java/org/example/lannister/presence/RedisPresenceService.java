package org.example.lannister.presence;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.TimeUnit;

@Component
public class RedisPresenceService {

    private static final long TTL_SECONDS = 30;

    private final RedisTemplate<String, String> redis;

    public RedisPresenceService(RedisTemplate<String, String> redis) {
        this.redis = redis;
    }

    private String deviceKey(String userId, String deviceId) {
        return "presence:" + userId + ":" + deviceId;
    }

    private String deviceSetKey(String userId) {
        return "presence:" + userId + ":devices";
    }

    // 1️⃣ On CONNECT
    public void markOnline(String userId, String deviceId) {
        redis.opsForSet().add(deviceSetKey(userId), deviceId);

        redis.opsForValue().set(
                deviceKey(userId, deviceId),
                "1",
                TTL_SECONDS,
                TimeUnit.SECONDS
        );
        System.out.println("ONLINE → " + userId + " / " + deviceId);
    }

    // 2️⃣ On HEARTBEAT
    public void heartbeat(String userId, String deviceId) {
        redis.expire(
                deviceKey(userId, deviceId),
                TTL_SECONDS,
                TimeUnit.SECONDS
        );
    }

    // 3️⃣ On CLEAN DISCONNECT
    public void markOffline(String userId, String deviceId) {
        redis.opsForSet().remove(deviceSetKey(userId), deviceId);
        redis.delete(deviceKey(userId, deviceId));
        System.out.println("TTL REFRESH → " + userId + " / " + deviceId);
    }

    // 4️⃣ Is user online?
    public boolean isOnline(String userId) {
        Long count = redis.opsForSet().size(deviceSetKey(userId));
        return count != null && count > 0;
    }
}
