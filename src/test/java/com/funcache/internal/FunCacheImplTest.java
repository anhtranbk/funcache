package com.funcache.internal;

import com.funcache.Configuration;
import com.funcache.FunCache;
import com.funcache.FunCacheOptions;
import com.funcache.exception.LimitExceededException;
import com.funcache.impl.StorageFactory;
import com.funcache.storage.CacheStorage;
import junit.framework.Assert;
import junit.framework.TestCase;

import java.util.HashMap;
import java.util.Map;

/**
 * TODO: Class description here.
 *
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
public class FunCacheImplTest extends TestCase {

    private FunCacheImpl<String, String> funCache;
    private Map<String, String> testData = new HashMap<>();

    @Override
    public void setUp() throws Exception {
        super.setUp();

        Configuration config = new FunCacheOptions();
        config.setCancelSyncIfNotLargerMin(true);
        config.setMaxItems(50);
        config.setMaxUnsyncedItems(10);
        config.setMinEvictableIdleTimeMillis(5000);
        config.setMinItemsToSync(5);
        config.setOverrideUnsyncedItems(true);
        config.setSyncInterval(30);
        config.setTimeBetweenEvictionRunsMillis(15000);
        config.setStorageFactory(new StorageFactory());

        funCache = (FunCacheImpl<String, String>) new FunCache.Builder<String, String>()
                .setConfiguration(config)
                .build();

        for (int i = 1; i <= 10; i++) {
            testData.put(String.valueOf(i), String.valueOf(i * 10));
        }
    }

    @Override
    public void tearDown() throws Exception {
        funCache.shutdown();
        funCache = null;
    }

    public void testInit() throws Exception {
        funCache.init(testData);

        CacheStorage<String, DataWrapperImpl<String, String>> cacheStorage = funCache.getCacheStorage();

        assertEquals("10", cacheStorage.get("1").getValue());
        assertEquals("1", cacheStorage.get("1").getKey());
        assertEquals("50", cacheStorage.get("5").getValue());
        assertEquals("5", cacheStorage.get("5").getKey());
        assertEquals("100", cacheStorage.get("10").getValue());
        assertEquals("10", cacheStorage.get("10").getKey());

        assertEquals(cacheStorage.get("1"), cacheStorage.get("2").getPrevious());
        assertEquals(cacheStorage.get("3"), cacheStorage.get("2").getNext());
        assertNull(cacheStorage.get("1").getPrevious());
        assertNull(cacheStorage.get("10").getNext());
    }

    public void testShutDown() throws Exception {

    }

    public void testSize() throws Exception {
        funCache.init(testData);

        assertEquals(testData.size(), funCache.size());
    }

    public void testIsEmpty_001() throws Exception {
        assertTrue(funCache.isEmpty());
    }

    public void testIsEmpty_002() throws Exception {
        funCache.init(testData);

        assertFalse(funCache.isEmpty());
    }

    public void testContains() throws Exception {
        funCache.init(testData);

        assertTrue(funCache.contains("1"));
        assertFalse(funCache.contains("11"));
    }

    public void testGet() throws Exception {
        funCache.init(testData);

        assertEquals(testData.get("1"), funCache.get("1"));
        assertEquals(testData.get("8"), funCache.get("8"));
        assertEquals("40", funCache.get("4"));
        assertNull(funCache.get("abc"));
        assertNull(funCache.get(null));

        assertNotSame(testData.get("3"), funCache.get("5"));
        assertNotSame(testData.get("2"), funCache.get("7"));
        assertNotSame("8", funCache.get("6"));
    }

    public void testPut() throws Exception {
        funCache.init(testData);

        funCache.put("5", "55");
        funCache.put("6", "66");
        funCache.put("11", "110");

        assertNotSame(testData.get("5"), funCache.get("5"));
        assertNotSame(testData.get("6"), funCache.get("6"));
        assertEquals("55", funCache.get("5"));
        assertEquals("66", funCache.get("6"));
        assertEquals("110", funCache.get("11"));
    }

    public void testPut_whenExceededMaxSizeBehavior_keepRecent() throws Exception {
        funCache.getConfiguration().setPutWhenExceededMaxSizeBehavior(Configuration.KEEP_RECENT);
        funCache.getConfiguration().setMaxItems(testData.size());
        funCache.init(testData);

        funCache.put("11", "110");

        assertEquals(testData.size(), funCache.size());
        assertNull(funCache.get("1"));
        assertEquals(testData.get("2"), funCache.getMostIdleItem().getValue());
    }

    public void testPut_whenExceededMaxSizeBehavior_refuse() throws Exception {
        try {
            funCache.getConfiguration().setPutWhenExceededMaxSizeBehavior(Configuration.REFUSE);
            funCache.getConfiguration().setMaxItems(testData.size());
            funCache.init(testData);

            funCache.put("11", "110");
        } catch (LimitExceededException e) {
            assertTrue(true);
            return;
        }

        Assert.fail();
    }

    public void testRemove() throws Exception {
        funCache.init(testData);

        funCache.remove("4");
        funCache.remove("9");

        assertFalse(funCache.contains("4"));
        assertFalse(funCache.contains("9"));
    }

    public void testKetSet() throws Exception {
        funCache.init(testData);

        assertEquals(testData.keySet(), funCache.ketSet());
    }

    public void testValues() throws Exception {
        funCache.init(testData);

        assertEquals(testData.values().size(), funCache.values().size());
    }

    public void testClear() throws Exception {
        funCache.init(testData);
        funCache.clear();

        assertTrue(funCache.isEmpty());
    }

    public void testGetNumberUnsyncedItems() throws Exception {
        funCache.init(testData);

        funCache.put("2", "33");
        funCache.put("5", "99");
        funCache.put("5", "324");
        funCache.put("12", "22");

        assertEquals(3, funCache.getNumberUnsyncedItems());
    }

    public void testGetMostIdleItem() throws Exception {
        funCache.init(testData);
        assertEquals(testData.get("1"), funCache.getMostIdleItem().getValue());

        funCache.remove("1");
        assertEquals(testData.get("2"), funCache.getMostIdleItem().getValue());

        funCache.put("2", "25");
        assertEquals(testData.get("3"), funCache.getMostIdleItem().getValue());
    }

    public void testGetMostRecentItem() throws Exception {
        funCache.init(testData);
        assertEquals(testData.get("10"), funCache.getMostRecentItem().getValue());

        funCache.remove("10");
        assertEquals(testData.get("9"), funCache.getMostRecentItem().getValue());

        funCache.put("5", "34");
        assertEquals("34", funCache.getMostRecentItem().getValue());
    }

}