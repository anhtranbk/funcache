package com.funcache.internal;

import com.funcache.Configuration;
import com.funcache.FunCache;
import com.funcache.exception.LimitExceededException;
import com.funcache.storage.CacheStorage;
import com.funcache.storage.PersistentStorage;
import com.funcache.storage.StorageFactory;
import com.funcache.storage.ThreadSafeStorage;
import com.funcache.util.FastLinkedList;

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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * TODO: Class description here.
 *
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
@SuppressWarnings("unchecked")
public class FunCacheImpl<K, V> implements FunCache<K, V> {

    private final Configuration config;
    private final CacheStorage<K, DataWrapperImpl<K, V>> cacheStorage;
    private final PersistentStorage persistentStorage;
    private final FastLinkedList fastList = FastLinkedList.Factory.create();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

//    private Subscription cleanIdleItemsSub;
//    private Subscription saveToPersistentSub;

    private final Timer cleanTimer = new Timer();
    private final Timer saveToPersistentTimer = new Timer();

    private final AtomicInteger numUnsyncedItems = new AtomicInteger(0);
    private final AtomicBoolean syncThreadRunning = new AtomicBoolean(false);


    public FunCacheImpl(Configuration config) {
        this.config = config;
        this.persistentStorage = config.getStorageFactory().createPersistentStorage();

        CacheStorage<K, DataWrapperImpl<K, V>> storage = config.getStorageFactory().createCacheStorage();
        this.cacheStorage = new ThreadSafeStorage<>(storage);

        startCleanTimer();
        startSaveToPersistentTimer();

//        Observable<Long> o1 = Observable.interval(config.getTimeBetweenEvictionRunsMillis(), TimeUnit.MILLISECONDS);
//        cleanIdleItemsSub = o1.subscribeOn(Schedulers.newThread()).skip(1).subscribe(new Action1<Long>() {
//            @Override
//            public void call(Long aLong) {
//                executor.submit(new CleanIdleItemsWorker<>(FunCacheImpl.this));
//            }
//        });
//        Observable<Long> o2 = Observable.interval(config.getSyncInterval(), TimeUnit.SECONDS);
//        saveToPersistentSub = o2.subscribeOn(Schedulers.newThread()).subscribe(new Action1<Long>() {
//            @Override
//            public void call(Long aLong) {
//                new SaveToPersistentWorker<>(FunCacheImpl.this).run();
//                numUnsyncedItems.set(0);
//            }
//        });
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
            e.printStackTrace();
        }
    }

    @Override
    public void shutdownAsync() {
//        cleanIdleItemsSub.unsubscribe();
//        saveToPersistentSub.unsubscribe();

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
            if (e.getCause() instanceof LimitExceededException) {
                throw (LimitExceededException) e.getCause();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public V remove(K key) {
        try {
            return removeAsync(key).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
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
            e.printStackTrace();
        }
    }

    @Override
    public int getMaxItems() {
        return config.getMaxItems();
    }

    @Override
    public void setMaxItems(int maxItems) {
        config.setMaxItems(maxItems);
    }

    @Override
    public boolean isOverrideUnsyncedItems() {
        return config.isOverrideUnsyncedItems();
    }

    @Override
    public void setOverrideUnsyncedItems(boolean overrideUnsyncedItems) {
        config.setOverrideUnsyncedItems(overrideUnsyncedItems);
    }

    @Override
    public long getTimeBetweenEvictionRunsMillis() {
        return config.getTimeBetweenEvictionRunsMillis();
    }

    @Override
    public void setTimeBetweenEvictionRunsMillis(long timeBetweenEvictionRunsMillis) {
        config.setTimeBetweenEvictionRunsMillis(timeBetweenEvictionRunsMillis);
        cleanTimer.cancel();
        startCleanTimer();
    }

    @Override
    public long getMinEvictableIdleTimeMillis() {
        return config.getMinEvictableIdleTimeMillis();
    }

    @Override
    public void setMinEvictableIdleTimeMillis(long minEvictableIdleTimeMillis) {
        config.setMinEvictableIdleTimeMillis(minEvictableIdleTimeMillis);
    }

    @Override
    public int getMaxUnsyncedItems() {
        return config.getMaxUnsyncedItems();
    }

    @Override
    public void setMaxUnsyncedItems(int maxUnsyncedItems) {
        config.setMaxUnsyncedItems(maxUnsyncedItems);
    }

    @Override
    public int getMinItemsToSync() {
        return config.getMinItemsToSync();
    }

    @Override
    public void setMinItemsToSync(int minItemsToSync) {
        config.setMinItemsToSync(minItemsToSync);
    }

    @Override
    public boolean isCancelSyncIfNotLargerMin() {
        return config.isCancelSyncIfNotLargerMin();
    }

    @Override
    public void setCancelSyncIfNotLargerMin(boolean cancelSyncIfNotLargerMin) {
        config.setCancelSyncIfNotLargerMin(cancelSyncIfNotLargerMin);
    }

    @Override
    public long getSyncInterval() {
        return config.getSyncInterval();
    }

    @Override
    public void setSyncInterval(long syncInterval) {
        config.setSyncInterval(syncInterval);
        saveToPersistentTimer.cancel();
        startSaveToPersistentTimer();
    }

    @Override
    public String getPutWhenExceededMaxSizeBehavior() {
        return config.getPutWhenExceededMaxSizeBehavior();
    }

    @Override
    public void setPutWhenExceededMaxSizeBehavior(String putToFulledPoolBehavior) {
        config.setPutWhenExceededMaxSizeBehavior(putToFulledPoolBehavior);
    }

    @Override
    public StorageFactory getStorageFactory() {
        return config.getStorageFactory();
    }

    @Override
    public void setStorageFactory(StorageFactory storageFactory) {
        throw new UnsupportedOperationException();
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

    void saveToPersistentStorage() {
        if (!syncThreadRunning.get()) {
            syncThreadRunning.set(true);
            Executors.newCachedThreadPool().submit(new SaveToPersistentWorker<>(FunCacheImpl.this));
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
                saveToPersistentStorage();
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

        if (numUnsyncedItems.get() >= config.getMaxUnsyncedItems()) {
            saveToPersistentStorage();
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
                if (idleTime < config.getMinEvictableIdleTimeMillis() || !dw.isSynced()) {
                    break;
                }

                funCache.removeUnsafe(dw.getKey());
                dw = dw.getNext();
                count++;
            }
            System.out.println("[CLEAN] Size: " + funCache.size() + ", cleaned: " + count);
        }
    }

    static final class SaveToPersistentWorker<K, V> implements Runnable {

        private final FunCacheImpl<K, V> funCache;
        private final Configuration config;

        public SaveToPersistentWorker(FunCacheImpl<K, V> funCache) {
            this.funCache = funCache;
            this.config = funCache.getConfiguration();
        }

        @SuppressWarnings("unchecked")
        @Override
        public void run() {
            try {
                final List<DataWrapperImpl<K, V>> forSyncs = funCache.submitTask(new Callable<List<DataWrapperImpl<K, V>>>() {
                    @Override
                    public List<DataWrapperImpl<K, V>> call() throws Exception {
                        final List<DataWrapperImpl<K, V>> forSyncs = new ArrayList<>();
                        DataWrapperImpl<K, V> dw = funCache.getMostRecentItem();
                        while (dw != null) {
                            if (dw.compareAndSetSyncState(DataWrapperImpl.STATE_UNSYNCED,
                                    DataWrapperImpl.STATE_SYNCING)) {
                                forSyncs.add(dw);
                                dw = dw.getPrevious();
                                continue;
                            }
                            break;
                        }
                        return forSyncs;
                    }
                }).get();

                if (config.isCancelSyncIfNotLargerMin() && forSyncs.size() < config.getMinItemsToSync()) {
                    System.out.println("[SYNC] Not enough min item to sync, actual: " + forSyncs.size());
                    return;
                }

                List<V> values = new ArrayList<>(forSyncs.size());
                for (DataWrapperImpl<K, V> dwi : forSyncs) {
                    values.add(dwi.getValue());
                }

                for (int i = 0; i < 5; i++) {
                    if (funCache.getPersistentStorage().saveAll((List<Object>) values)) {
                        funCache.submitTask(new Runnable() {
                            @Override
                            public void run() {
                                for (DataWrapperImpl<K, V> dw : forSyncs) {
                                    if (dw.compareAndSetSyncState(DataWrapperImpl.STATE_SYNCING,
                                            DataWrapperImpl.STATE_SYNCED)) {
                                        funCache.numUnsyncedItems.decrementAndGet();
                                    }
                                }
                                System.out.println("[SYNC] Synced: " + forSyncs.size() + ", current unsynced: "
                                        + funCache.getNumberUnsyncedItems());
                            }
                        }).get();
                        break;
                    }
                }
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            } finally {
                funCache.submitTask(new Runnable() {
                    @Override
                    public void run() {
                        funCache.syncThreadRunning.set(false);
                    }
                });
            }
        }
    }
}
