package com.funcache.util;

/**
 * TODO: Class description here.
 *
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
public interface FastLinkedList {

    FastLinkedListItem head();

    FastLinkedListItem tail();

    void addToFirst(FastLinkedListItem item);

    void addToLast(FastLinkedListItem item);

    void remove(FastLinkedListItem item);

    void removeHead();

    void remoteTail();

    void reset();

    /**
     * ItemDoesNotExistException
     */
    class ItemDoesNotExistException extends Exception {
    }

    /**
     * Factory class create default implement of FunLinkedList
     */
    class Factory {

        public static FastLinkedList create() {
            return new FastLinkedListImpl();
        }
    }
}
