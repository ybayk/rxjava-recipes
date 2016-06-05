package yurgis.rxjava.recipes;

import java.util.Comparator;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import rx.Observable;

public class RxRecipesTest {


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
  public void testMergeSortedThreeWithComparator() {
    Observable<Integer> o1 = Observable.just(5, 3, 1);
    Observable<Integer> o2 = Observable.just(6, 4, 2);
    Observable<Integer> o3 = Observable.just(9, 8, 7);
    List<Integer> list = RxRecipes.mergeSorted(o1, o2, o3, reverseComparator).toList().toBlocking().single();
    Assert.assertArrayEquals(new Integer[] {9,8,7,6,5,4,3,2,1}, list.toArray(new Integer[0]));
  }

  @Test
  public void testMergeSortedFouWithComparatorr() {
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
  
}
