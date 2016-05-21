package com.funcache.parser;

import com.funcache.Configuration;
import com.funcache.impl.HashMapCacheStorage;
import junit.framework.TestCase;

/**
 * TODO: Class description here.
 *
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
public class PropertiesFileParserTest extends TestCase {

    static final String FUNCACHE_TEST_PROPERTIES_FILE = "funcache-test.properties";

    private final ConfigurationParser parser = ConfigurationParser.Factory.fromFile(FUNCACHE_TEST_PROPERTIES_FILE);

    public void setUp() throws Exception {
        super.setUp();

    }

    public void tearDown() throws Exception {

    }

    public void testParse() throws Exception {
        Configuration config = parser.parse(getClass().getClassLoader().getResourceAsStream(
                FUNCACHE_TEST_PROPERTIES_FILE));

        assertEquals(10000, config.getMaxItems());
        assertEquals(true, config.isOverrideUnsyncedItems());
        assertEquals(3600, config.getTimeBetweenEvictionRunsMillis());
        assertEquals(1800, config.getMinEvictableIdleTimeMillis());
        assertEquals(800, config.getMaxUnsyncedItems());
        assertEquals(500, config.getMinItemsToSync());
        assertEquals(false, config.isCancelSyncIfNotLargerMin());
        assertEquals(600, config.getSyncInterval());
        assertEquals(Configuration.KEEP_RECENT, config.getPutWhenExceededMaxSizeBehavior());
        assertEquals(true, config.isAllowMultiSync());
        assertEquals(10, config.getMaxTryWhenSyncFailed());
        assertEquals(20, config.getMaxSyncConcurrency());

        assertTrue(config.getStorageFactory().createCacheStorage() instanceof HashMapCacheStorage);
    }

}