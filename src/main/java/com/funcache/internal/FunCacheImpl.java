package com.funcache.internal;

import com.funcache.Configuration;
import com.funcache.FunCache;
import com.funcache.exception.LimitExceededException;
import com.funcache.storage.CacheStorage;
import com.funcache.storage.PersistentStorage;
import com.funcache.storage.ThreadSafeStorage;
import com.funcache.util.FastLinkedList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.exceptions.OnErrorThrowable;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * TODO: Class description here.
 *
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
@SuppressWarnings(value = {"unchecked", "unused"})
public class FunCacheImpl<K, V> implements FunCache<K, V> {

    private static final Logger LOGGER = LoggerFactory.getLogger(FunCacheImpl.class);

    private final Configuration config;
    private final CacheStorage<K, DataWrapperImpl<K, V>> cacheStorage;
    private final PersistentStorage persistentStorage;
    private final FastLinkedList fastList = FastLinkedList.Factory.create();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private final Timer cleanTimer = new Timer();
    private final Timer saveToPersistentTimer = new Timer();

    final AtomicInteger numUnsyncedItems = new AtomicInteger(0);
    final AtomicInteger numSyncWorkersRunning = new AtomicInteger(0);


    public FunCacheImpl(Configuration config) {
        this.config = config;
        this.persistentStorage = config.getStorageFactory().createPersistentStorage();

        CacheStorage<K, DataWrapperImpl<K, V>> storage = config.getStorageFactory().createCacheStorage();
        this.cacheStorage = new ThreadSafeStorage<>(storage);

        startCleanTimer();
        startSaveToPersistentTimer();

        LOGGER.info("New funcache instance has been created with configuration:");
        LOGGER.info("maxItems=" + config.getMaxItems());
        LOGGER.info("overrideUnsyncedItems=" + config.isOverrideUnsyncedItems());
        LOGGER.info("timeBetweenEvictionRunMillis=" + config.getTimeBetweenEvictionRunsMillis());
        LOGGER.info("minEvictableIdleTimeMills=" + config.getMinEvictableIdleTimeMillis());
        LOGGER.info("maxUnsyncedItems=" + config.getMaxUnsyncedItems());
        LOGGER.info("minItemsToSync=" + config.getMinItemsToSync());
        LOGGER.info("cancelSyncIfNotLargerMin=" + config.isCancelSyncIfNotLargerMin());
        LOGGER.info("maxTryWhenSyncFailed=" + config.getMaxTryWhenSyncFailed());
        LOGGER.info("maxSyncConcurrency=" + config.getMaxSyncConcurrency());
        LOGGER.info("syncInterval=" + config.getSyncInterval());
        LOGGER.info("putWhenExceededMaxSizeBehavior=" + config.getPutWhenExceededMaxSizeBehavior());
        LOGGER.info("storageFactory=" + config.getStorageFactory().getClass().getName());
    }

    /**
     * Clear all current items in cache and init with new items
     *
     * @param map new items
     */
    @Override
    public void init(Map<? extends K, ? extends V> map) {
        if (!cacheStorage.isEmpty()) {
            throw new IllegalStateException("Funcache has already initialized");
        }

        for (K key : map.keySet()) {
            DataWrapperImpl<K, V> dw = new DataWrapperImpl<>(key, map.get(key));
            cacheStorage.put(key, dw);
            fastList.addToLast(dw);
        }
    }

    @Override
    public Future<V> putAsync(final K key, final V value) {
        return executor.submit(new Callable<V>() {
            @Override
            public V call() throws Exception {
                return putUnsafe(key, value);
            }
        });
    }

    @Override
    public Future<V> removeAsync(final K key) {
        return executor.submit(new Callable<V>() {
            @Override
            public V call() throws Exception {
                return removeUnsafe(key);
            }
        });
    }

    @Override
    public Future<?> clearAsync() {
        return executor.submit(new Runnable() {
            @Override
            public void run() {
                clearUnsafe();
            }
        });
    }

