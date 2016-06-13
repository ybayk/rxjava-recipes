package ybayk.rxjava.recipes.guava;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Assert;
import org.junit.Test;

import rx.Observable;
import rx.subjects.PublishSubject;

import com.google.common.collect.Iterators;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.SettableFuture;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

public class ListenableFutureToObservableTest {
  
  final static ThreadFactory namedThreadFactory = new ThreadFactoryBuilder().setNameFormat("test-fixed-%d").build();
  final static Executor executor = Executors.newFixedThreadPool(1, namedThreadFactory);

  @Test
  public void testFromIterator() throws InterruptedException {
    Iterator<Integer> iter = Arrays.asList(1,2,3).iterator();
    SettableFuture<Iterator<Integer>> future = SettableFuture.create();
    Executors.newSingleThreadScheduledExecutor().schedule(()->future.set(iter), 100, TimeUnit.MILLISECONDS);
    CountDownLatch latch = new CountDownLatch(3);
    long start = System.currentTimeMillis();
    AtomicReference<Throwable> error = new AtomicReference<>();
    Observable<Integer> o = ListenableFutureToObservable.fromIterator(future);
    o.subscribe(item->latch.countDown(),e->error.set(e));
    Assert.assertNull(error.get());
    Assert.assertTrue("should not block", System.currentTimeMillis() - start < 90);
    latch.await(150, TimeUnit.MILLISECONDS);
  }

  @Test
  public void testFromIteratorWithExecutor() throws InterruptedException {
    Iterator<Integer> iter = Arrays.asList(1,2,3).iterator();
    SettableFuture<Iterator<Integer>> future = SettableFuture.create();
    Executors.newSingleThreadScheduledExecutor().schedule(()->future.set(iter), 100, TimeUnit.MILLISECONDS);
    CountDownLatch latch = new CountDownLatch(3);
    AtomicReference<String> threadName = new AtomicReference<>();
    Observable<Integer> o = ListenableFutureToObservable.fromIterator(future, executor);
    o.doOnNext(item->threadName.set(Thread.currentThread().getName()))
      .subscribe(item->latch.countDown());
    latch.await();
    Assert.assertNotNull(threadName.get());
    Assert.assertEquals("test-fixed-0", threadName.get());
  }
  
  @Test
  public void testFromLazyIterator() throws InterruptedException {
    PublishSubject<Integer> o1 = PublishSubject.create();
    AtomicBoolean fetched = new AtomicBoolean();
    Iterator<Integer> iter = Iterators.filter(Arrays.asList(1,2,3).iterator(), item->{fetched.set(true); return true;});
    CountDownLatch latch = new CountDownLatch(3);
    AtomicReference<Throwable> error = new AtomicReference<>();
    Observable<Integer> o2 = ListenableFutureToObservable.fromIterator(()->Futures.immediateFuture(iter), executor);
    Observable.concat(o1, o2).subscribe(item->latch.countDown(),e->error.set(e));
    Thread.sleep(100);
    Assert.assertFalse("lazy future should not have been fetched yet", fetched.get());
    o1.onCompleted(); //should trigger lazy future to compute
    Thread.sleep(50);
    Assert.assertTrue("lazy future should have been fetched by now", fetched.get());
    Assert.assertNull(error.get());
  }

  @Test
  public void testFromIterablre() throws InterruptedException {
    List<Integer> list = Arrays.asList(1,2,3);
    SettableFuture<List<Integer>> future = SettableFuture.create();
    Executors.newSingleThreadScheduledExecutor().schedule(()->future.set(list), 100, TimeUnit.MILLISECONDS);
    CountDownLatch latch = new CountDownLatch(3);
    long start = System.currentTimeMillis();
    AtomicReference<Throwable> error = new AtomicReference<>();
    Observable<Integer> o = ListenableFutureToObservable.fromIterable(future);
    o.subscribe(item->latch.countDown(),e->error.set(e));
    Assert.assertNull(error.get());
    Assert.assertTrue("should not block", System.currentTimeMillis() - start < 90);
    latch.await(150, TimeUnit.MILLISECONDS);
  }

