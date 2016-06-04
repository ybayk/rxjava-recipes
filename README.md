# RxJavaRecipes

A small repo with reusable custom operators and utilities on top of RxJava.

### Include as a depenency to your project

#### Maven

#### Gradle

### OperatorMergeSorted

Merges source Observables into one Observable, in the order specified by the provided comparator (assuming the source Observable's have their items pre-sorted in the order consistent with the comparator). 

#### Usage

```java
Observable<Integer> o1 = Observable.just(2, 4, 6, 8, 10, 20);
    Observable<Integer> o2 = Observable.just(1, 3, 5, 7, 9,  99);
    Observable<Integer> merged = Observable.just(o1, o2)
        .lift(new OperatorMergeSorted<Integer>(naturalComparator))
```
