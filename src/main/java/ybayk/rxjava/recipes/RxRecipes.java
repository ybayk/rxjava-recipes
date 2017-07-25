package ybayk.rxjava.recipes;

import java.util.Comparator;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import rx.Observable;
import rx.Scheduler;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.schedulers.Schedulers;

/**
 * Misc Utilities
 */
public class RxRecipes {

  /**
   * Merges two source {@link Observable}s into one {@code Observable}, without
   * any transformation, and emits items {@linkplain Comparable} natural
   * ordering.
   * 
   * @param o1
   *          1st source {@code Observable} to merge
   * @param o2
   *          2nd source {@code Observable} to merge
   * @param <T>
   *          the type of the items emitted by both the source and merged
   *          {@code Observable}s
   * @return an {@link Observable} that emits items {@linkplain Comparable}
   *         natural ordering.
   */
  public static <T> Observable<T> mergeSorted(Observable<? extends T> o1, Observable<? extends T> o2) {
    return Observable.just(o1, o2).lift(new OperatorMergeSorted<T>());
  }

  /**
   * Merges three source {@link Observable}s into one {@code Observable},
   * without any transformation, and emits items {@linkplain Comparable} natural
   * ordering.
   * 
   * @param o1
   *          1st source {@code Observable} to merge
   * @param o2
   *          2nd source {@code Observable} to merge
   * @param o3
   *          3rd source {@code Observable} to merge
   * @param <T>
   *          the type of the items emitted by both the source and merged
   *          {@code Observable}s
   * @return an {@link Observable} that emits items {@linkplain Comparable}
   *         natural ordering.
   */
  public static <T> Observable<T> mergeSorted(Observable<T> o1, Observable<T> o2, Observable<T> o3) {
    return Observable.just(o1, o2, o3).lift(new OperatorMergeSorted<T>());
  }

  /**
   * Merges four source {@link Observable}s into one {@code Observable}, without
   * any transformation, and emits items {@linkplain Comparable} natural
   * ordering.
   * 
   * @param o1
   *          1st source {@code Observable} to merge
   * @param o2
   *          2nd source {@code Observable} to merge
   * @param o3
   *          3rd source {@code Observable} to merge
   * @param o4
   *          4th source {@code Observable} to merge
   * @param <T>
   *          the type of the items emitted by both the source and merged
   *          {@code Observable}s
   * @return an {@link Observable} that emits items {@linkplain Comparable}
   *         natural ordering.
   */
  public static <T> Observable<T> mergeSorted(Observable<? extends T> o1, Observable<? extends T> o2, 
      Observable<? extends T> o3, Observable<? extends T> o4) {
    return Observable.just(o1, o2, o3, o4).lift(new OperatorMergeSorted<T>());
  }

  /**
   * Merges multiple source {@link Observable}s into one {@code Observable},
   * without any transformation, and emits items {@linkplain Comparable} natural
   * ordering.
   * 
   * @param sources
   *          an {@code Observable} that emits the source {@code Observable}s.
   * @param <T>
   *          the type of the items emitted by both the source and merged
   *          {@code Observable}s
   * @return an {@link Observable} that emits items {@linkplain Comparable}
   *         natural ordering.
   */
  public static <T> Observable<T> mergeSorted(Observable<Observable<? extends T>> sources) {
    return sources.lift(new OperatorMergeSorted<T>());
  }

  /**
   * Merges two source {@link Observable}s into one {@code Observable}, without
   * any transformation, and emits items in the order specified by the
   * {@code comparator}.
   * 
   * @param o1
   *          1st source {@code Observable} to merge
   * @param o2
   *          2nd source {@code Observable} to merge
   * @param comparator
   *          comparator to apply during sorted merge
   * @param <T>
   *          the type of the items emitted by both the source and merged
   *          {@code Observable}s
   * @return an {@link Observable} that emits items ordered by the specified
   *         {@code comparator}.
   */
  public static <T> Observable<T> mergeSorted(Observable<? extends T> o1, Observable<? extends T> o2, 
      Comparator<T> comparator) {
    return Observable.just(o1, o2).lift(new OperatorMergeSorted<T>(comparator));
  }

