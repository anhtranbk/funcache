package com.funcache.util;

import junit.framework.TestCase;

/**
 * TODO: Class description here.
 *
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
public class FastLinkedListImplTest extends TestCase {

    private FastLinkedList list;

    public void setUp() throws Exception {
        super.setUp();
        list = FastLinkedList.Factory.create();
    }

    public void tearDown() throws Exception {

    }

    public void testHead() throws Exception {
        Item itemA = new Item("item A");
        Item itemB = new Item("item B");

        list.addToLast(itemA);
        list.addToLast(itemB);

        assertEquals(itemA, list.head());
    }

    public void testTail() throws Exception {
        Item itemA = new Item("item A");
        Item itemB = new Item("item B");

        list.addToLast(itemA);
        list.addToLast(itemB);

        assertEquals(itemB, list.tail());
    }

    public void testAddToFirst() throws Exception {
        Item itemA = new Item("item A");
        Item itemB = new Item("item B");

        list.addToFirst(itemA);
        list.addToFirst(itemB);

        assertEquals(itemB.getNext(), itemA);
        assertNull(itemB.getPrevious());
        assertEquals(itemA.getPrevious(), itemB);
        assertNull(itemA.getNext());
        assertEquals(list.head(), itemB);
        assertEquals(list.tail(), itemA);
    }

    public void testAddToLast() throws Exception {
        Item itemA = new Item("item A");
        Item itemB = new Item("item B");

        list.addToLast(itemA);
        list.addToLast(itemB);

        assertEquals(itemA.getNext(), itemB);
        assertNull(itemA.getPrevious());
        assertEquals(itemB.getPrevious(), itemA);
        assertNull(itemB.getNext());
        assertEquals(list.head(), itemA);
        assertEquals(list.tail(), itemB);
    }

    public void testMoveExistItemToLast() throws Exception {
        Item itemA = new Item("item A");
        Item itemB = new Item("item B");
        Item itemC = new Item("item C");
        Item itemD = new Item("item D");

        list.addToLast(itemA);
        list.addToLast(itemB);
        list.addToLast(itemC);
        list.addToLast(itemD);

        list.addToLast(itemD);
        assertEquals(list.tail(), itemD);

        list.addToLast(itemA);
        assertEquals(list.head(), itemB);
        assertEquals(list.tail(), itemA);
        assertNull(itemB.getPrevious());
        assertNull(itemA.getNext());

        list.addToLast(itemC);
        assertEquals(itemD, itemB.getNext());
        assertEquals(itemD.getPrevious(), itemB);
        assertEquals(itemA.getNext(), itemC);
        assertEquals(list.tail(), itemC);
        assertEquals(itemC.getPrevious(), itemA);
        assertNull(itemC.getNext());
    }

    public void testRemove() throws Exception {
        Item itemA = new Item("item A");
        Item itemB = new Item("item B");
        Item itemC = new Item("item C");
        Item itemD = new Item("item D");
        Item itemE = new Item("item E");
        Item itemF = new Item("item F");

        list.addToLast(itemA);
        list.addToLast(itemB);
        list.addToLast(itemC);
        list.addToLast(itemD);
        list.addToLast(itemE);
        list.addToLast(itemF);

        list.remove(itemA);
        assertEquals(list.head(), itemB);
        assertNull(itemB.getPrevious());

        list.remove(itemF);
        assertEquals(list.tail(), itemE);
        assertNull(itemE.getNext());

        list.remove(itemC);
        assertEquals(itemD, itemB.getNext());
        assertEquals(itemB, itemD.getPrevious());
    }

    public void testRemoveHead() throws Exception {
        Item itemA = new Item("item A");
        Item itemB = new Item("item B");
        Item itemC = new Item("item C");

        list.addToLast(itemA);
        list.addToLast(itemB);
        list.addToLast(itemC);

        list.removeHead();

        assertEquals(list.head(), itemB);
    }

    public void testRemoteTail() throws Exception {
        Item itemA = new Item("item A");
        Item itemB = new Item("item B");
        Item itemC = new Item("item C");

        list.addToLast(itemA);
        list.addToLast(itemB);
        list.addToLast(itemC);

        list.remoteTail();

        assertEquals(list.tail(), itemB);
    }

    public void testReset() throws Exception {
        Item itemA = new Item("item A");
        Item itemB = new Item("item B");
        Item itemC = new Item("item C");

        list.addToLast(itemA);
        list.addToLast(itemB);
        list.addToLast(itemC);

        list.reset();

        assertNull(itemA.getNext());
        assertNull(itemB.getNext());
        assertNull(itemB.getPrevious());
        assertNull(itemC.getPrevious());
        assertNull(list.head());
        assertNull(list.tail());
    }

    static class Item implements FastLinkedListItem {

        private FastLinkedListItem prev;
        private FastLinkedListItem next;
        private String data;

        public Item(String data) {
            this.data = data;
        }

        @Override
        public FastLinkedListItem getPrevious() {
            return prev;
        }

        @Override
        public void setPrevious(FastLinkedListItem previous) {
            this.prev = previous;
        }

        @Override
        public FastLinkedListItem getNext() {
            return next;
        }

        @Override
        public void setNext(FastLinkedListItem next) {
            this.next = next;
        }

        @Override
        public boolean equals(Object obj) {
            return (obj instanceof Item) && ((Item) obj).data.equals(this.data);
        }

        @Override
        public String toString() {
            return data;
        }
    }

}