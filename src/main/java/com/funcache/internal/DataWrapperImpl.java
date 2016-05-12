package com.funcache.internal;

import com.funcache.DataWrapper;
import com.funcache.util.FastLinkedListItem;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * TODO: Class description here.
 *
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
class DataWrapperImpl<K, V> implements DataWrapper<K, V>, FastLinkedListItem, Serializable {

    static final int STATE_SYNCED = 0;
    static final int STATE_UNSYNCED = 1;
    static final int STATE_SYNCING = 2;

    private final AtomicInteger syncState = new AtomicInteger();
    private volatile long lastActivate;
    private K key;
    private volatile V value;

    private volatile DataWrapperImpl<K, V> previous;
    private volatile DataWrapperImpl<K, V> next;

    public DataWrapperImpl(K key, V value) {
        this(key, value, true);
    }

    public DataWrapperImpl(K key, V value, boolean synced) {
        this.key = key;
        this.value = value;
        this.lastActivate = System.currentTimeMillis();
        this.setSynced(synced);
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

    @Override
    public K getKey() {
        return key;
    }

    @Override
    public long getLastActivate() {
        return lastActivate;
    }

    void setLastActivate(long lastActivate) {
        this.lastActivate = lastActivate;
    }

    @Override
    public boolean isSynced() {
        return syncState.get() == STATE_SYNCED;
    }

    void setSynced(boolean synced) {
        this.syncState.set(synced ? STATE_SYNCED : STATE_UNSYNCED);
    }

    @Override
    public V getValue() {
        return value;
    }

    void setValue(V value) {
        this.value = value;
    }

    boolean compareAndSetSyncState(int expected, int update) {
        return syncState.compareAndSet(expected, update);
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof DataWrapperImpl) {
            return key.equals(((DataWrapperImpl<K, V>) obj).key);
        }
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return key.hashCode();
    }

    @Override
    public String toString() {
        return key.toString() + "," + String.valueOf(syncState);
    }
}
