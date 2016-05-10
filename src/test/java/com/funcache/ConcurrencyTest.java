package com.funcache;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * TODO: Class description here.
 *
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
public class ConcurrencyTest {

    public static void main(String[] args) throws InterruptedException {
//        Observable.interval(1, TimeUnit.SECONDS).subscribeOn(Schedulers.newThread()).subscribe(new Action1<Long>() {
//            @Override
//            public void call(Long aLong) {
//                System.out.println("Hello Rx, we are at thread: " + Thread.currentThread().getId());
//            }
//        });
//        System.out.println("Main thread: " + Thread.currentThread().getId());
//        Thread.sleep(2000);

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<?> fut1 = executor.submit(new Runnable() {
            @Override
            public void run() {
                System.out.println("Task 1 running ...");
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        Future<?> fut2 = executor.submit(new Runnable() {
            @Override
            public void run() {
                System.out.println("Task 2 running ...");
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        Future<?> fut3 = executor.submit(new Runnable() {
            @Override
            public void run() {
                System.out.println("Task 3 running ...");
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        try {
            fut1.get();
            fut2.get();
            fut3.get();
            Thread.sleep(2000);
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        executor.submit(new Runnable() {
            @Override
            public void run() {
                System.out.println("Task 4 running ...");
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        executor.shutdown();
    }
}
