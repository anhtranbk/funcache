package com.funcache.internal;

import com.funcache.Configuration;
import com.funcache.storage.CacheStorage;
import com.funcache.storage.PersistentStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * TODO: Class description here.
 *
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
class SaveToPersistentWorker<K, V> implements Runnable {

    private final FunCacheImpl<K, V> funCache;
    private final PersistentStorage persistentStorage;
    private final CacheStorage<K, DataWrapperImpl<K, V>> cacheStorage;
    private final Configuration config;

    public SaveToPersistentWorker(FunCacheImpl<K, V> funCache) {
        this.funCache = funCache;
        this.cacheStorage = funCache.getCacheStorage();
        this.persistentStorage = funCache.getPersistentStorage();
        this.config = funCache.getConfiguration();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void run() {
        final List<DataWrapperImpl<K, V>> forSyncs = new ArrayList<>();

        for (K key : funCache.ketSet()) {
            DataWrapperImpl<K, V> dw = cacheStorage.get(key);
            if (dw != null && !dw.isSynced()) {
                forSyncs.add(dw);
            }
        }

        if (!config.isCancelSyncIfNotLargerMin() || forSyncs.size() >= config.getMinItemsToSync()) {
            List<V> values = new ArrayList<>(forSyncs.size());
            for (DataWrapperImpl<K, V> dw : forSyncs) {
                values.add(dw.getValue());
            }

            while (true) {
                if (persistentStorage.saveAll((List<Object>) values)) break;
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            try {
                funCache.submitTask(new Runnable() {
                    @Override
                    public void run() {
                        for (DataWrapperImpl<K, V> dw : forSyncs) {
                            dw.setSynced(true);
                        }
                    }
                }).get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
    }
}
