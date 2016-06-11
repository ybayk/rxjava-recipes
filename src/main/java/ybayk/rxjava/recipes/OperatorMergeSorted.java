package ybayk.rxjava.recipes;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicLongFieldUpdater;

import rx.Observable;
import rx.Observable.Operator;
import rx.Observer;
import rx.Producer;
import rx.Subscriber;
import rx.exceptions.Exceptions;
import rx.exceptions.MissingBackpressureException;
import rx.exceptions.OnErrorThrowable;
import rx.internal.operators.BackpressureUtils;
import rx.internal.util.RxRingBuffer;
import rx.subscriptions.CompositeSubscription;

/**
 * Merges source {@link Observable}s into one {@code Observable}, without any
 * transformation, in the order specified by the {@code comparator} (assuming
 * the source {@link Observable}s have their items pre-sorted in the order
 * consistent with the {@code comparator}).
 * <p>
 * Items emitted by each of the source {@link Observable}s are expected to be
 * pre-ordered according to the specified {@code comparator}, otherwise the
 * order of the resulting {@code Observable}'s items is undefined.
 * <p>
 * The resulting observable will start emitting items only after obtaining at
 * least one item from <b>each</b> of the source {@link Observable}s being
 * merged.
 * <p>
 * The operator has as a space cost of {@code O(M)}, where M is a number of the
 * source {@link Observable}s to merge, and it may hold up to
 * {@code M * RxRingBuffer.SIZE} items at a time in memory.
 * <p>
 * This operator supports backpressure and is loosely based on
 * {@link rx.internal.operators.OperatorZip}.
 * 
 * @param <T>
 *          the type of the items emitted by both the source and merged
 *          {@code Observable}s
 */
public final class OperatorMergeSorted<T> implements Operator<T, Observable<? extends T>> {

  private final Comparator<T> comparator;

  /**
   * Constructs an operator that emits its elements according to their
   * {@linkplain Comparable} natural ordering.
   */
  public OperatorMergeSorted() {
    this.comparator = null;
  }

  /**
   * Constructs an operator using the specified {@link Comparator}.
   * 
   * @param comparator
   *          a comparator to use for a sorted merge.
   */
  public OperatorMergeSorted(Comparator<T> comparator) {
    this.comparator = comparator;
  }

  @Override
  public Subscriber<? super Observable<? extends T>> call(final Subscriber<? super T> child) {
    final SortedMerge<T> merger = new SortedMerge<T>(comparator, child);
    final MergeSortedProducer<T> producer = new MergeSortedProducer<T>(merger);
    child.setProducer(producer);
    return new MergeSortedSubscriber(child, merger, producer);
  }

  private final class MergeSortedSubscriber extends Subscriber<Observable<? extends T>> {

    final Subscriber<? super T> child;
    final SortedMerge<T> merge;
    final MergeSortedProducer<T> producer;
    final List<Observable<? extends T>> sources = new ArrayList<Observable<? extends T>>();

    public MergeSortedSubscriber(Subscriber<? super T> child, SortedMerge<T> merge, MergeSortedProducer<T> producer) {
      this.child = child;
      this.merge = merge;
      this.producer = producer;
    }

    @Override
    public void onCompleted() {
      if (sources.isEmpty()) {
        // this means we have not received a valid onNext before termination so
        // we emit the onCompleted
        child.onCompleted();
      } else {
        merge.start(sources, producer);
      }
    }

    @Override
    public void onError(Throwable e) {
      child.onError(e);
    }

    @Override
    public void onNext(Observable<? extends T> observable) {
      sources.add(observable);
    }

  }

  private static final class MergeSortedProducer<T> extends AtomicLong implements Producer {
    /** */
    private static final long serialVersionUID = -1216676403723546796L;
    private SortedMerge<T> merge;

    public MergeSortedProducer(SortedMerge<T> merge) {
      this.merge = merge;
    }

    @Override
    public void request(long n) {
      BackpressureUtils.getAndAddRequest(this, n);
      // try and claim emission if no other threads are doing so
      merge.tick();
    }

  }

  private static final class SortedMerge<T> extends AtomicLong {
    /** */
    private static final long serialVersionUID = 1237247396158074733L;
    
    private final Comparator<T> comparator;
    private final Observer<? super T> child;
    private final CompositeSubscription childSubscription = new CompositeSubscription();

    static final int THRESHOLD = (int) (RxRingBuffer.SIZE * 0.7);

    /* initialized when started in `start` */
    private AtomicLong requested;
    private PriorityQueue<ValueSourcePair> queue; // not synchronized as
                                                  // accessed inside
                                                  // AtomicLong block
    private Set<InnerSubscriber> observers; // not synchronized as accessed
                                            // inside AtomicLong block

    public SortedMerge(final Comparator<T> comparator, final Subscriber<? super T> child) {
      this.comparator = comparator;
      this.child = child;
      child.add(childSubscription);
    }

    public void start(Iterable<Observable<? extends T>> os, AtomicLong requested) {
      this.requested = requested;

      // create a subscriber for each observable in the passed collection
      Map<InnerSubscriber, Observable<? extends T>> map = new HashMap<InnerSubscriber, Observable<? extends T>>();
      for (Observable<? extends T> o : os) {
        InnerSubscriber io = new InnerSubscriber();
        map.put(io, o);
        childSubscription.add(io);
      }

      // queue of values with their originating observers ordered by values
      queue = new PriorityQueue<ValueSourcePair>(map.size());

      // set of pending source observers - items will not be emitted down the
      // stream until the set is empty
      this.observers = new HashSet<InnerSubscriber>(map.keySet());

      // subscribe to all sources
      for (Map.Entry<InnerSubscriber, Observable<? extends T>> e : map.entrySet()) {
        e.getValue().unsafeSubscribe(e.getKey());
      }

    }

