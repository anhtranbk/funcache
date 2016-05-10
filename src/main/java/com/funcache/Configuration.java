package com.funcache;

import com.funcache.storage.StorageFactory;

/**
 * TODO: Class description here.
 *
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
public interface Configuration {

    String KEEP_RECENT = "KEEP_RECENT";
    String REFUSE = "REFUSE";

    int getMaxItems();

    void setMaxItems(int maxItems);

    boolean isOverrideUnsyncedItems();

    void setOverrideUnsyncedItems(boolean overrideUnsyncedItems);

    long getTimeBetweenEvictionRunsMillis();

    void setTimeBetweenEvictionRunsMillis(long timeBetweenEvictionRunsMillis);

    long getMinEvictableIdleTimeMillis();

    void setMinEvictableIdleTimeMillis(long minEvictableIdleTimeMillis);

    int getMaxUnsyncedItems();

    void setMaxUnsyncedItems(int maxUnsyncedItems);

    int getMinItemsToSync();

    void setMinItemsToSync(int minItemsToSync);

    boolean isCancelSyncIfNotLargerMin();

    void setCancelSyncIfNotLargerMin(boolean cancelSyncIfNotLargerMin);

    long getSyncInterval();

    void setSyncInterval(long syncInterval);

    String getPutWhenExceededMaxSizeBehavior();

    void setPutWhenExceededMaxSizeBehavior(String putToFulledPoolBehavior);

    StorageFactory getStorageFactory();

    void setStorageFactory(StorageFactory storageFactory);

}
