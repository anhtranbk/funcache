package com.funcache;

import com.funcache.exception.ParseConfigurationException;
import com.funcache.internal.FunCacheImpl;
import com.funcache.parser.ConfigurationParser;
import com.funcache.storage.CacheStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.Future;

/**
 * TODO: Class description here.
 *
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
public interface FunCache<K, V> extends CacheStorage<K, V> {

    Configuration getConfiguration();

    void init(Map<? extends K, ? extends V> map);

    Future<V> putAsync(K key, V value);

    Future<V> removeAsync(K key);

    Future<?> clearAsync();

    void shutdown();

    void shutdownAsync();

    boolean isShutdown();


    class Builder<K, V> {

        private static final Logger LOGGER = LoggerFactory.getLogger(FunCache.Builder.class);

        private Configuration configuration;
        private String fileName;

        public FunCache<K, V> build() {
            if (configuration == null) {
                if (fileName == null) {
                    String envPath = System.getenv("funcache.config");
                    fileName = envPath != null ? envPath : FunCacheOptions.DEFAULT_FILE_NAME;
                }
                InputStream is = FunCache.class.getClassLoader().getResourceAsStream(fileName);
                if (is != null) {
                    try {
                        configuration = ConfigurationParser.Factory.fromFile(fileName).parse(is);
                    } catch (ParseConfigurationException e) {
                        LOGGER.warn(e.getMessage(), e);
                    }
                }

                // if not found at least one option file, use default configuration
                if (configuration == null) configuration = new FunCacheOptions();
            }

            return new FunCacheImpl<>(configuration);
        }

        public Builder<K, V> setConfiguration(Configuration configuration) {
            this.configuration = configuration;
            return this;
        }

        public Builder<K, V> setConfigurationFile(String name) {
            this.fileName = name;
            return this;
        }
    }
}
