package com.funcache.internal;

import com.funcache.Configuration;
import com.funcache.FunCacheBuilder;
import com.funcache.FunCacheOptions;
import com.funcache.impl.StorageFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

/**
 * TODO: Class description here.
 *
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
public class ConcurrencyTest {

    public static void main(String[] args) throws InterruptedException, ExecutionException {
//        Observable.interval(1, TimeUnit.SECONDS).subscribeOn(Schedulers.newThread()).subscribe(new Action1<Long>() {
//            @Override
//            public void call(Long aLong) {
//                System.out.println("Hello Rx, we are at thread: " + Thread.currentThread().getId());
//            }
//        });
//        System.out.println("Main thread: " + Thread.currentThread().getId());
//        Thread.sleep(2000);

//        final CacheStorage<String, String> storage = new HashMapCacheStorage<String, String>();
//        int size = Executors.newSingleThreadExecutor().submit(new Callable<Integer>() {
//            @Override
//            public Integer call() throws Exception {
//                for (int i = 0; i < 10; i++) {
//                    storage.put(String.valueOf(i), UUID.randomUUID().toString());
//                }
//                storage.remove("4");
//                storage.remove("7");
//                storage.remove("8");
//                return storage.size();
//            }
//        }).get();
//
//        System.out.println(size + ", " + storage.size());

        Configuration config = new FunCacheOptions();
        config.setCancelSyncIfNotLargerMin(true);
        config.setMaxItems(50000000);
        config.setMaxUnsyncedItems(10000);
        config.setMinEvictableIdleTimeMillis(5000);
        config.setMinItemsToSync(500);
        config.setOverrideUnsyncedItems(true);
        config.setSyncInterval(60);
        config.setTimeBetweenEvictionRunsMillis(15000);
        config.setStorageFactory(new StorageFactory());

        final FunCacheImpl<String, String> funCache = (FunCacheImpl<String, String>)
                new FunCacheBuilder<String, String>()
                        .setConfiguration(config)
                        .build();

        for (int i = 0; i < 20; i++) {
            funCache.put(String.valueOf(i), UUID.randomUUID().toString());
        }

        final List<DataWrapperImpl<String, String>> forSyncs = new ArrayList<>();
        DataWrapperImpl<String, String> dw = funCache.getMostRecentItem();
        while (dw != null) {
            if (dw.compareAndSetSyncState(DataWrapperImpl.STATE_UNSYNCED,
                    DataWrapperImpl.STATE_SYNCING)) {
                forSyncs.add(dw);
                dw = dw.getPrevious();
                continue;
            }
            break;
        }

        for (int i = 19; i >= 15; i--) {
            funCache.putUnsafe(String.valueOf(i), UUID.randomUUID().toString());
        }

        for (DataWrapperImpl<String, String> dwi : forSyncs) {
            if (dwi.compareAndSetSyncState(DataWrapperImpl.STATE_SYNCING,
                    DataWrapperImpl.STATE_SYNCED)) {
                funCache.numUnsyncedItems.decrementAndGet();
            }
        }

        System.out.println("[SYNC] Synced: " + forSyncs.size() + ", current unsynced: "
                + funCache.getNumberUnsyncedItems());

        dw = funCache.getMostIdleItem();
        final List<DataWrapperImpl<String, String>> tmp = new ArrayList<>();
        while (dw != null) {
            tmp.add(dw);
            dw = dw.getNext();
        }

        funCache.shutdown();
    }
}
