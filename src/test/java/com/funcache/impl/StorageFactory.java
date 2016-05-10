package com.funcache.impl;

import com.funcache.storage.CacheStorage;
import com.funcache.storage.PersistentStorage;

import java.util.List;

/**
 * TODO: Class description here.
 *
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
public class StorageFactory implements com.funcache.storage.StorageFactory {

    @Override
    public <K, V> CacheStorage<K, V> createCacheStorage() {
        return new HashMapCacheStorage<>();
    }

    @Override
    public PersistentStorage createPersistentStorage() {
        return new PersistentStorage() {
            @Override
            public boolean saveAll(List<Object> data) {
                return true;
            }

            @Override
            public boolean save(Object data) {
                return true;
            }

            @Override
            public boolean contains(Object data) {
                return true;
            }
        };
    }
}
