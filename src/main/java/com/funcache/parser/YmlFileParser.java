package com.funcache.parser;

import com.funcache.Configuration;
import com.funcache.exception.ParseConfigurationException;

import java.io.InputStream;

/**
 * TODO: Class description here.
 *
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
public class YmlFileParser implements ConfigurationParser {

    @Override
    public Configuration parse(InputStream is) throws ParseConfigurationException {
        throw new UnsupportedOperationException("This format current not supported");
    }
}
