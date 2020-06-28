package com.chrisworks.personal.inventorysystem.Backend.Configurations;

import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
public class RedisUtil<T> {

    private final RedisTemplate<String, T> redisTemplate;
    private final HashOperations<String, Object, T> hashOperation;
    private final ValueOperations<String, T> valueOperations;

    RedisUtil(RedisTemplate<String, T> redisTemplate){
        this.redisTemplate = redisTemplate;
        this.hashOperation = redisTemplate.opsForHash();
        this.valueOperations = redisTemplate.opsForValue();
    }

    public void putMap(String redisKey, Object key, T data) {
        hashOperation.put(redisKey, key, data);
    }

    public T getMapAsSingleEntry(String redisKey, Object key) {
        return  hashOperation.get(redisKey, key);
    }

    public Map<Object, T> getMapAsAll(String redisKey) {
        return hashOperation.entries(redisKey);
    }

    public void putValue(String key, T value) {
        valueOperations.set(key, value);
    }

    public void putValueWithExpireTime(String key, T value, long timeout, TimeUnit unit) {
        valueOperations.set(key, value, timeout, unit);
    }

    public T getValue(String key) {
        return valueOperations.get(key);
    }

    public void setExpire(String key, long timeout, TimeUnit unit) {
        redisTemplate.expire(key, timeout, unit);
    }

    public boolean keyExistsInMap(String redisKey){
        return !hashOperation.keys(redisKey).isEmpty();
    }

    public void removeIfExistsInMap(String redisKey, String objectKey){
        if (keyExistsInMap(redisKey)) hashOperation.delete(redisKey, objectKey);
    }
}
