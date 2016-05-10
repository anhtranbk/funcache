package com.funcache.exception;

import java.io.IOException;

/**
 * TODO: Class description here.
 *
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
public class ParseConfigurationException extends IOException {

    public ParseConfigurationException() {

    }

    public ParseConfigurationException(String message) {
        super(message);
    }

    public ParseConfigurationException(Exception e) {
        super(e);
    }
}
