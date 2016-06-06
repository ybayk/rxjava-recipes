package yurgis.rxjava.recipes;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.Assert;
import org.junit.Test;

import rx.Observable;
import rx.Scheduler;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.schedulers.Schedulers;

public class RxRecipesTest {

  static final int EPSILON = 50;

  final static Comparator<Integer> reverseComparator = new Comparator<Integer>() {

    @Override
    public int compare(Integer o1, Integer o2) {
      return o2.compareTo(o1);
    }
    
  };
  
  
  @Test
  public void testMergeSortedTwo() {
    Observable<Integer> o1 = Observable.just(1, 3, 5);
    Observable<Integer> o2 = Observable.just(2, 4, 6);
    List<Integer> list = RxRecipes.mergeSorted(o1, o2).toList().toBlocking().single();
    Assert.assertArrayEquals(new Integer[] {1,2,3,4,5,6}, list.toArray(new Integer[0]));
  }

  @Test
  public void testMergeSortedThree() {
    Observable<Integer> o1 = Observable.just(1, 3, 5);
    Observable<Integer> o2 = Observable.just(2, 4, 6);
    Observable<Integer> o3 = Observable.just(7, 8, 9);
    List<Integer> list = RxRecipes.mergeSorted(o1, o2, o3).toList().toBlocking().single();
    Assert.assertArrayEquals(new Integer[] {1,2,3,4,5,6,7,8,9}, list.toArray(new Integer[0]));
  }

  @Test
  public void testMergeSortedFour() {
    Observable<Integer> o1 = Observable.just(1, 3, 5);
    Observable<Integer> o2 = Observable.just(2, 4, 6);
    Observable<Integer> o3 = Observable.just(7, 8, 9);
    Observable<Integer> o4 = Observable.just(-3, -2, -1);
    List<Integer> list = RxRecipes.mergeSorted(o1, o2, o3, o4).toList().toBlocking().single();
    Assert.assertArrayEquals(new Integer[] {-3,-2,-1,1,2,3,4,5,6,7,8,9}, list.toArray(new Integer[0]));
  }


  @Test
  public void testMergeSortedMany() {
    Observable<Integer> o1 = Observable.just(1, 3, 5);
    Observable<Integer> o2 = Observable.just(2, 4, 6);
    Observable<Integer> o3 = Observable.just(7, 8, 9);
    Observable<Integer> o4 = Observable.just(-3, -2, -1);
    List<Integer> list = RxRecipes.mergeSorted(Observable.just(o1, o2, o3, o4)).toList().toBlocking().single();
    Assert.assertArrayEquals(new Integer[] {-3,-2,-1,1,2,3,4,5,6,7,8,9}, list.toArray(new Integer[0]));
  }
  
  @Test
  public void testMergeSortedTwoWithComparator() {
    Observable<Integer> o1 = Observable.just(5, 3, 1);
    Observable<Integer> o2 = Observable.just(6, 4, 2);
    List<Integer> list = RxRecipes.mergeSorted(o1, o2, reverseComparator).toList().toBlocking().single();
    Assert.assertArrayEquals(new Integer[] {6,5,4,3,2,1}, list.toArray(new Integer[0]));
  }

  @Test
  public void testMergeSortedTwoWithLambdaComparator() {
    Observable<Integer> o1 = Observable.just(5, 3, 1);
    Observable<Integer> o2 = Observable.just(6, 4, 2);
    List<Integer> list = RxRecipes.mergeSorted(o1, o2, (v1,v2)->(v2-v1)).toList().toBlocking().single();
    Assert.assertArrayEquals(new Integer[] {6,5,4,3,2,1}, list.toArray(new Integer[0]));
  }
  
  @Test
  public void testMergeSortedThreeWithComparator() {
    Observable<Integer> o1 = Observable.just(5, 3, 1);
    Observable<Integer> o2 = Observable.just(6, 4, 2);
    Observable<Integer> o3 = Observable.just(9, 8, 7);
    List<Integer> list = RxRecipes.mergeSorted(o1, o2, o3, reverseComparator).toList().toBlocking().single();
    Assert.assertArrayEquals(new Integer[] {9,8,7,6,5,4,3,2,1}, list.toArray(new Integer[0]));
  }

