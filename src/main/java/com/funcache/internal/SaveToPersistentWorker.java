package com.funcache.internal;

import com.funcache.Configuration;
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
    private final Configuration config;

    public SaveToPersistentWorker(FunCacheImpl<K, V> funCache) {
        this.funCache = funCache;
        this.persistentStorage = funCache.getPersistentStorage();
        this.config = funCache.getConfiguration();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void run() {
        final List<DataWrapperImpl<K, V>> forSyncs = new ArrayList<>();

        DataWrapperImpl<K, V> dw = funCache.getMostRecentItem();
        while (dw != null) {
            if (dw.isSynced()) break;
            forSyncs.add(dw);
            dw = dw.getPrevious();
        }
        if (forSyncs.isEmpty()) return;

        if (!config.isCancelSyncIfNotLargerMin() || forSyncs.size() >= config.getMinItemsToSync()) {
            List<V> values = new ArrayList<>(forSyncs.size());
            for (DataWrapperImpl<K, V> dwi : forSyncs) {
                values.add(dwi.getValue());
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
