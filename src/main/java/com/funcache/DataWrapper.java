package com.funcache;

/**
 * TODO: Class description here.
 *
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
public interface DataWrapper<K, V> {

    K getKey();

    void setKey(K key);

    V getValue();

    void setValue(V value);

    long getLastActivate();

    void setLastActivate(long lastActivate);

    boolean isSynced();

    void setSynced(boolean synced);

}
