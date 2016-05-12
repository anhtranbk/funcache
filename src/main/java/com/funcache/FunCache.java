package com.funcache;

import com.funcache.storage.CacheStorage;

import java.util.Map;
import java.util.concurrent.Future;

/**
 * TODO: Class description here.
 *
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
public interface FunCache<K, V> extends CacheStorage<K, V>, Configuration {

    Configuration getConfiguration();

    void init(Map<? extends K, ? extends V> map);

    Future<V> putAsync(K key, V value);

    Future<V> removeAsync(K key);

    Future<?> clearAsync();

    void shutdown();

    void shutdownAsync();

    boolean isShutdown();
}
