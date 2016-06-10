package yurgis.rxjava.recipes.guava;

import java.util.Iterator;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Func1;
import rx.internal.producers.SingleDelayedProducer;
import rx.observables.BlockingObservable;

import com.google.common.base.Function;
import com.google.common.base.Supplier;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;

/**
 * Utility class to help creating {@link Observable}s from {@link ListenableFuture}s
 * @author yurgis
 *
 */
public class ListenableFutureToObservable {
  /**
   * Converts {@link ListenableFuture} iterator into an rx {@link Observable}.
   * <p>
   * Note that by calling this method actual source iterator will not be traversed automatically. 
   * <p>
   * The client on observer side can request only a limited number of items (e.g. using {@link Observable#take(int)}
   * or {@link BlockingObservable#getIterator()} (since rxjava 1.0.15)
   * 
   * @param futureIterator
   *          a {@link ListenableFuture} with {@link Iterator}
   * 
   * @param <T> target object type
   * 
   * @return instance of {@link Observable} that represents a stream of items from the source future iterator
   */
  public static <T> Observable<T> toObservable(final ListenableFuture<Iterator<T>> futureIterator) {
    return toObservable(futureIterator, MoreExecutors.sameThreadExecutor());
  }

  /**
   * Converts a lazily loaded {@link ListenableFuture} iterator into an rx {@link Observable}.
   * <p>
   * Note that by calling this method actual source iterator will not be traverse automatically. 
   * <p>
   * The client on observer side can request only a limited number of items (e.g. using {@link Observable#take(int)}
   * 
   * @param futureIteratorSupplier
   *          a lazily loaded future iterator
   * 
   * @param <T> target object type
   * 
   * @return instance of {@link Observable} that represents a stream of items from the source future iterator
   */
  public static <T> Observable<T> toObservable(final Supplier<ListenableFuture<Iterator<T>>> futureIteratorSupplier) {
    return toObservable(futureIteratorSupplier, MoreExecutors.sameThreadExecutor());
  }
  
  /**
   * Converts {@link ListenableFuture} iterator into an rx {@link Observable}.
   * <p>
   * Note that by calling this method actual source iterator will not be traversed automatically. 
   * <p>
   * The client on observer side can request only a limited number of items (e.g. using {@link Observable#take(int)}
   * or {@link BlockingObservable#getIterator()} (since rxjava 1.0.15)
   * 
   * @param futureIterator
   *          a future iterator
   * @param executor The executor to use for transformation
   * 
   * @param <T> target object type
   * 
   * @return instance of {@link Observable} that represents a stream of items from the source future iterator
   */
  public static <T> Observable<T> toObservable(final ListenableFuture<Iterator<T>> futureIterator,
      final Executor executor) {
    //With java8 lamdas the code below would look like:
    //
    //  return observeFutureIterator(statement, rowMapper)
    //      .concatMap(iter -> Observable.from(() -> iter))
    //
    //but for now we stuck with jdk7 support because of guice and storm issues
    return toObservableIterator(futureIterator, executor)
        .concatMap(new Func1<Iterator<T>, Observable<T>>() {

      @Override
      public Observable<T> call(final Iterator<T> iter) {
        return Observable.from(new Iterable<T>() {

          @Override
          public Iterator<T> iterator() {
            return iter;
          }
          
        });
      }
      
    });
  }

  /**
   * Converts a lazily loaded {@link ListenableFuture} iterator into an rx {@link Observable}.
   * <p>
   * Note that by calling this method actual source iterator will not be traversed automatically. 
   * <p>
   * The client on observer side can request only a limited number of items (e.g. using {@link Observable#take(int)}
   * or {@link BlockingObservable#getIterator()} (since rxjava 1.0.15)
   * 
   * @param futureIteratorSupplier
   *          a lazily loaded future iterator
   * @param executor The executor to use for transformation
   * 
   * @param <T> target object type
   * 
   * @return instance of {@link Observable} that represents a stream of items from the source future iterator
   */
  public static <T> Observable<T> toObservable(final Supplier<ListenableFuture<Iterator<T>>> futureIteratorSupplier,
      final Executor executor) {
    //With java8 lamdas the code below would look like:
    //
    //  return observeFutureIterator(statement, rowMapper)
    //      .concatMap(iter -> Observable.from(() -> iter))
    //
    //but for now we stuck with jdk7 support because of guice and storm issues
    return toObservableIterator(futureIteratorSupplier, executor)
        .concatMap(new Func1<Iterator<T>, Observable<T>>() {

      @Override
      public Observable<T> call(final Iterator<T> iter) {
        return Observable.from(new Iterable<T>() {

          @Override
          public Iterator<T> iterator() {
            return iter;
          }
          
        });
      }
      
    });
  }
  
