package com.funcache;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * TODO: Class description here.
 *
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
public class PerformanceTest {

    public static void main(String[] args) {
//        Configuration config = new FunCacheOptions();
//        config.setCancelSyncIfNotLargerMin(true);
//        config.setMaxItems(50000000);
//        config.setMaxUnsyncedItems(1000);
//        config.setMinEvictableIdleTimeMillis(5000);
//        config.setMinItemsToSync(500);
//        config.setOverrideUnsyncedItems(true);
//        config.setSyncInterval(30);
//        config.setTimeBetweenEvictionRunsMillis(15000);
//        config.setStorageFactory(new StorageFactory());
//
//        final FunCacheImpl<String, String> funCache = (FunCacheImpl<String, String>) new FunCacheBuilder<String, String>()
//                .setConfiguration(config)
//                .build();
//
//        Executors.newFixedThreadPool(50).submit(new Runnable() {
//            @Override
//            public void run() {
//                while (true) {
//                    funCache.put(UUID.randomUUID().toString(), UUID.randomUUID().toString());
////                    System.out.println("Size: " + funCache.size()
////                            + ", unsynced size: " + funCache.getNumberUnsyncedItems());
//                }
//            }
//        });

        AtomicInteger integer = new AtomicInteger(9);
        integer.compareAndSet(10, 12);
        System.out.println(integer.get());
    }
}
