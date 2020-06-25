package com.chrisworks.personal.inventorysystem.Backend.Services.CacheManager;

import com.chrisworks.personal.inventorysystem.Backend.Configurations.RedisUtil;
import com.chrisworks.personal.inventorysystem.Backend.Services.CacheManager.Interfaces.CacheInterface;
import com.chrisworks.personal.inventorysystem.Backend.Utility.AuthenticatedUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author Chris_Eteka
 * @since 6/24/2020
 * @email chriseteka@gmail.com
 */
@Component
@RequiredArgsConstructor
public class CacheManagerImpl<T> implements CacheInterface<T> {

    private final RedisUtil<T> redisUtil;

    private String REDIS_TABLE_KEY(String key) {
        return String.format("TABLE_%s_%s", key, AuthenticatedUserDetails.getUserFullName());
    }

    private String OBJECT_KEY(String key, Long id) {
        return String.format("%s_%d", key, id);
    }

    @Override
    public void cacheDetail(String key, T t, Long id) {
        String redisTableKey = REDIS_TABLE_KEY(key);
        redisUtil.putMap(redisTableKey, OBJECT_KEY(key, id), t);
        redisUtil.setExpire(redisTableKey, 18, TimeUnit.HOURS);
    }

    @Override
    public void updateCacheDetail(String key, T t, Long id) {
        removeDetail(key, id);
        cacheDetail(key, t, id);
    }

    @Override
    public boolean nonEmpty(String key) {
        return redisUtil.keyExistsInMap(REDIS_TABLE_KEY(key));
    }

    @Override
    public void removeDetail(String key, Long id) {
        redisUtil.removeIfExistsInMap(REDIS_TABLE_KEY(key), OBJECT_KEY(key, id));
    }

    @Override
    public List<T> fetchDetailsByKey(String key, CacheDetailTransformer<T> transformer) {
        return transformer.transform(redisUtil.getMapAsAll(REDIS_TABLE_KEY(key)).entrySet());
    }
}
