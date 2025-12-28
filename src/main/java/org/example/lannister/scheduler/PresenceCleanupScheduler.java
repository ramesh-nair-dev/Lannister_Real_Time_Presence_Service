package org.example.lannister.scheduler;

import org.example.lannister.manager.PresenceManager;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class PresenceCleanupScheduler {

    private final PresenceManager presenceManager;

    public PresenceCleanupScheduler(PresenceManager presenceManager) {
        this.presenceManager = presenceManager;
    }

    @Scheduled(fixedDelay = 10_000) // every 10 seconds
    public void cleanup() {
        presenceManager.expireOfflineUsers();
    }
}
