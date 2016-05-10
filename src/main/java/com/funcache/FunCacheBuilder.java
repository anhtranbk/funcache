package com.funcache;

import com.funcache.exception.ParseConfigurationException;
import com.funcache.internal.FunCacheImpl;
import com.funcache.parser.ConfigurationParser;

import java.io.InputStream;

/**
 * TODO: Class description here.
 *
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
public class FunCacheBuilder<K, V> {

    private Configuration configuration;
    private String fileName;

    public FunCache<K, V> build() {
        if (configuration == null) {
            if (fileName == null) {
                String envPath = System.getenv("funcache.config");
                fileName = envPath != null ? envPath : FunCacheOptions.DEFAULT_FILE_NAME;
            }
            InputStream is = FunCacheBuilder.class.getClassLoader().getResourceAsStream(fileName);
            if (is != null) {
                try {
                    configuration = ConfigurationParser.Factory.fromFile(fileName).parse(is);
                } catch (ParseConfigurationException e) {
                    e.printStackTrace();
                }
            }

            // if not found at least one option file, use default configuration
            if (configuration == null) configuration = new FunCacheOptions();
        }

        return new FunCacheImpl<>(configuration);
    }

    public FunCacheBuilder<K, V> setConfiguration(Configuration configuration) {
        this.configuration = configuration;
        return this;
    }

    public FunCacheBuilder<K, V> setConfigurationFile(String name) {
        this.fileName = name;
        return this;
    }
}
