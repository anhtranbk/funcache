package com.funcache.parser;

import com.funcache.Configuration;
import com.funcache.FunCacheOptions;
import com.funcache.exception.ParseConfigurationException;
import com.funcache.storage.StorageFactory;

import java.io.InputStream;
import java.util.Properties;

/**
 * TODO: Class description here.
 *
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
public class PropertiesFileParser implements ConfigurationParser {

    @Override
    public Configuration parse(InputStream is) throws ParseConfigurationException {
        try {
            Properties properties = new Properties();
            properties.load(is);

            Configuration config = new FunCacheOptions();
            String tmp;

            if ((tmp = properties.getProperty(FunCacheOptions.KEY_MAX_ITEMS)) != null) {
                config.setMaxItems(Integer.parseInt(tmp));
            }
            if ((tmp = properties.getProperty(FunCacheOptions.KEY_CANCEL_SYNC_IF_NOT_LARGER_MIN)) != null) {
                config.setCancelSyncIfNotLargerMin(Boolean.parseBoolean(tmp));
            }
            if ((tmp = properties.getProperty(FunCacheOptions.KEY_MAX_UNSYNCED_ITEMS)) != null) {
                config.setMaxUnsyncedItems(Integer.parseInt(tmp));
            }
            if ((tmp = properties.getProperty(FunCacheOptions.KEY_MIN_EVICTABLE_IDLE_TIME_MILLIS)) != null) {
                config.setMinEvictableIdleTimeMillis(Long.parseLong(tmp));
            }
            if ((tmp = properties.getProperty(FunCacheOptions.KEY_TIME_BETWEEN_EVICTION_RUN_MILLIS)) != null) {
                config.setTimeBetweenEvictionRunsMillis(Long.parseLong(tmp));
            }
            if ((tmp = properties.getProperty(FunCacheOptions.KEY_MIN_ITEMS_TO_SYNC)) != null) {
                config.setMinItemsToSync(Integer.parseInt(tmp));
            }
            if ((tmp = properties.getProperty(FunCacheOptions.KEY_SYNC_INTERVAL)) != null) {
                config.setSyncInterval(Long.parseLong(tmp));
            }
            if ((tmp = properties.getProperty(FunCacheOptions.KEY_OVERRIDE_UNSYNCED_ITEMS)) != null) {
                config.setOverrideUnsyncedItems(Boolean.parseBoolean(tmp));
            }
            if ((tmp = properties.getProperty(FunCacheOptions.KEY_PUT_WHEN_EXCEEDED_MAX_SIZE_BEHAVIOR)) != null) {
                if (!Configuration.KEEP_RECENT.equals(tmp) && !Configuration.REFUSE.equals(tmp)) {
                    throw new ParseConfigurationException("Invalid value for key " +
                            "funcache.putWhenExceededMaxSizeBehavior");
                }
                config.setPutWhenExceededMaxSizeBehavior(tmp);
            }
            if ((tmp = properties.getProperty(FunCacheOptions.KEY_STORAGE_FACTORY)) != null) {
                StorageFactory factory = (StorageFactory) getClass().getClassLoader().loadClass(tmp).newInstance();
                config.setStorageFactory(factory);
            }
            if ((tmp = properties.getProperty(FunCacheOptions.KEY_ALLOW_MULTI_SYNC)) != null) {
                config.setAllowMultiSync(Boolean.parseBoolean(tmp));
            }
            if ((tmp = properties.getProperty(FunCacheOptions.KEY_MAX_RETRY_SYNC_IF_FAILED)) != null) {
                config.setMaxRetrySyncIfFailed(Integer.parseInt(tmp));
            }
            if ((tmp = properties.getProperty(FunCacheOptions.KEY_MAX_SYNC_CONCURRENCY)) != null) {
                config.setMaxSyncConcurrency(Integer.parseInt(tmp));
            }

            return config;
        } catch (Exception e) {
            throw new ParseConfigurationException(e);
        }
    }
}