  /**
   * Merges three source {@link Observable}s into one {@code Observable},
   * without any transformation, and emits items in the order specified by the
   * {@code comparator}.
   * 
   * @param o1
   *          1st source {@code Observable} to merge
   * @param o2
   *          2nd source {@code Observable} to merge
   * @param o3
   *          3rd source {@code Observable} to merge
   * @param comparator
   *          comparator to apply during sorted merge
   * @param <T>
   *          the type of the items emitted by both the source and merged
   *          {@code Observable}s
   * @return an {@link Observable} that emits items ordered by the specified
   *         {@code comparator}.
   */
  public static <T> Observable<T> mergeSorted(Observable<? extends T> o1, Observable<? extends T> o2, Observable<? extends T> o3,
      Comparator<T> comparator) {
    return Observable.just(o1, o2, o3).lift(new OperatorMergeSorted<T>(comparator));
  }

  /**
   * Merges four source {@link Observable}s into one {@code Observable}, without
   * any transformation, and emits items in the order specified by the
   * {@code comparator}.
   * 
   * @param o1
   *          1st source {@code Observable} to merge
   * @param o2
   *          2nd source {@code Observable} to merge
   * @param o3
   *          3rd source {@code Observable} to merge
   * @param o4
   *          4th source {@code Observable} to merge
   * @param comparator
   *          comparator to apply during sorted merge
   * @param <T>
   *          the type of the items emitted by both the source and merged
   *          {@code Observable}s
   * @return an {@link Observable} that emits items ordered by the specified
   *         {@code comparator}.
   */
  public static <T> Observable<T> mergeSorted(Observable<? extends T> o1, Observable<? extends T> o2, 
      Observable<? extends T> o3, Observable<? extends T> o4,
      Comparator<T> comparator) {
    return Observable.just(o1, o2, o3, o4).lift(new OperatorMergeSorted<T>(comparator));
  }

  /**
   * Merges multiple source {@link Observable}s into one {@code Observable},
   * without any transformation, and emits items in the order specified by the
   * {@code comparator}.
   * 
   * @param sources
   *          an {@code Observable} that emits the source {@code Observable}s.
   * @param comparator
   *          comparator to apply during sorted merge
   * @param <T>
   *          the type of the items emitted by both the source and merged
   *          {@code Observable}s
   * @return an {@link Observable} that emits items ordered by the specified
   *         {@code comparator}.
   */
  public static <T> Observable<T> mergeSorted(Observable<Observable<? extends T>> sources, Comparator<T> comparator) {
    return sources.lift(new OperatorMergeSorted<T>(comparator));
  }

  /**
   * Returns an Observable that emits a {@code 0L} after the {@code initialDelay} and ever increasing numbers
   * after each {@code fastPeriod} or {@code slowPeriod} (depending on {@code fast} flag value) of time thereafter, 
   * on a specified {@link Scheduler}.
   * <dl>
   *  <dt><b>Backpressure Support:</b></dt>
   *  <dd>This operator does not support backpressure as it uses time. If the downstream needs a slower rate
   *      it should slow the timer or use something like {@link Observable#onBackpressureDrop()}.</dd>
   *  <dt><b>Scheduler:</b></dt>
   *  <dd>you specify which {@link Scheduler} this operator will use</dd>
   * </dl>
   * 
   * @param fast
   *          mutable flag indicating whether the timer emits in fast mode ({@code true}) or slow mode ({@code false})
   * @param initialDelay
   *          the initial delay time to wait before emitting the first value of 0L
   * @param fastPeriod
   *          the period of time between emissions of the subsequent numbers in fast mode. 
   *          Should be less than {@code slowPeriod}
   * @param slowPeriod
   *          the period of time between emissions of the subsequent numbers in slow mode.
   *          Should be greater than {@code fastPeriod}
   * @param unit
   *          the time unit for both {@code initialDelay}, {@code slowPeriod}, and {@code fastPeriod}
   * @param scheduler
   *          the Scheduler on which the waiting happens and items are emitted
   * 
   * @return a new fast/slow interval observable
   */
  public static Observable<Long> fastSlowInterval(final AtomicBoolean fast, final long initialDelay,
      final long fastPeriod, final long slowPeriod, TimeUnit unit, Scheduler scheduler) {

    if (fastPeriod >= slowPeriod) {
      throw new IllegalArgumentException("slow period should be greater than fast");
    }
    return Observable.merge(
        Observable.interval(initialDelay, slowPeriod, unit, scheduler).filter(new Func1<Long, Boolean>() {

          @Override
          public Boolean call(Long tick) {
            return !fast.get();
          }

        }), Observable.interval(initialDelay, fastPeriod, unit, scheduler).filter(new Func1<Long, Boolean>() {

          @Override
          public Boolean call(Long tick) {
            return fast.get();
          }

        })).scan(new Func2<Long,Long,Long>() {

          @Override
          public Long call(Long acc, Long tick) {
            return acc + 1;
          }
          
        });
  }

