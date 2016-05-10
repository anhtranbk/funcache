package com.funcache.parser;

import com.funcache.Configuration;
import com.funcache.exception.ParseConfigurationException;

import java.io.InputStream;

/**
 * TODO: Class description here.
 *
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
public interface ConfigurationParser {

    Configuration parse(InputStream is) throws ParseConfigurationException;

    class Factory {

        public static ConfigurationParser fromFile(String fileName) {
            if (fileName.endsWith(".properties")) {
                return new PropertiesFileParser();
            } else if (fileName.endsWith(".yml")) {
                return new YmlFileParser();
            } else throw new UnsupportedOperationException();
        }
    }
}
