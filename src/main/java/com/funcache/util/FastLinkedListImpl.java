package com.funcache.util;

/**
 * TODO: Class description here.
 *
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 *         <p>
 *         Because only one main worker thread can change properties of this class,
 *         use volative properties instead of synchronize methods for best performance
 */
class FastLinkedListImpl implements FastLinkedList {

    private volatile FastLinkedListItem head;
    private volatile FastLinkedListItem tail;

    @Override
    public FastLinkedListItem head() {
        return head;
    }

    @Override
    public FastLinkedListItem tail() {
        return tail;
    }

    @Override
    public void addToFirst(FastLinkedListItem item) {
        if (head != item) {
            remove(item);
            if (head != null) head.setPrevious(item);
            item.setNext(head);
            head = item;

            if (tail == null) tail = item;
        }
    }

    @Override
    public void addToLast(FastLinkedListItem item) {
        if (tail != item) {
            remove(item);
            if (tail != null) tail.setNext(item);
            item.setPrevious(tail);
            tail = item;

            if (head == null) head = item;
        }
    }

    @Override
    public void remove(FastLinkedListItem item) {
        FastLinkedListItem next = item.getNext();
        FastLinkedListItem prev = item.getPrevious();

        if (item == head) {
            head = next;
            if (next != null) next.setPrevious(null);
        } else if (item == tail) {
            tail = prev;
            if (prev != null) prev.setNext(null);
        } else {
            if (next != null) next.setPrevious(prev);
            if (prev != null) prev.setNext(next);
        }
    }

    @Override
    public void removeHead() {
        remove(head);
    }

    @Override
    public void remoteTail() {
        remove(tail);
    }

    @Override
    public void reset() {
        while (head != null) {
            FastLinkedListItem item = head;
            head = item.getNext();
            item.setNext(null);
            item.setPrevious(null);
        }
        tail = null;
    }
}
