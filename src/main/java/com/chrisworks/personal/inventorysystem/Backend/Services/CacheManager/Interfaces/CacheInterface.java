package com.chrisworks.personal.inventorysystem.Backend.Services.CacheManager.Interfaces;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Chris_Eteka
 * @since 6/24/2020
 * @email chriseteka@gmail.com
 */
public interface CacheInterface<T> {

    boolean nonEmpty(String key);

    void cacheDetail(String key, T t, Long id);

    void updateCacheDetail(String key, T t, Long id);

    void removeDetail(String key, Long id);

    List<T> fetchDetailsByKey(String key, CacheDetailTransformer<T> transformer);

    /**
     * A functional Interface whose only job is to convert cache data to their parent class
     *
     * @param <T>
     */
    interface CacheDetailTransformer<T> {

        /**
         * Method responsible for the transformation.
         *
         * An Implementation should be given when this method is called.
         * I used 'Gson()' library in one of my implementations.
         *
         * @param data: 'Set<Map.Entry<Object, T>>'
         * @return 'List<T>'
         */
        List<T> transform(Set<Map.Entry<Object, T>> data);
    }
}