  /**
   * Same as calling {@link #fastSlowInterval(AtomicBoolean, long, long, long, TimeUnit, Scheduler)} 
   * with {@code initialDelay == fast.get()? fastPeriod : slowPeriod}
   * @param fast
   *          mutable flag indicating whether the timer emits in fast mode ({@code true}) or slow mode ({@code false})
   * @param fastPeriod
   *          the period of time between emissions of the subsequent numbers in fast mode. 
   *          Should be less than {@code slowPeriod}
   * @param slowPeriod
   *          the period of time between emissions of the subsequent numbers in slow mode.
   *          Should be greater than {@code fastPeriod}
   * @param unit
   *          the time unit for both {@code initialDelay}, {@code slowPeriod}, and {@code fastPeriod}
   * @param scheduler
   *          the Scheduler on which the waiting happens and items are emitted
   * 
   * @return a new fast/slow interval observable
   */
  public static Observable<Long> fastSlowInterval(final AtomicBoolean fast, 
      final long fastPeriod, final long slowPeriod, TimeUnit unit, Scheduler scheduler) {
    return fastSlowInterval(fast, fast.get()? fastPeriod : slowPeriod, fastPeriod, slowPeriod, unit, scheduler);
  }  

  /**
   * Same as calling {@link #fastSlowInterval(AtomicBoolean, long, long, TimeUnit, Scheduler)} 
   * with {@code scheduler == }{@link Schedulers#computation()}
   * @param fast
   *          mutable flag indicating whether the timer emits in fast mode ({@code true}) or slow mode ({@code false})
   * @param fastPeriod
   *          the period of time between emissions of the subsequent numbers in fast mode. 
   *          Should be less than {@code slowPeriod}
   * @param slowPeriod
   *          the period of time between emissions of the subsequent numbers in slow mode.
   *          Should be greater than {@code fastPeriod}
   * @param unit
   *          the time unit for both {@code initialDelay}, {@code slowPeriod}, and {@code fastPeriod}
   * 
   * @return a new fast/slow interval observable
   */
  public static Observable<Long> fastSlowInterval(final AtomicBoolean fast, 
      final long fastPeriod, final long slowPeriod, TimeUnit unit) {
    return fastSlowInterval(fast, fastPeriod, slowPeriod, unit, Schedulers.computation());
  }  
  
  /**
   * Same as calling {@link #fastSlowInterval(AtomicBoolean, long, long, long, TimeUnit, Scheduler)} 
   * with {@code scheduler == }{@link Schedulers#computation()}
   * @param fast
   *          mutable flag indicating whether the timer emits in fast mode ({@code true}) or slow mode ({@code false})
   * @param initialDelay
   *          the initial delay time to wait before emitting the first value of 0L
   * @param fastPeriod
   *          the period of time between emissions of the subsequent numbers in fast mode. 
   *          Should be less than {@code slowPeriod}
   * @param slowPeriod
   *          the period of time between emissions of the subsequent numbers in slow mode.
   *          Should be greater than {@code fastPeriod}
   * @param unit
   *          the time unit for both {@code initialDelay}, {@code slowPeriod}, and {@code fastPeriod}
   * 
   * @return a new fast/slow interval observable
   */
  public static Observable<Long> fastSlowInterval(final AtomicBoolean fast, final long initialDelay,
      final long fastPeriod, final long slowPeriod, TimeUnit unit) {
    return fastSlowInterval(fast, initialDelay, fastPeriod, slowPeriod, unit, Schedulers.computation());

  }
  