    /**
     * Tries to pick items from pending sources. Not synchronized as accessed
     * inside COUNTER_UPDATER block
     * 
     * @param pending
     *          set of source observers pending a value for the merge
     * @return true if we need a shutdown
     */
    boolean pickPendingItems(Set<InnerSubscriber> pending) {
      // peek for a potential onCompleted event
      for (Iterator<InnerSubscriber> iter = pending.iterator(); iter.hasNext();) {
        InnerSubscriber subscr = iter.next();
        if (subscr.isUnsubscribed()) {
          // we should remove completed observer from the pending set
          iter.remove();
        }

        RxRingBuffer buffer = subscr.items;
        Object n = buffer.peek();

        if (n == null) {
          // we should keep this observer as pending
          continue;
        }

        if (buffer.isCompleted(n)) {
          iter.remove();
          // this observer indicates completion - unsubscribe and remove from
          // pending
          childSubscription.remove(subscr);
          if (!childSubscription.hasSubscriptions()) {
            // it is last upstream observer so onComplete so shut down
            child.onCompleted();
            return true;
          }
        } else {
          // got new data add to the priority queue and remove from pending set
          @SuppressWarnings("unchecked")
          T value = (T) buffer.getValue(n);
          ValueSourcePair tuple = new ValueSourcePair(value, subscr);
          queue.add(tuple);
          // remove from pending set
          iter.remove();
        }
      }
      return false;

    }

    /**
     * Polls a top item from the priority queue. Adds the top source back to
     * pending set. Not synchronized as accessed inside COUNTER_UPDATER block
     * 
     * @param pending
     *          set of source observers pending a value for the merge
     * @return true if we need a shutdown
     */
    private boolean pollTopItem(Set<InnerSubscriber> pending) {
      // get a next item in order from the priority queue
      ValueSourcePair tuple = queue.poll();
      T value = tuple.value;

      InnerSubscriber observer = tuple.source;
      // mark the wining observer as pending again
      pending.add(observer);
      try {
        // emit the next item in order
        child.onNext(value);
        // we emitted so decrement the requested counter
        requested.decrementAndGet();
        observer.emitted++;
      } catch (Throwable e) {
        Exceptions.throwOrReport(e, child, value);
        return true;
      }
      // now remove
      RxRingBuffer buffer = observer.items;
      buffer.poll();
      // eagerly check if the next item on this queue is an onComplete
      if (buffer.isCompleted(buffer.peek())) {
        // we need to unsubscribe and remove from pending
        pending.remove(observer);
        childSubscription.remove(observer);
        if (!childSubscription.hasSubscriptions()) {
          // it is last upstream observer so onComplete so shut down
          child.onCompleted();
          return true;
        }
        return false; // dont request for this observer
      }
      if (observer.emitted > THRESHOLD) {
        observer.requestMore(observer.emitted);
        observer.emitted = 0;
      }
      return false;
    }

    /**
     * check if we have values for each active (subscribed) source and emit top
     * value if we do
     * 
     * This will only allow one thread at a time to do the work, but ensures via
     * `counter` increment/decrement that there is always once who acts on each
     * `tick`. Same concept as used in OperationObserveOn.
     * 
     */
    void tick() {
      final Set<InnerSubscriber> pending = this.observers;
      if (pending == null) {
        // nothing yet to do (initial request from Producer)
        return;
      }
      if (getAndIncrement() == 0) {
        final AtomicLong requested = this.requested;
        do {
          while (true) {
            if (pickPendingItems(pending)) {
              return;
            }
            // we only emit if requested > 0 and no observer pending a value
            if (requested.get() > 0 && pending.isEmpty()) {
              if (pollTopItem(pending)) {
                return;
              }
            } else {
              break;
            }
          }
        } while (decrementAndGet() > 0);
      }

    }

    // used to observe each Observable we are sort-merging together
    // it collects one item per source into an internal priority queue
    final class InnerSubscriber extends Subscriber<T> {
      // Concurrent* since we need to read it from across threads
      final RxRingBuffer items = RxRingBuffer.getSpmcInstance();
      int emitted = 0; // not volatile/synchronized as accessed inside
                       // COUNTER_UPDATER block

      @Override
      public void onStart() {
        request(RxRingBuffer.SIZE);
      }

      public void requestMore(long n) {
        request(n);
      }

      @Override
      public void onCompleted() {
        items.onCompleted();
        tick();
      }

      @Override
      public void onError(Throwable e) {
        // emit error immediately and shut down
        child.onError(e);
      }

      @Override
      public void onNext(Object t) {
        try {
          items.onNext(t);
        } catch (MissingBackpressureException e) {
          onError(e);
        }
        tick();
      }
    };

    final class ValueSourcePair implements Comparable<ValueSourcePair> {
      T value;
      InnerSubscriber source;

      ValueSourcePair(T value, InnerSubscriber source) {
        this.value = value;
        this.source = source;
      }

      @SuppressWarnings("unchecked")
      @Override
      public int compareTo(ValueSourcePair o) {
        if (comparator != null) {
          return comparator.compare(value, o.value);
        } else {
          Comparable<? super T> comparable = (Comparable<? super T>) value;
          return comparable.compareTo(o.value);
        }
      }

      @Override
      public String toString() {
        return String.valueOf(value);
      }

    }

  }

}