  @Test
  public void testMergeSortedFourWithComparator() {
    Observable<Integer> o1 = Observable.just(5, 3, 1);
    Observable<Integer> o2 = Observable.just(6, 4, 2);
    Observable<Integer> o3 = Observable.just(9, 8, 7);
    Observable<Integer> o4 = Observable.just(-1, -2, -3);
    List<Integer> list = RxRecipes.mergeSorted(o1, o2, o3, o4, reverseComparator).toList().toBlocking().single();
    Assert.assertArrayEquals(new Integer[] {9,8,7,6,5,4,3,2,1,-1,-2,-3}, list.toArray(new Integer[0]));
  }
  
  @Test
  public void testMergeSortedManyWithComparator() {
    Observable<Integer> o1 = Observable.just(5, 3, 1);
    Observable<Integer> o2 = Observable.just(6, 4, 2);
    Observable<Integer> o3 = Observable.just(9, 8, 7);
    Observable<Integer> o4 = Observable.just(-1, -2, -3);
    List<Integer> list = RxRecipes.mergeSorted(Observable.just(o1, o2, o3, o4), reverseComparator).toList().toBlocking().single();
    Assert.assertArrayEquals(new Integer[] {9,8,7,6,5,4,3,2,1,-1,-2,-3}, list.toArray(new Integer[0]));
  }
 
  public void testFastSlowInterval(AtomicBoolean fast, Observable<Long> o, long initial, long fastPeriod, long slowPeriod) {
     AtomicLong start = new AtomicLong();
    
    List<Long> ticks = o.take(6).toList().toBlocking().single();
    Assert.assertArrayEquals("should start from 0L", new Long[] {0L,1L,2L,3L,4L,5L}, ticks.toArray(new Long[0]));

    List<Long> ticks2 = o.take(6).toList().toBlocking().single();
    Assert.assertArrayEquals("should start from 0L", new Long[] {0L,1L,2L,3L,4L,5L}, ticks2.toArray(new Long[0]));
    
    List<Long> ticks3 = new ArrayList<Long>();
    List<Long> times = o.doOnNext(tick->fast.set(tick < 2))
      .doOnNext(tick->ticks3.add(tick))
      .doOnSubscribe(()->start.set(System.currentTimeMillis()))
      .map(tick->System.currentTimeMillis() - start.get())
      .take(6)
      .toList().toBlocking().single();

    Assert.assertArrayEquals(new Long[] {0L,1L,2L,3L,4L,5L}, ticks3.toArray(new Long[0]));
    
    Assert.assertTrue(times.get(0) >= initial - EPSILON);
    Assert.assertTrue(times.get(0) <= initial + EPSILON);
    
    //fast
    Assert.assertTrue(times.get(1) - times.get(0) >= fastPeriod-EPSILON);
    Assert.assertTrue(times.get(1) - times.get(0) <= fastPeriod+EPSILON);
    
    //fast
    Assert.assertTrue(times.get(2) - times.get(1) >= fastPeriod-EPSILON);
    Assert.assertTrue(times.get(2) - times.get(1) <= fastPeriod+EPSILON);
    
    //transitional
    Assert.assertTrue(times.get(3) - times.get(2) >= fastPeriod-EPSILON);
    Assert.assertTrue(times.get(3) - times.get(2) <= slowPeriod+EPSILON);
    
    //slow
    Assert.assertTrue(times.get(4) - times.get(3) >= slowPeriod-EPSILON);
    Assert.assertTrue(times.get(4) - times.get(3) <= slowPeriod+EPSILON);
    
    //slow
    Assert.assertTrue(times.get(5) - times.get(4) >= slowPeriod-EPSILON);
    Assert.assertTrue(times.get(5) - times.get(4) <= slowPeriod+EPSILON);
  }
  
  @Test
  public void testFastSlowInterval() {
    AtomicBoolean fast = new AtomicBoolean(true);
    Observable<Long> o = RxRecipes.fastSlowInterval(fast, 50, 100, 300, TimeUnit.MILLISECONDS, Schedulers.computation());
    testFastSlowInterval(fast, o, 50, 100, 300);
  }

  @Test
  public void testFastSlowIntervalDefaultScheduler() {
    AtomicBoolean fast = new AtomicBoolean(true);
    Observable<Long> o = RxRecipes.fastSlowInterval(fast, 50, 100, 300, TimeUnit.MILLISECONDS);
    testFastSlowInterval(fast, o, 50, 100, 300);
  }

