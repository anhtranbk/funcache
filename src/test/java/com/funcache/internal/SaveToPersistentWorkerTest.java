package com.funcache.internal;

import com.funcache.Configuration;
import com.funcache.FunCache;
import com.funcache.FunCacheOptions;
import com.funcache.impl.StorageFactory;
import junit.framework.TestCase;

import java.util.HashMap;
import java.util.Map;

import static com.funcache.internal.FunCacheImpl.SaveToPersistentWorker;

/**
 * TODO: Class description here.
 *
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
public class SaveToPersistentWorkerTest extends TestCase {

    private FunCacheImpl<String, String> funCache;
    private Map<String, String> testData = new HashMap<>();

    @Override
    public void setUp() throws Exception {
        super.setUp();

        Configuration config = new FunCacheOptions();
        config.setStorageFactory(new StorageFactory());

        for (int i = 1; i <= 10; i++) {
            testData.put(String.valueOf(i), String.valueOf(i * 10));
        }

        funCache = (FunCacheImpl<String, String>) new FunCache.Builder<String, String>()
                .setConfiguration(config)
                .build();
        funCache.init(testData);

        funCache.put("1", "11");
        funCache.put("3", "23");
        funCache.put("5", "44");
    }

    @Override
    protected void tearDown() throws Exception {
        funCache.shutdown();
        funCache = null;
    }

    public void testRun_001() throws Exception {
        funCache.getConfiguration().setMinItemsToSync(5);
        funCache.getConfiguration().setCancelSyncIfNotLargerMin(false);

        SaveToPersistentWorker<String, String> saveToPersistentWorker = new SaveToPersistentWorker<>(funCache);
        funCache.numSyncWorkersRunning.incrementAndGet();
        funCache.submitTask(saveToPersistentWorker);
        while (funCache.numSyncWorkersRunning.get() > 0) {
            Thread.sleep(100);
        }

        assertTrue(funCache.getCacheStorage().get("1").isSynced());
        assertTrue(funCache.getCacheStorage().get("3").isSynced());
        assertTrue(funCache.getCacheStorage().get("5").isSynced());
    }

    public void testRun_002() throws Exception {
        funCache.getConfiguration().setMinItemsToSync(5);
        funCache.getConfiguration().setCancelSyncIfNotLargerMin(true);

        SaveToPersistentWorker<String, String> saveToPersistentWorker = new SaveToPersistentWorker<>(funCache);
        saveToPersistentWorker.run();

        assertFalse(funCache.getCacheStorage().get("1").isSynced());
        assertFalse(funCache.getCacheStorage().get("3").isSynced());
        assertFalse(funCache.getCacheStorage().get("5").isSynced());
    }

    public void testRun_003() throws Exception {
        funCache.getConfiguration().setMinItemsToSync(2);
        funCache.getConfiguration().setCancelSyncIfNotLargerMin(true);

        SaveToPersistentWorker<String, String> saveToPersistentWorker = new SaveToPersistentWorker<>(funCache);
        funCache.numSyncWorkersRunning.incrementAndGet();
        funCache.submitTask(saveToPersistentWorker);
        while (funCache.numSyncWorkersRunning.get() > 0) {
            Thread.sleep(100);
        }

        assertTrue(funCache.getCacheStorage().get("1").isSynced());
        assertTrue(funCache.getCacheStorage().get("3").isSynced());
        assertTrue(funCache.getCacheStorage().get("5").isSynced());
    }

}