  @Test
  public void testFromIterableWithExecutor() throws InterruptedException {
    List<Integer> list = Arrays.asList(1,2,3);
    SettableFuture<List<Integer>> future = SettableFuture.create();
    Executors.newSingleThreadScheduledExecutor().schedule(()->future.set(list), 100, TimeUnit.MILLISECONDS);
    CountDownLatch latch = new CountDownLatch(3);
    AtomicReference<String> threadName = new AtomicReference<>();
    Observable<Integer> o = ListenableFutureToObservable.fromIterable(future, executor);
    o.doOnNext(item->threadName.set(Thread.currentThread().getName()))
      .subscribe(item->latch.countDown());
    latch.await();
    Assert.assertNotNull(threadName.get());
    Assert.assertEquals("test-fixed-0", threadName.get());
  }
  
  @Test
  public void testFromLazyIterable() throws InterruptedException {
    PublishSubject<Integer> o1 = PublishSubject.create();
    AtomicBoolean fetched = new AtomicBoolean();
    List<Integer> list = Arrays.asList(1,2,3);
    Iterable<Integer> iterable = new Iterable<Integer>() {

      @Override
      public Iterator<Integer> iterator() {
        fetched.set(true);
        return list.iterator();
      }
      
    };
    CountDownLatch latch = new CountDownLatch(3);
    AtomicReference<Throwable> error = new AtomicReference<>();
    Observable<Integer> o2 = ListenableFutureToObservable.fromIterable(()->Futures.immediateFuture(iterable), executor);
    Observable.concat(o1, o2).subscribe(item->latch.countDown(),e->error.set(e));
    Thread.sleep(100);
    Assert.assertFalse("lazy future should not have been fetched yet", fetched.get());
    o1.onCompleted(); //should trigger lazy future to compute
    Thread.sleep(50);
    Assert.assertTrue("lazy future should have been fetched by now", fetched.get());
    Assert.assertNull(error.get());
  }
  

  @Test
  public void testFromScalar() throws InterruptedException {
    String value = "test";
    SettableFuture<String> future = SettableFuture.create();
    Executors.newSingleThreadScheduledExecutor().schedule(()->future.set(value), 100, TimeUnit.MILLISECONDS);
    CountDownLatch latch = new CountDownLatch(1);
    long start = System.currentTimeMillis();
    AtomicReference<Throwable> error = new AtomicReference<>();
    Observable<String> o = ListenableFutureToObservable.fromScalar(future);
    o.subscribe(item->latch.countDown(),e->error.set(e));
    Assert.assertNull(error.get());
    Assert.assertTrue("should not block", System.currentTimeMillis() - start < 90);
    latch.await(150, TimeUnit.MILLISECONDS);
  }

  @Test
  public void testFromScalarWithExecutor() throws InterruptedException {
    String value = "test";
    SettableFuture<String> future = SettableFuture.create();
    Executors.newSingleThreadScheduledExecutor().schedule(()->future.set(value), 100, TimeUnit.MILLISECONDS);
    CountDownLatch latch = new CountDownLatch(1);
    AtomicReference<String> threadName = new AtomicReference<>();
    Observable<String> o = ListenableFutureToObservable.fromScalar(future, executor);
    o.doOnNext(item->threadName.set(Thread.currentThread().getName()))
      .subscribe(item->latch.countDown());
    latch.await();
    Assert.assertNotNull(threadName.get());
    Assert.assertEquals("test-fixed-0", threadName.get());
  }
  
  @Test
  public void testFromLazyScalar() throws Exception {
    PublishSubject<Integer> o1 = PublishSubject.create();
    AtomicBoolean fetched = new AtomicBoolean();
    CountDownLatch latch = new CountDownLatch(3);
    AtomicReference<Throwable> error = new AtomicReference<>();
    Observable<String> o2 = ListenableFutureToObservable
        .fromScalar(()->{fetched.set(true); return Futures.immediateFuture("test");}, executor);
    Observable.concat(o1, o2).subscribe(item->latch.countDown(),e->error.set(e));
    Thread.sleep(100);
    Assert.assertFalse("lazy future should not have been fetched yet", fetched.get());
    o1.onCompleted(); //should trigger lazy future to compute
    Thread.sleep(50);
    Assert.assertTrue("lazy future should have been fetched by now", fetched.get());
    Assert.assertNull(error.get());
  }
  
}
