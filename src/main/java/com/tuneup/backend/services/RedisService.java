package com.tuneup.backend.services;

import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class RedisService {

    private final StatefulRedisConnection<String, String> redisConnection;

    public RedisService(StatefulRedisConnection<String, String> redisConnection) {
        this.redisConnection = redisConnection;
    }

    public void saveUnverifiedUser(String key, Map<String, String> data, long ttlSeconds) {
        RedisCommands<String, String> commands = redisConnection.sync();
        commands.del(key);
        commands.hset(key, data);
        commands.expire(key, ttlSeconds);
    }

    public Map<String, String> getUserData(String key) {
        return redisConnection.sync().hgetall(key);
    }

    public void delete(String key) {
        redisConnection.sync().del(key);
    }

    public boolean exists(String key) {
        return redisConnection.sync().exists(key) > 0;
    }
}