  /**
   * Converts {@link ListenableFuture} {@link Iterator} into observable {@link Iterator} 
   * <p>
   * Note, that to be truly non-blocking it creates a custom observable instead of calling naive 
   * {@link Observable#from(java.util.concurrent.Future, rx.Scheduler)} which is calling blocking {@link Future#get()}.
   * <p>
   * Also it never triggers triggers unnecessary iterations thank to reactive pull back-pressure implemented by
   * {@link Observable#from(Iterable)}. 
   * This along with Guava's iterator on-the-fly transformations allows iterating over physical result set on demand 
   * using observer's pace. A client observer may unsubscribe at any time without actually requesting all the items,
   * and this will not cause the remaining result set to be fetched.
   * 
   * @param futureIterator a future iterator to convert 
   * 
   * @param <T> target object type
   * 
   * @return instance of {@link Observable} iterator
   */
  public static <T> Observable<Iterator<T>> toObservableIterator(final ListenableFuture<Iterator<T>> futureIterator) {
    return toOneObservable(futureIterator);
  }

  /**
   * Converts a lazily evaluated {@link ListenableFuture} {@link Iterator} into observable {@link Iterator} 
   * <p>
   * Note, that to be truly non-blocking it creates a custom observable instead of calling naive 
   * {@link Observable#from(java.util.concurrent.Future, rx.Scheduler)} which is calling blocking {@link Future#get()}.
   * <p>
   * Also it never triggers triggers unnecessary iterations thank to reactive pull back-pressure implemented by
   * {@link Observable#from(Iterable)}. 
   * This along with Guava's iterator on-the-fly transformations allows iterating over physical result set on demand 
   * using observer's pace. A client observer may unsubscribe at any time without actually requesting all the items,
   * and this will not cause the remaining result set to be fetched.
   * 
   * @param futureIteratorSupplier a lazily evaluated future iterator to convert 
   * 
   * @param <T> target object type
   * 
   * @return instance of {@link Observable} iterator
   */
  public static <T> Observable<Iterator<T>> toObservableIterator(final Supplier<ListenableFuture<Iterator<T>>> futureIteratorSupplier) {
    return toOneObservable(futureIteratorSupplier);
  }
  
  /**
   * Converts {@link ListenableFuture} {@link Iterator} into observable {@link Iterator} 
   * <p>
   * Note, that to be truly non-blocking it creates a custom observable instead of calling naive 
   * {@link Observable#from(java.util.concurrent.Future, rx.Scheduler)} which is calling blocking {@link Future#get()}.
   * <p>
   * Also it never triggers unnecessary iterations thank to reactive pull back-pressure implemented by
   * {@link Observable#from(Iterable)}. 
   * This along with Guava's iterator on-the-fly transformations allows iterating over physical result set on demand 
   * using observer's pace. A client observer may unsubscribe at any time without actually requesting all the items,
   * and this will not cause the remaining iterator to be fetched.
   * 
   * @param futureIterator a future iterator to convert 
   * @param executor The executor to use for transformation
   * 
   * @param <T> target object type
   * 
   * @return instance of {@link Observable} iterator
   */
  public static <T> Observable<Iterator<T>> toObservableIterator(final ListenableFuture<Iterator<T>> futureIterator, 
      final Executor executor) {
    return toOneObservable(futureIterator, executor);
  }

  /**
   * Converts a lazily evaluated {@link ListenableFuture} {@link Iterator} into observable {@link Iterator} 
   * <p>
   * Note, that to be truly non-blocking it creates a custom observable instead of calling naive 
   * {@link Observable#from(java.util.concurrent.Future, rx.Scheduler)} which is calling blocking {@link Future#get()}.
   * <p>
   * Also it never triggers unnecessary iterations thank to reactive pull back-pressure implemented by
   * {@link Observable#from(Iterable)}. 
   * This along with Guava's iterator on-the-fly transformations allows iterating over physical result set on demand 
   * using observer's pace. A client observer may unsubscribe at any time without actually requesting all the items,
   * and this will not cause the remaining iterator to be fetched.
   * 
   * @param futureIteratorSupplier a lazily evaluated future iterator to convert 
   * @param executor The executor to use for transformation
   * 
   * @param <T> target object type
   * 
   * @return instance of {@link Observable} iterator
   */
  public static <T> Observable<Iterator<T>> toObservableIterator(final Supplier<ListenableFuture<Iterator<T>>> futureIteratorSupplier, 
      final Executor executor) {
    return toOneObservable(futureIteratorSupplier, executor);
  }
  
  /**
   * Converts {@link ListenableFuture} into single item {@link Observable} 
   * <p>
   * Note, that to be truly non-blocking it creates a custom observable instead of calling naive 
   * {@link Observable#from(java.util.concurrent.Future, rx.Scheduler)} which is calling blocking {@link Future#get()}.
   * <p>
   * Also it never triggers unnecessary source iteration automatically thank to reactive pull back-pressure implemented by
   * {@link Observable#from(Iterable)}. 
   * This along with Guava's iterator on-the-fly transformations allows iterating over physical result set on demand 
   * using observer's pace. A client observer may unsubscribe at any time without actually requesting all the items,
   * and this will not cause the remaining iterator to be fetched.
   * 
   * @param future a future to convert 
   * 
   * @param <T> target object type
   * 
   * @return instance of {@link Observable} iterator
   * 
   */
  public static <T> Observable<T> toOneObservable(final ListenableFuture<T> future) {
    return toOneObservable(future, MoreExecutors.sameThreadExecutor());
  }

