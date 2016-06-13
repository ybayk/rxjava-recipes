package ybayk.rxjava.recipes.guava;

import java.util.Arrays;
import java.util.Iterator;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Assert;
import org.junit.Test;

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
    ListenableFutureToObservable.fromIterator(future)
      .subscribe(item->latch.countDown(),e->error.set(e));
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
    ListenableFutureToObservable.fromIterator(future, executor)
      .doOnNext(item->threadName.set(Thread.currentThread().getName()))
      .subscribe(item->latch.countDown());
    latch.await();
    Assert.assertNotNull(threadName.get());
    Assert.assertEquals("test-fixed-0", threadName.get());
  }
  
}
