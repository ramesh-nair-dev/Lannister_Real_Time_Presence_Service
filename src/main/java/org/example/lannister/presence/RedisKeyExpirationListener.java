package org.example.lannister.presence;

import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class RedisKeyExpirationListener implements MessageListener {

    private final RedisTemplate<String, String> redis;

    public RedisKeyExpirationListener(RedisTemplate<String, String> redis) {
        this.redis = redis;
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String expiredKey = message.toString();

        // We only care about presence keys
        if (!expiredKey.startsWith("presence:")) return;

        // presence:userId:deviceId
        String[] parts = expiredKey.split(":");
        if (parts.length != 3) return;

        String userId = parts[1];
        String deviceId = parts[2];

        // Remove device from SET
        redis.opsForSet().remove(
                "presence:" + userId + ":devices",
                deviceId
        );

        System.out.println(
                "Device expired → " + userId + " / " + deviceId
        );
    }
}