  @Test
  public void testFastSlowIntervalDefaultInitial() {
    AtomicBoolean fast = new AtomicBoolean(true);
    Observable<Long> o = RxRecipes.fastSlowInterval(fast, 100, 300, TimeUnit.MILLISECONDS, Schedulers.computation());
    testFastSlowInterval(fast, o, 100, 100, 300);
  }

  @Test
  public void testFastSlowIntervalDefaultInitialAndScheduler() {
    AtomicBoolean fast = new AtomicBoolean(true);
    Observable<Long> o = RxRecipes.fastSlowInterval(fast, 100, 300, TimeUnit.MILLISECONDS);
    testFastSlowInterval(fast, o, 100, 100, 300);
  }
  
  public void testPausableInterval(Observable<Long> o, AtomicBoolean pause, long initial, long period) {
    AtomicLong start = new AtomicLong();
    List<Long> ticks = o.take(6).toList().toBlocking().single();
    Assert.assertArrayEquals("should start from 0L", new Long[] {0L,1L,2L,3L,4L,5L}, ticks.toArray(new Long[0]));

    List<Long> ticks2 = o.take(6).toList().toBlocking().single();
    Assert.assertArrayEquals("should restart from 0L", new Long[] {0L,1L,2L,3L,4L,5L}, ticks2.toArray(new Long[0]));
        
    List<Long> ticks3 = new ArrayList<Long>();
    List<Long> times = o.doOnNext(tick->pause.set(tick == 2)) //set pause on a second tick
      .doOnNext(tick->ticks3.add(tick))
      .doOnSubscribe(()->start.set(System.currentTimeMillis()))
      //schedule resume
      .doOnSubscribe(()->Executors.newScheduledThreadPool(1).schedule(()->{pause.set(false);}, initial + period * 4, TimeUnit.MILLISECONDS))
      .map(tick->System.currentTimeMillis() - start.get())
      .take(6)
      .toList().toBlocking().single();
    
    Assert.assertArrayEquals(new Long[] {0L,1L,2L,3L,4L,5L}, ticks3.toArray(new Long[0]));
    
    Assert.assertTrue(times.get(0) >= initial - EPSILON);
    Assert.assertTrue(times.get(0) <= initial + EPSILON);
    
    //no pause
    Assert.assertTrue(times.get(1) - times.get(0) >= period - EPSILON);
    Assert.assertTrue(times.get(1) - times.get(0) <= period + EPSILON);
    
    //before pause
    Assert.assertTrue(times.get(2) - times.get(1) >= period - EPSILON);
    Assert.assertTrue(times.get(2) - times.get(1) <= period + EPSILON);
    
    //after pause
    Assert.assertTrue(times.get(3) - times.get(2) >= period * 2 - EPSILON);
    Assert.assertTrue(times.get(3) - times.get(2) <= period * 3 + EPSILON);

    //no pause
    Assert.assertTrue(times.get(4) - times.get(3) >= period - EPSILON);
    Assert.assertTrue(times.get(4) - times.get(3) <= period + EPSILON);
    
    //no pause
    Assert.assertTrue(times.get(5) - times.get(4) >= period - EPSILON);
    Assert.assertTrue(times.get(5) - times.get(4) <= period + EPSILON);
    
    
    
  }
  
  
  @Test
  public void testPausableInterval() {
    AtomicBoolean pause = new AtomicBoolean(false);
    Observable<Long> o = RxRecipes.pausableInterval(pause, 50, 100, TimeUnit.MILLISECONDS, Schedulers.computation());
    testPausableInterval(o, pause, 50, 100);
  }
  
  @Test
  public void testPausableIntervalDefaultScheduler() {
    AtomicBoolean pause = new AtomicBoolean(false);
    Observable<Long> o = RxRecipes.pausableInterval(pause, 50, 100, TimeUnit.MILLISECONDS);
    testPausableInterval(o, pause, 50, 100);
  }
  
  @Test
  public void testPausableIntervalDefaultInitial() {
    AtomicBoolean pause = new AtomicBoolean(false);
    Observable<Long> o = RxRecipes.pausableInterval(pause, 100, TimeUnit.MILLISECONDS, Schedulers.computation());
    testPausableInterval(o, pause, 100, 100);
  }

  
  @Test
  public void testPausableIntervalDefaultInitialAndScheduler() {
    AtomicBoolean pause = new AtomicBoolean(false);
    Observable<Long> o = RxRecipes.pausableInterval(pause, 100, TimeUnit.MILLISECONDS);
    testPausableInterval(o, pause, 100, 100);
  }
}
