# RxJavaRecipes

A small repo with reusable custom operators and utilities on top of RxJava.

### Include as a depenency to your project

#### Maven

#### Gradle

### OperatorMergeSorted

Merges source Observables into one Observable, in the order specified by the provided comparator (assuming the source Observable's have their items pre-sorted in the order consistent with the comparator). 
This operator supports backpressure which means that it will not fetch data from the source observable beyond of what you request. It will also work greate if you are merging observables that have a different emission pace.

#### Usage

```java
    //You can have one or more soure observables ordered naturally
    Observable<Integer> o1 = Observable.just(2, 4, 6, 8, 10);
    Observable<Integer> o2 = Observable.just(1, 3, 5, 7, 9);
    
    //all you need is to wrap then in a single observable and use a lift operator:
    Observable<Integer> merged = Observable.just(o1, o2)
        .lift(new OperatorMergeSorted<Integer>());
        
        
```
