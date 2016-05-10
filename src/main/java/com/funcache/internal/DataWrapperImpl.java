package com.funcache.internal;

import com.funcache.DataWrapper;
import com.funcache.util.FastLinkedListItem;

/**
 * TODO: Class description here.
 *
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
class DataWrapperImpl<K, V> implements DataWrapper<K, V>, FastLinkedListItem {

    private volatile long lastActivate;
    private boolean synced;
    private K key;
    private V value;

    private DataWrapperImpl<K, V> previous;
    private DataWrapperImpl<K, V> next;

    public DataWrapperImpl(K key, V value) {
        this(key, value, true);
    }

    public DataWrapperImpl(K key, V value, boolean synced) {
        this.key = key;
        this.value = value;
        this.lastActivate = System.currentTimeMillis();
        this.synced = synced;
    }

    @Override
    public DataWrapperImpl<K, V> getPrevious() {
        return previous;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void setPrevious(FastLinkedListItem previous) {
        this.previous = (DataWrapperImpl<K, V>) previous;
    }

    @Override
    public DataWrapperImpl<K, V> getNext() {
        return next;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void setNext(FastLinkedListItem next) {
        this.next = (DataWrapperImpl<K, V>) next;
    }

    public K getKey() {
        return key;
    }

    public void setKey(K key) {
        this.key = key;
    }

    public long getLastActivate() {
        return lastActivate;
    }

    public void setLastActivate(long lastActivate) {
        this.lastActivate = lastActivate;
    }

    public boolean isSynced() {
        return synced;
    }

    public void setSynced(boolean synced) {
        this.synced = synced;
    }

    public V getValue() {
        return value;
    }

    public void setValue(V value) {
        this.value = value;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof DataWrapperImpl) {
            return value.equals(((DataWrapperImpl<K, V>) obj).value);
        }
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