  /**
   * Converts a lazy evaluated {@link ListenableFuture} into single item {@link Observable} 
   * <p>
   * Note, that to be truly non-blocking it creates a custom observable instead of calling naive 
   * {@link Observable#from(java.util.concurrent.Future, rx.Scheduler)} which is calling blocking {@link Future#get()}.
   * <p>
   * Also it never triggers unnecessary source iteration automatically thank to reactive pull back-pressure implemented by
   * {@link Observable#from(Iterable)}. 
   * This along with Guava's iterator on-the-fly transformations allows iterating over physical result set on demand 
   * using observer's pace. A client observer may unsubscribe at any time without actually requesting all the items,
   * and this will not cause the remaining iterator to be fetched.
   * 
   * @param futureSupplier a lazily evaluated future to convert 
   * 
   * @param <T> target object type
   * 
   * @return instance of {@link Observable} iterator
   * 
   */
  public static <T> Observable<T> toOneObservable(final Supplier<ListenableFuture<T>> futureSupplier) {
    return toOneObservable(futureSupplier, MoreExecutors.sameThreadExecutor());
  }
  
  /**
   * Converts {@link ListenableFuture} into single item {@link Observable} 
   * <p>
   * Note, that to be truly non-blocking it creates a custom observable instead of calling naive 
   * {@link Observable#from(java.util.concurrent.Future, rx.Scheduler)} which is calling blocking {@link Future#get()}.
   * <p>
   * Also it never triggers iteration thank to reactive pull back-pressure implemented by
   * {@link Observable#from(Iterable)}. 
   * This along with Guava's iterator on-the-fly transformations allows iterating over physical result set on demand 
   * using observer's pace. A client observer may unsubscribe at any time without actually requesting all the items,
   * and this will not cause the remaining iterator to be fetched.
   * 
   * @param future a future to convert 
   * @param executor The executor to use for transformation
   * 
   * @param <T> target object type
   * 
   * @return instance of {@link Observable}
   */
  public static <T> Observable<T> toOneObservable(final ListenableFuture<T> future, final Executor executor) {
    return Observable.create(new Observable.OnSubscribe<T>() {

      @Override
      public void call(final Subscriber<? super T> subscriber) {
        final SingleDelayedProducer<T> producer = new SingleDelayedProducer<T>(subscriber);
        subscriber.setProducer(producer);

        Futures.addCallback(future, new FutureCallback<T>() {

          @Override
          public void onSuccess(T t) {
            producer.setValue(t);
          }

          @Override
          public void onFailure(Throwable t) {
            subscriber.onError(t);
          }
        }, executor);
      }
    });
    
  }

  /**
   * Converts lazily evaluated {@link ListenableFuture} into single item {@link Observable} 
   * <p>
   * Note, that to be truly non-blocking it creates a custom observable instead of calling naive 
   * {@link Observable#from(java.util.concurrent.Future, rx.Scheduler)} which is calling blocking {@link Future#get()}.
   * <p>
   * Also it never triggers iteration thank to reactive pull back-pressure implemented by
   * {@link Observable#from(Iterable)}. 
   * This along with Guava's iterator on-the-fly transformations allows iterating over physical result set on demand 
   * using observer's pace. A client observer may unsubscribe at any time without actually requesting all the items,
   * and this will not cause the remaining iterator to be fetched.
   * 
   * @param futureSupplier a lazy evaluated future to convert 
   * @param executor The executor to use for transformation
   * 
   * @param <T> target object type
   * 
   * @return instance of {@link Observable}
   */
  public static <T> Observable<T> toOneObservable(final Supplier<ListenableFuture<T>> futureSupplier, final Executor executor) {
    return Observable.create(new Observable.OnSubscribe<T>() {

      @Override
      public void call(final Subscriber<? super T> subscriber) {
        final SingleDelayedProducer<T> producer = new SingleDelayedProducer<T>(subscriber);
        subscriber.setProducer(producer);

        Futures.addCallback(futureSupplier.get(), new FutureCallback<T>() {

          @Override
          public void onSuccess(T t) {
            producer.setValue(t);
          }

          @Override
          public void onFailure(Throwable t) {
            subscriber.onError(t);
          }
        }, executor);
      }
    });
    
  }
  
  /**
   * Converts {@link ListenableFuture} {@link Iterable} to future {@link Iterator}
   * @param iterable source {@link Iterable} 
   * @param <T> item type
   * @return future {@link Iterator}
   */
  public static <T> ListenableFuture<Iterator<T>> toFutureIterator(ListenableFuture<Iterable<T>> iterable) {
    return Futures.transform(iterable, new Function<Iterable<T>, Iterator<T>>() {

      @Override
      public Iterator<T> apply(Iterable<T> rs) {
        return rs.iterator();
      }
      
    });
  }
  
  
}