  /**
   * Returns an Observable that emits a {@code 0L} after the {@code initialDelay} and ever increasing numbers
   * after each {@code period} of time thereafter, 
   * on a specified {@link Scheduler} as long as {@code pause} flag holds {@code false}.
   * <dl>
   *  <dt><b>Backpressure Support:</b></dt>
   *  <dd>This operator does not support backpressure as it uses time. If the downstream needs a slower rate
   *      it should slow the timer or use something like {@link Observable#onBackpressureDrop}.</dd>
   *  <dt><b>Scheduler:</b></dt>
   *  <dd>you specify which {@link Scheduler} this operator will use</dd>
   * </dl>
   * 
   * @param pause
   *          mutable flag indicating whether the emission is paused or not
   *          emitting
   * @param initialDelay
   *          the initial delay time to wait before emitting the first value of 0L
   * @param period
   *          the period of time between emissions of the subsequent numbers
   * @param unit
   *          the time unit for both {@code initialDelay}, {@code slowPeriod}, and {@code fastPeriod}
   * @param scheduler
   *          the Scheduler on which the waiting happens and items are emitted
   * 
   * @return a new pausable interval observable
   */
  public static final Observable<Long> pausableInterval(final AtomicBoolean pause, final long initialDelay, 
      final long period, TimeUnit unit, Scheduler scheduler) {

    return Observable.interval(initialDelay, period, unit, scheduler).filter(new Func1<Long, Boolean>() {

      @Override
      public Boolean call(Long tick) {
        return !pause.get();
      }

    }).scan(new Func2<Long,Long,Long>() {

      @Override
      public Long call(Long acc, Long tick) {
        return acc + 1;
      }
      
    });
  }

  /**
   * Same as calling {@link #pausableInterval(AtomicBoolean, long, long, TimeUnit, Scheduler)}
   * with {@code initialDelay == period}
   * 
   * @param pause
   *          mutable flag indicating whether the emission is paused or not
   *          emitting
   * @param period
   *          the period of time between emissions of the subsequent numbers
   * @param unit
   *          the time unit for both {@code initialDelay}, {@code slowPeriod}, and {@code fastPeriod}
   * @param scheduler
   *          the Scheduler on which the waiting happens and items are emitted
   * 
   * @return a new pausable interval observable
   */
  public static final Observable<Long> pausableInterval(final AtomicBoolean pause, 
      final long period, TimeUnit unit, Scheduler scheduler) {
    return pausableInterval(pause, period, period, unit, scheduler);
  }  


  /**
   * Same as calling {@link #pausableInterval(AtomicBoolean, long, TimeUnit, Scheduler)}
   * with {@code scheduler == }{@link Schedulers#computation()}
   * 
   * @param pause
   *          mutable flag indicating whether the emission is paused or not
   *          emitting
   * @param period
   *          the period of time between emissions of the subsequent numbers
   * @param unit
   *          the time unit for both {@code initialDelay}, {@code slowPeriod}, and {@code fastPeriod}
    * 
   * @return a new pausable interval observable
   */
  public static final Observable<Long> pausableInterval(final AtomicBoolean pause, 
      final long period, TimeUnit unit) {
    return pausableInterval(pause, period, unit, Schedulers.computation());
  }
  
  /**
   * Same as calling {@link #pausableInterval(AtomicBoolean, long, long, TimeUnit, Scheduler)}
   * with {@code scheduler == }{@link Schedulers#computation()}
   * 
   * @param pause
   *          mutable flag indicating whether the emission is paused or not
   *          emitting
   * @param initialDelay
   *          the initial delay time to wait before emitting the first value of 0L
   * @param period
   *          the period of time between emissions of the subsequent numbers
   * @param unit
   *          the time unit for both {@code initialDelay}, {@code slowPeriod}, and {@code fastPeriod}
   * 
   * @return a new pausable interval observable
   */
  public static final Observable<Long> pausableInterval(final AtomicBoolean pause, final long initialDelay, 
      final long period, TimeUnit unit) {
    return pausableInterval(pause, initialDelay, period, unit, Schedulers.computation());
  }  
  
}
