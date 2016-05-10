package com.funcache.util;

/**
 * TODO: Class description here.
 *
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
public interface FastLinkedListItem {

    FastLinkedListItem getPrevious();

    void setPrevious(FastLinkedListItem previous);

    FastLinkedListItem getNext();

    void setNext(FastLinkedListItem next);
}
