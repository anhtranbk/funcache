package com.funcache.storage;

/**
 * TODO: Class description here.
 *
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
public interface StorageFactory {

    <K, V> CacheStorage<K, V> createCacheStorage();

    PersistentStorage createPersistentStorage();
}
