package com.funcache.internal;

import junit.framework.TestCase;

/**
 * TODO: Class description here.
 *
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
public class DataWrapperImplTest extends TestCase {

    public void testIsSynced() throws Exception {
        DataWrapperImpl<String, String> dw1 = new DataWrapperImpl<>("a", "b", true);
        DataWrapperImpl<String, String> dw2 = new DataWrapperImpl<>("a", "b", false);

        assertTrue(dw1.isSynced());
        assertFalse(dw2.isSynced());
    }

    public void testSetSynced() throws Exception {
        DataWrapperImpl<String, String> dw1 = new DataWrapperImpl<>("a", "b", true);
        DataWrapperImpl<String, String> dw2 = new DataWrapperImpl<>("a", "b", false);

        dw1.setSynced(false);
        dw2.setSynced(true);

        assertFalse(dw1.isSynced());
        assertTrue(dw2.isSynced());
    }

    public void testCompareAndSetSyncState() throws Exception {
        DataWrapperImpl<String, String> dw1 = new DataWrapperImpl<>("a", "b", true);
        DataWrapperImpl<String, String> dw2 = new DataWrapperImpl<>("a", "b", false);
        DataWrapperImpl<String, String> dw3 = new DataWrapperImpl<>("a", "b", false);

        dw1.compareAndSetSyncState(DataWrapperImpl.STATE_UNSYNCED, DataWrapperImpl.STATE_SYNCING);
        dw2.compareAndSetSyncState(DataWrapperImpl.STATE_UNSYNCED, DataWrapperImpl.STATE_SYNCING);
        dw3.compareAndSetSyncState(DataWrapperImpl.STATE_UNSYNCED, DataWrapperImpl.STATE_SYNCING);

        assertTrue(dw1.isSynced());
        assertFalse(dw2.isSynced());
        assertFalse(dw3.isSynced());

        dw1.compareAndSetSyncState(DataWrapperImpl.STATE_SYNCING, DataWrapperImpl.STATE_SYNCED);
        dw2.compareAndSetSyncState(DataWrapperImpl.STATE_SYNCING, DataWrapperImpl.STATE_SYNCED);
        dw3.setSynced(false);
        dw3.compareAndSetSyncState(DataWrapperImpl.STATE_SYNCING, DataWrapperImpl.STATE_SYNCED);

        assertTrue(dw1.isSynced());
        assertTrue(dw2.isSynced());
        assertFalse(dw3.isSynced());
    }

}