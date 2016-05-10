package com.funcache.storage;

import java.util.Collection;
import java.util.Set;

/**
 * TODO: Class description here.
 *
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
public interface CacheStorage<K, V> {

    int size();

    boolean isEmpty();

    boolean contains(K key);

    V get(K key);

    void put(K key, V value);

    V remove(K key);

    Set<K> ketSet();

    Collection<V> values();

    void clear();

}
