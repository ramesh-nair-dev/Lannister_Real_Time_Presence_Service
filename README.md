# Lannister - Real-Time User Presence System

## What is this?

A Spring Boot application that tracks whether users are online or offline in real-time. Think of the green/gray dots you see in WhatsApp, Slack, or Discord showing who's online.

## Problem it solves

- Know if a user is currently online
- Support users logged in from multiple devices (phone, laptop, tablet)
- Automatically detect when a user's app crashes without properly disconnecting
- Scale across multiple servers

## Technologies Used

- **Spring Boot 4.0.1** - Backend framework
- **WebSocket** - Real-time bidirectional communication
- **Redis** - Distributed storage for presence data
- **Java 17** - Programming language
- **Maven** - Build tool

## How it works

### 1. User Connects
When a user opens the app, they establish a WebSocket connection:
```
ws://localhost:8080/ws?userId=user123&deviceId=phone1
```

The system marks them as online in Redis.

### 2. Heartbeat
The client sends periodic messages (heartbeats) every few seconds to say "I'm still here". This refreshes their online status.

### 3. Disconnect Handling

**Clean disconnect:** User closes app normally - system marks them offline immediately.

**Crash/Network failure:** If no heartbeat is received for 30 seconds, Redis automatically expires the user's presence key and they appear offline.

### 4. Multi-device support
A user can be online from multiple devices. They only appear offline when ALL devices are disconnected.

## What we implemented

### Components

**1. WebSocket Handler** (`PresenceWebSocketHandler`)
- Handles user connections, disconnections, and heartbeat messages
- Extracts userId and deviceId from connection URL

**2. Redis Presence Service** (`RedisPresenceService`)
- Stores presence data in Redis
- Uses TTL (Time To Live) for automatic expiration
- Manages device lists per user

**3. Redis Key Expiration Listener** (`RedisKeyExpirationListener`)
- Listens to Redis key expiration events
- Cleans up device lists when TTL expires
- Handles crashed connections automatically

**4. Scheduled Cleanup** (`PresenceCleanupScheduler`)
- Runs every 10 seconds as a backup
- Removes stale connections if expiration events are missed

**5. In-Memory Presence Manager** (`PresenceManager`)
- Local backup tracking system
- Stores WebSocket sessions in memory
- Works alongside Redis for redundancy

**6. Configuration Classes**
- `WebSocketConfig` - Sets up WebSocket endpoint at `/ws`
- `RedisConfig` - Configures Redis connection and expiration listener

## Redis Data Structure

For user `user123` with two devices:

```
presence:user123:phone1    → "1" (expires in 30 seconds)
presence:user123:laptop2   → "1" (expires in 30 seconds)
presence:user123:devices   → SET {"phone1", "laptop2"}
```

**Why this design?**
- Each device tracked independently
- Automatic cleanup via TTL expiration
- Fast presence check (if set size > 0, user is online)

## API Usage

### Check if user is online

```java
boolean online = redisPresenceService.isOnline("user123");
```

Returns `true` if any device is connected, `false` if all devices are offline.

## Setup and Run

### Prerequisites
- Java 17 or higher
- Redis server running on `localhost:6379`
- Maven

### Steps

1. Start Redis:
```bash
redis-server
```

2. Enable Redis keyspace notifications (required for expiration events):
```bash
redis-cli config set notify-keyspace-events Ex
```

3. Run the application:
```bash
mvn spring-boot:run
```

4. The server starts on `http://localhost:8080`

### Test with WebSocket client

Connect using any WebSocket client:
```
ws://localhost:8080/ws?userId=testUser&deviceId=device1
```

Send any text message as heartbeat to keep the connection alive.

## Configuration

Edit `src/main/resources/application.properties`:

```properties
# Redis
spring.data.redis.host=localhost
spring.data.redis.port=6379

# Server
server.port=8080
```

## Key Features Implemented

✅ Real-time WebSocket connections  
✅ Multi-device support per user  
✅ Automatic crash detection (30-second TTL)  
✅ Redis-based distributed presence tracking  
✅ Event-driven cleanup using Redis pub/sub  
✅ Scheduled backup cleanup (every 10 seconds)  
✅ In-memory fallback tracking  
✅ Concurrent connection handling  
✅ Thread-safe operations  

## Project Structure

```
src/main/java/org/example/lannister/
├── LannisterApplication.java          # Main entry point
├── handler/
│   └── PresenceWebSocketHandler.java  # WebSocket event handler
├── presence/
│   ├── RedisPresenceService.java      # Redis operations
│   └── RedisKeyExpirationListener.java # Expiration event listener
├── manager/
│   └── PresenceManager.java           # In-memory backup
├── scheduler/
│   └── PresenceCleanupScheduler.java  # Periodic cleanup
└── configs/
    ├── WebSocketConfig.java           # WebSocket setup
    └── RedisConfig.java               # Redis setup
```

## How Crash Detection Works

1. User connects, Redis key created with 30-second TTL
2. Client sends heartbeat every 10 seconds
3. Each heartbeat refreshes the TTL back to 30 seconds
4. If app crashes (no heartbeat):
   - After 30 seconds, Redis key expires
   - Redis publishes expiration event
   - Listener receives event and removes device from user's device list
   - User appears offline

## Scaling

This system scales horizontally:
- Run multiple Spring Boot instances behind a load balancer
- All instances share the same Redis server
- No sticky sessions needed
- For production, use Redis Cluster for high availability

## Use Cases

- Chat applications (show who's online)
- Collaboration tools (see active team members)
- Gaming platforms (display online friends)
- Customer support (show available agents)
- Social networks (presence indicators)

## Notes

- TTL is set to 30 seconds (configurable in `RedisPresenceService`)
- Cleanup scheduler runs every 10 seconds (configurable in `PresenceCleanupScheduler`)
- All operations are thread-safe using `ConcurrentHashMap`
- Redis connection must be active for the system to work

## License

This is a demonstration project.