    @Override
    public void shutdown() {
        shutdownAsync();
        try {
            executor.awaitTermination(60, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            LOGGER.warn(e.getMessage(), e);
        }
    }

    @Override
    public void shutdownAsync() {
        cleanTimer.cancel();
        saveToPersistentTimer.cancel();
        executor.shutdown();
    }

    @Override
    public boolean isShutdown() {
        return executor.isShutdown();
    }

    @Override
    public Configuration getConfiguration() {
        return this.config;
    }

    @Override
    public int size() {
        return cacheStorage.size();
    }

    @Override
    public boolean isEmpty() {
        return cacheStorage.isEmpty();
    }

    @Override
    public boolean contains(K key) {
        return cacheStorage.contains(key);
    }

    @Override
    public V get(K key) {
        DataWrapperImpl<K, V> dw = cacheStorage.get(key);
        return dw != null ? dw.getValue() : null;
    }

    @Override
    public void put(K key, V value) {
        try {
            putAsync(key, value).get();
        } catch (ExecutionException e) {
            if (e.getCause() instanceof LimitExceededException)
                throw (LimitExceededException) e.getCause();
        } catch (InterruptedException e) {
            LOGGER.warn(e.getMessage(), e);
        }
    }

    @Override
    public V remove(K key) {
        try {
            return removeAsync(key).get();
        } catch (InterruptedException | ExecutionException e) {
            LOGGER.warn(e.getMessage(), e);
        }
        return null;
    }

    @Override
    public Set<K> ketSet() {
        return cacheStorage.ketSet();
    }

    @Override
    public Collection<V> values() {
        List<V> values = new ArrayList<>(cacheStorage.size());
        for (DataWrapperImpl<K, V> dw : cacheStorage.values()) {
            values.add(dw.getValue());
        }
        return values;
    }

    @Override
    public void clear() {
        try {
            clearAsync().get();
        } catch (InterruptedException | ExecutionException e) {
            LOGGER.warn(e.getMessage(), e);
        }
    }

    public int getNumberUnsyncedItems() {
        return numUnsyncedItems.get();
    }

    DataWrapperImpl<K, V> getMostIdleItem() {
        return (DataWrapperImpl<K, V>) fastList.head();
    }

    DataWrapperImpl<K, V> getMostRecentItem() {
        return (DataWrapperImpl<K, V>) fastList.tail();
    }

    CacheStorage<K, DataWrapperImpl<K, V>> getCacheStorage() {
        return cacheStorage;
    }

    PersistentStorage getPersistentStorage() {
        return persistentStorage;
    }

    <T> Future<T> submitTask(Callable<T> callable) {
        return executor.submit(callable);
    }

    Future<?> submitTask(Runnable runnable) {
        return executor.submit(runnable);
    }

    void startSyncToPersistentWorker() {
        if (numSyncWorkersRunning.get() < config.getMaxSyncConcurrency()) {
            numSyncWorkersRunning.incrementAndGet();
            executor.submit(new SaveToPersistentWorker<>(this));
            LOGGER.info("Start new sync worker, current sync running: " + numSyncWorkersRunning.get());
        }
    }

    void startCleanTimer() {
        cleanTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                executor.submit(new CleanIdleItemsWorker<>(FunCacheImpl.this));
            }
        }, config.getTimeBetweenEvictionRunsMillis(), config.getTimeBetweenEvictionRunsMillis());
    }

    void startSaveToPersistentTimer() {
        saveToPersistentTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                startSyncToPersistentWorker();
            }
        }, config.getSyncInterval() * 1000, config.getSyncInterval() * 1000);
    }

    V putUnsafe(K key, V value) {
        if (cacheStorage.size() >= config.getMaxItems()) {
            if (config.getPutWhenExceededMaxSizeBehavior().equals(Configuration.REFUSE)) {
                throw new LimitExceededException();
            } else {
                removeUnsafe(getMostIdleItem().getKey());
            }
        }

        if (cacheStorage.contains(key)) {
            DataWrapperImpl<K, V> dw = cacheStorage.get(key);
            if (config.isOverrideUnsyncedItems() || dw.isSynced()) {
                dw.setLastActivate(System.currentTimeMillis());
                dw.setValue(value);
                if (dw.isSynced()) {
                    numUnsyncedItems.incrementAndGet();
                }
                dw.setSynced(false);
                fastList.addToLast(dw);
            }
        } else {
            DataWrapperImpl<K, V> dw = new DataWrapperImpl<>(key, value, false);
            cacheStorage.put(key, dw);
            fastList.addToLast(dw);
            numUnsyncedItems.incrementAndGet();
        }

        if (numUnsyncedItems.get() >= config.getMaxUnsyncedItems() && numSyncWorkersRunning.get() == 0) {
            startSyncToPersistentWorker();
        }
        return value;
    }

    V removeUnsafe(K key) {
        fastList.remove(cacheStorage.get(key));
        return cacheStorage.remove(key).getValue();
    }

    void clearUnsafe() {
        fastList.reset();
        cacheStorage.clear();
    }

    static final class CleanIdleItemsWorker<K, V> implements Runnable {

        private final FunCacheImpl<K, V> funCache;
        private final Configuration config;

        public CleanIdleItemsWorker(FunCacheImpl<K, V> funCache) {
            this.funCache = funCache;
            this.config = funCache.getConfiguration();
        }

        @Override
        public void run() {
            int count = 0;
            DataWrapperImpl<K, V> dw = funCache.getMostIdleItem();
            while (dw != null) {
                long idleTime = System.currentTimeMillis() - dw.getLastActivate();
                if (idleTime < config.getMinEvictableIdleTimeMillis() || !dw.isSynced()) break;

                DataWrapperImpl<K, V> next = dw.getNext();
                funCache.removeUnsafe(dw.getKey());
                dw = next;
                count++;
            }
            LOGGER.info("[CLEAN] Size: " + funCache.size() + ", cleaned: " + count);
        }
    }

    static final class SaveToPersistentWorker<K, V> implements Runnable {

        private final FunCacheImpl<K, V> funCache;
        private final Configuration config;

        public SaveToPersistentWorker(FunCacheImpl<K, V> funCache) {
            this.funCache = funCache;
            this.config = funCache.getConfiguration();
        }

        @Override
        public void run() {
            final List<DataWrapperImpl<K, V>> forSyncs = getListUnsynedItems();
            if (config.isCancelSyncIfNotLargerMin() && forSyncs.size() < config.getMinItemsToSync()) {
                LOGGER.info("[SYNC] Not enough items to sync, actual: " + forSyncs.size());
                funCache.numSyncWorkersRunning.decrementAndGet();
                return;
            }

            Observable.fromCallable(new Callable<Boolean>() {
                @Override
                public Boolean call() throws Exception {
                    final List<V> values = new ArrayList<>(forSyncs.size());
                    for (DataWrapperImpl<K, V> dw : forSyncs) {
                        values.add(dw.getValue());
                    }

                    final int maxTry = config.getMaxTryWhenSyncFailed();
                    int i = 0;
                    while (maxTry < 0 || i++ < config.getMaxTryWhenSyncFailed()) {
                        if (funCache.getPersistentStorage().saveAll((List<Object>) values)) return true;
                    }
                    throw OnErrorThrowable.from(new RuntimeException());
                }
            }).observeOn(Schedulers.io()).subscribeOn(Schedulers.computation()).subscribe(new Action1<Boolean>() {
                @Override
                public void call(final Boolean success) {
                    funCache.submitTask(new Runnable() {
                        @Override
                        public void run() {
                            for (DataWrapperImpl<K, V> dw : forSyncs) {
                                if (dw.compareAndSetSyncState(DataWrapperImpl.STATE_SYNCING,
                                        DataWrapperImpl.STATE_SYNCED)) {
                                    funCache.numUnsyncedItems.decrementAndGet();
                                }
                            }
                            funCache.numSyncWorkersRunning.decrementAndGet();
                            LOGGER.info("[SYNC] Synced: " + forSyncs.size() + ", current unsynced: "
                                    + funCache.getNumberUnsyncedItems());
                        }
                    });
                }
            }, new Action1<Throwable>() {
                @Override
                public void call(Throwable throwable) {
                    funCache.submitTask(new Runnable() {
                        @Override
                        public void run() {
                            for (DataWrapperImpl<K, V> dw : forSyncs) {
                                dw.compareAndSetSyncState(DataWrapperImpl.STATE_SYNCING,
                                        DataWrapperImpl.STATE_UNSYNCED);
                            }
                            funCache.numSyncWorkersRunning.decrementAndGet();
                        }
                    });
                }
            });
        }

        private List<DataWrapperImpl<K, V>> getListUnsynedItems() {
            final List<DataWrapperImpl<K, V>> forSyncs = new ArrayList<>();
            for (DataWrapperImpl<K, V> dw = funCache.getMostRecentItem(); dw != null; dw = dw.getPrevious()) {
                if (dw.compareAndSetSyncState(DataWrapperImpl.STATE_UNSYNCED, DataWrapperImpl.STATE_SYNCING)) {
                    forSyncs.add(dw);
                    continue;
                }
                break;
            }
            return forSyncs;
        }
    }
}
