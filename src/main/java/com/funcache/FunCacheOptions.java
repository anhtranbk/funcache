package com.funcache;

import com.funcache.storage.StorageFactory;

import java.util.concurrent.TimeUnit;

/**
 * TODO: Class description here.
 *
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
public class FunCacheOptions implements Configuration {

    public static final String DEFAULT_FILE_NAME = "funcache.properties";

    public static final String KEY_MAX_ITEMS = "funcache.maxItems";
    public static final String KEY_OVERRIDE_UNSYNCED_ITEMS = "funcache.overrideUnsyncedItems";
    public static final String KEY_TIME_BETWEEN_EVICTION_RUN_MILLIS = "funcache.timeBetweenEvictionRunMillis";
    public static final String KEY_MIN_EVICTABLE_IDLE_TIME_MILLIS = "funcache.minEvictableIdleTimeMills";
    public static final String KEY_MAX_UNSYNCED_ITEMS = "funcache.maxUnsyncedItems";
    public static final String KEY_MIN_ITEMS_TO_SYNC = "funcache.minItemsToSync";
    public static final String KEY_CANCEL_SYNC_IF_NOT_LARGER_MIN = "funcache.cancelSyncIfNotLargerMin";
    public static final String KEY_SYNC_INTERVAL = "funcache.syncInterval";
    public static final String KEY_PUT_WHEN_EXCEEDED_MAX_SIZE_BEHAVIOR = "funcache.putWhenExceededMaxSizeBehavior";
    public static final String KEY_STORAGE_FACTORY = "funcache.storageFactory";

    public static final int DEFAULT_MAX_ITEMS = Integer.MAX_VALUE;
    public static final boolean DEFAULT_OVERRIDE_UNSYNCED_ITEMS = true;
    public static final long DEFAULT_TIME_BETWEEN_EVICTION_RUN_MILLIS = TimeUnit.DAYS.toSeconds(1);
    public static final long DEFAULT_MIN_EVICTABLE_IDLE_TIME_MILLIS = TimeUnit.DAYS.toSeconds(5);
    public static final int DEFAULT_MAX_UNSYNCED_ITEMS = DEFAULT_MAX_ITEMS;
    public static final int DEFAULT_MIN_ITEMS_TO_SYNC = 500;
    public static final boolean DEFAULT_CANCEL_SYNC_IF_NOT_LARGER_MIN = true;
    public static final long DEFAULT_SYNC_INTERVAL = TimeUnit.MINUTES.toSeconds(10);

    private volatile int maxItems = DEFAULT_MAX_ITEMS;
    private volatile boolean overrideUnsyncedItems = DEFAULT_OVERRIDE_UNSYNCED_ITEMS;

    private volatile long timeBetweenEvictionRunsMillis = DEFAULT_TIME_BETWEEN_EVICTION_RUN_MILLIS;
    private volatile long minEvictableIdleTimeMillis = DEFAULT_MIN_EVICTABLE_IDLE_TIME_MILLIS;

    private volatile int maxUnsyncedItems = DEFAULT_MAX_UNSYNCED_ITEMS;
    private volatile int minItemsToSync = DEFAULT_MIN_ITEMS_TO_SYNC;
    private volatile boolean cancelSyncIfNotLargerMin = DEFAULT_CANCEL_SYNC_IF_NOT_LARGER_MIN;
    private volatile long syncInterval = DEFAULT_SYNC_INTERVAL;

    private String putWhenExceededMaxSizeBehavior = KEEP_RECENT;

    private StorageFactory storageFactory;

    public int getMaxItems() {
        return maxItems;
    }

    public void setMaxItems(int maxItems) {
        this.maxItems = maxItems;
    }

    public boolean isOverrideUnsyncedItems() {
        return overrideUnsyncedItems;
    }

    public void setOverrideUnsyncedItems(boolean overrideUnsyncedItems) {
        this.overrideUnsyncedItems = overrideUnsyncedItems;
    }

    public long getTimeBetweenEvictionRunsMillis() {
        return timeBetweenEvictionRunsMillis;
    }

    public void setTimeBetweenEvictionRunsMillis(long timeBetweenEvictionRunsMillis) {
        this.timeBetweenEvictionRunsMillis = timeBetweenEvictionRunsMillis;
    }

    public long getMinEvictableIdleTimeMillis() {
        return minEvictableIdleTimeMillis;
    }

    public void setMinEvictableIdleTimeMillis(long minEvictableIdleTimeMillis) {
        this.minEvictableIdleTimeMillis = minEvictableIdleTimeMillis;
    }

    public int getMaxUnsyncedItems() {
        return maxUnsyncedItems;
    }

    public void setMaxUnsyncedItems(int maxUnsyncedItems) {
        this.maxUnsyncedItems = maxUnsyncedItems;
    }

    public int getMinItemsToSync() {
        return minItemsToSync;
    }

    public void setMinItemsToSync(int minItemsToSync) {
        this.minItemsToSync = minItemsToSync;
    }

    public boolean isCancelSyncIfNotLargerMin() {
        return cancelSyncIfNotLargerMin;
    }

    public void setCancelSyncIfNotLargerMin(boolean cancelSyncIfNotLargerMin) {
        this.cancelSyncIfNotLargerMin = cancelSyncIfNotLargerMin;
    }

    public long getSyncInterval() {
        return syncInterval;
    }

    public void setSyncInterval(long syncInterval) {
        this.syncInterval = syncInterval;
    }

    public String getPutWhenExceededMaxSizeBehavior() {
        return putWhenExceededMaxSizeBehavior;
    }

    public void setPutWhenExceededMaxSizeBehavior(String putToFulledPoolBehavior) {
        this.putWhenExceededMaxSizeBehavior = putToFulledPoolBehavior;
    }

    public StorageFactory getStorageFactory() {
        return storageFactory;
    }

    public void setStorageFactory(StorageFactory storageFactory) {
        this.storageFactory = storageFactory;
    }
}
