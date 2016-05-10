package com.funcache.internal;

import com.funcache.Configuration;

/**
 * TODO: Class description here.
 *
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
class CleanIdleItemsWorker<K, V> implements Runnable {

    private final FunCacheImpl<K, V> funCache;
    private final Configuration config;

    public CleanIdleItemsWorker(FunCacheImpl<K, V> funCache) {
        this.funCache = funCache;
        this.config = funCache.getConfiguration();
    }

    @Override
    public void run() {
        DataWrapperImpl<K, V> dw = funCache.getMostIdleItem();
        while (dw != null) {
            long idleTime = System.currentTimeMillis() - dw.getLastActivate();
            if (idleTime < config.getMinEvictableIdleTimeMillis() || !dw.isSynced()) {
                break;
            }

            funCache.remove(dw.getKey());
            dw = dw.getNext();
        }
    }
}
