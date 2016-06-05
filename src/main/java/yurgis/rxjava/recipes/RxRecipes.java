package yurgis.rxjava.recipes;

import java.util.Comparator;

import rx.Observable;

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
  public static <T> Observable<T> mergeSorted(Observable<T> o1, Observable<T> o2) {
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
  public static <T> Observable<T> mergeSorted(Observable<T> o1, Observable<T> o2, Observable<T> o3, Observable<T> o4) {
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
  public static <T> Observable<T> mergeSorted(Observable<Observable<T>> sources) {
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
  public static <T> Observable<T> mergeSorted(Observable<T> o1, Observable<T> o2, Comparator<T> comparator) {
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
  public static <T> Observable<T> mergeSorted(Observable<T> o1, Observable<T> o2, Observable<T> o3,
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
  public static <T> Observable<T> mergeSorted(Observable<T> o1, Observable<T> o2, Observable<T> o3, Observable<T> o4,
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
  public static <T> Observable<T> mergeSorted(Observable<Observable<T>> sources, Comparator<T> comparator) {
    return sources.lift(new OperatorMergeSorted<T>(comparator));
  }

}
