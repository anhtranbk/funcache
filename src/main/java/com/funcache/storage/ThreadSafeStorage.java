package com.funcache.storage;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * TODO: Class description here.
 *
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
public class ThreadSafeStorage<K, V> implements CacheStorage<K, V> {

    private final CacheStorage<K, V> target;

    private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock(true);
    private final Lock r = rwl.readLock();
    private final Lock w = rwl.writeLock();

    public ThreadSafeStorage(CacheStorage<K, V> target) {
        this.target = target;
    }

    @Override
    public int size() {
        r.lock();
        try {
            return target.size();
        } finally {
            r.unlock();
        }
    }

    @Override
    public boolean isEmpty() {
        r.lock();
        try {
            return target.isEmpty();
        } finally {
            r.unlock();
        }
    }

    @Override
    public boolean contains(K key) {
        r.lock();
        try {
            return target.contains(key);
        } finally {
            r.unlock();
        }
    }

    @Override
    public V get(K key) {
        r.lock();
        try {
            return target.get(key);
        } finally {
            r.unlock();
        }
    }

    @Override
    public void put(K key, V value) {
        w.lock();
        try {
            target.put(key, value);
        } finally {
            w.unlock();
        }
    }

    @Override
    public V remove(K key) {
        w.lock();
        try {
            return target.remove(key);
        } finally {
            w.unlock();
        }
    }

    @Override
    public Set<K> ketSet() {
        r.lock();
        try {
            return target.ketSet();
        } finally {
            r.unlock();
        }
    }

    @Override
    public Collection<V> values() {
        r.lock();
        try {
            return target.values();
        } finally {
            r.unlock();
        }
    }

    @Override
    public void clear() {
        w.lock();
        try {
            target.clear();
        } finally {
            w.unlock();
        }
    }
}
