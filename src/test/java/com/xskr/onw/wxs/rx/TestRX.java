package com.xskr.onw.wxs.rx;

import io.reactivex.*;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.Subject;
import org.junit.Test;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class TestRX {
    @Test
    public void testRX01(){
        Observable.fromArray(new String[]{"a", "b"}).subscribe( e -> {
            System.out.println(e);
        });
    }

    @Test
    public void testRX02(){
        Flowable.just("Hello world").subscribe(System.out::println);
    }

    @Test
    public void testRX03(){
        Flowable.just("Hello world").subscribe(new Consumer<String>(){
            @Override
            public void accept(String s) throws Exception {
                System.out.println(s);
            }
        });
    }

    @Test
    public void testRX04(){
        Observable.create(emitter -> {
            while (!emitter.isDisposed()) {
                long time = System.currentTimeMillis();
                emitter.onNext(time);
                if (time % 2 != 0) {
                    emitter.onError(new IllegalStateException("Odd millisecond!"));
                    break;
                }
            }
        }).subscribe(System.out::println, Throwable::printStackTrace);
    }

    @Test
    public void testRX05() throws InterruptedException {
        Flowable.fromCallable(() -> {
            Thread.sleep(1000); //  imitate expensive computation
            return "Done";
        })
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.single())
                .subscribe(System.out::println, Throwable::printStackTrace);

        Thread.sleep(2000); // <--- wait for the flow to finish
    }

    @Test
    public void concurrency(){
        Flowable.range(1, 10)
                .observeOn(Schedulers.computation())
                .map(v -> v*v)
                .blockingSubscribe(System.out::println);
    }

    @Test
    public void parallel(){
        Flowable.range(1, 10)
                .flatMap( v -> Flowable.just(v)
                .subscribeOn(Schedulers.computation())
                .map(w -> w * w)
        ).blockingSubscribe(System.out::println);
    }
    @Test
    public void parallel2(){
        Flowable.range(1, 10)
                .parallel()
//                .runOn(Schedulers.computation())
                .map(v -> v * v)
                .sequential()
                .blockingSubscribe(System.out::println);
    }
    @Test
    public void subflows(){
//        Flowable<Inventory> inventorySource = warehouse.getInventoryAsync();
//
//        inventorySource.flatMap(inventoryItem ->
//                erp.getDemandAsync(inventoryItem.getId())
//                        .map(demand
//                                -> System.out.println("Item " + inventoryItem.getName() + " has demand " + demand));
//  )
//  .subscribe();
    }
    @Test
    public void deferredDependent(){
        AtomicInteger count = new AtomicInteger();

        Observable.range(1, 10)
                .doOnNext(ignored -> count.incrementAndGet())
                .ignoreElements()
                .andThen(Single.defer(() -> Single.just(count.get())))
                .subscribe(System.out::println);
    }

    @Test
    public void fireByCondition(){
//        Single.fromCallable(() -> {System.out.println(Thread.currentThread().getName()); return 100;}).subscribe(System.out::println);
//        Single.fromCallable(() -> {System.out.println(Thread.currentThread().getName()); return 200;}).subscribe(System.out::println);
//        Observable.just(1,2,3).map( e -> e * e).subscribe(System.out::println);
        Observable<Integer> observable = new Observable<Integer>() {
            @Override
            protected void subscribeActual(Observer<? super Integer> observer) {
                for(int i=0;i<3;i++){
                    observer.onNext(i);
                }
                observer.onComplete();
            }
        };
        observable.subscribe(System.out::println);
    }
}
