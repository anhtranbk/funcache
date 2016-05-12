package com.funcache;

/**
 * TODO: Class description here.
 *
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
public interface DataWrapper<K, V> {

    K getKey();

    V getValue();

    long getLastActivate();

    boolean isSynced();

}
