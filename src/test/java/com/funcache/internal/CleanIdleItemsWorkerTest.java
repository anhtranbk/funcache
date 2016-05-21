package com.funcache.internal;

import com.funcache.Configuration;
import com.funcache.FunCache;
import com.funcache.FunCacheOptions;
import com.funcache.impl.StorageFactory;
import com.funcache.storage.CacheStorage;
import junit.framework.TestCase;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.funcache.internal.FunCacheImpl.CleanIdleItemsWorker;

/**
 * TODO: Class description here.
 *
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
public class CleanIdleItemsWorkerTest extends TestCase {

    private FunCacheImpl<String, String> funCache;
    private Map<String, String> testData = new HashMap<>();

    @Override
    public void setUp() throws Exception {
        super.setUp();

        Configuration config = new FunCacheOptions();
        config.setMinEvictableIdleTimeMillis(5000);
        config.setStorageFactory(new StorageFactory());

        for (int i = 1; i <= 10; i++) {
            testData.put(String.valueOf(i), String.valueOf(i * 10));
        }

        funCache = (FunCacheImpl<String, String>) new FunCache.Builder<String, String>()
                .setConfiguration(config)
                .build();
        funCache.init(testData);
    }

    @Override
    protected void tearDown() throws Exception {
        funCache.shutdown();
        funCache = null;
    }

    public void testRun_001() throws Exception {
        CacheStorage<String, DataWrapperImpl<String, String>> cacheStorage = funCache.getCacheStorage();
        DataWrapperImpl<String, String> dw1 = cacheStorage.get("1");
        DataWrapperImpl<String, String> dw2 = cacheStorage.get("2");

        dw1.setLastActivate(System.currentTimeMillis() - TimeUnit.SECONDS.toMillis(10));
        dw1.setSynced(true);

        dw2.setLastActivate(System.currentTimeMillis() - TimeUnit.SECONDS.toMillis(8));
        dw2.setSynced(false);

        CleanIdleItemsWorker<String, String> cleanIdleItemsWorker = new CleanIdleItemsWorker<>(funCache);
        cleanIdleItemsWorker.run();

        assertEquals(testData.size() - 1, funCache.size());
    }

    public void testRun_002() throws Exception {
        CacheStorage<String, DataWrapperImpl<String, String>> cacheStorage = funCache.getCacheStorage();
        DataWrapperImpl<String, String> dw1 = cacheStorage.get("1");
        DataWrapperImpl<String, String> dw2 = cacheStorage.get("2");

        dw1.setLastActivate(System.currentTimeMillis() - TimeUnit.SECONDS.toMillis(10));
        dw1.setSynced(true);

        dw2.setLastActivate(System.currentTimeMillis() - TimeUnit.SECONDS.toMillis(8));
        dw2.setSynced(true);

        CleanIdleItemsWorker<String, String> cleanIdleItemsWorker = new CleanIdleItemsWorker<>(funCache);
        cleanIdleItemsWorker.run();

        assertEquals(testData.size() - 2, funCache.size());
    }

    public void testRun_003() throws Exception {
        CacheStorage<String, DataWrapperImpl<String, String>> cacheStorage = funCache.getCacheStorage();
        DataWrapperImpl<String, String> dw1 = cacheStorage.get("1");
        DataWrapperImpl<String, String> dw2 = cacheStorage.get("2");
        DataWrapperImpl<String, String> dw3 = cacheStorage.get("3");

        dw1.setLastActivate(System.currentTimeMillis() - TimeUnit.SECONDS.toMillis(10));
        dw1.setSynced(true);

        dw2.setLastActivate(System.currentTimeMillis() - TimeUnit.SECONDS.toMillis(8));
        dw2.setSynced(true);

        dw3.setLastActivate(System.currentTimeMillis() - TimeUnit.SECONDS.toMillis(2));
        dw3.setSynced(true);

        CleanIdleItemsWorker<String, String> cleanIdleItemsWorker = new CleanIdleItemsWorker<>(funCache);
        cleanIdleItemsWorker.run();

        assertEquals(testData.size() - 2, funCache.size());
    }

}