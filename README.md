# RxJavaRecipes

A small repo with a set of minimal libraries with reusable custom operators and utilities on top of RxJava.
The core library depends only on https://github.com/ReactiveX/RxJava (1.1+) or (1.0.15+) and compatible with Java 6+.

##### Table of Contents  

[1. RxRecipes - Core Utils](#rxjavarecipescore)  
* [1.1. OperatorMergeSorted](#operatormergesorted)  
* [1.2. Pausable Interval](#pausableinterval)  
* [1.3. Fast/Slow Interval](#fastslowinterval)  

[2. RxGuava - ListenableFuture to Observable](#rxjavarecipesguava)  
* [2.1. From Iterator](#fromiterator)  
* [2.2. From Iterable](#fromiterable)  
* [2.3. From Scalar](#fromscalar)  

<a name="rxjavarecipescore"/>
## 1. RxRecipes

A core utility library with minimal dependencies.

### Include as a depenency to your project

#### Maven

To use it in your Maven build add:
```xml
  <repositories>
	<repository>
	    <id>jitpack.io</id>
	    <url>https://jitpack.io</url>
	</repository>
  </repositories>
```

and the dependency:

```xml
	<dependency>
		<groupId>com.github.ybayk.rxjava-recipes</groupId>
		<artifactId>rxjava-recipes</artifactId>
		<version>0.0.7</version>
	</dependency>
```

#### Gradle

```groovy
   repositories { 
        jcenter()
        maven { url "https://jitpack.io" }
   }
   dependencies {
         compile 'com.github.ybayk.rxjava-recipes:rxjava-recipes:0.0.7'
   }
```

<a name="operatormergesorted"/>
### 1.2. OperatorMergeSorted

This [operator](https://github.com/ybayk/rxjava-recipes/blob/master/src/main/java/ybayk/rxjava/recipes/OperatorMergeSorted.java) is very similar to the standard RxJava's [OperatorMerge](https://github.com/ReactiveX/RxJava/blob/1.x/src/main/java/rx/internal/operators/OperatorMerge.java) but its implementation is loosly based on and have the same performance characteristics as [OperatorZip](https://github.com/ReactiveX/RxJava/blob/1.x/src/main/java/rx/internal/operators/OperatorZip.java).
It merges source Observables into one Observable, but does it in the order specified by the provided comparator (assuming the source Observable's have their items pre-sorted in the order consistent with the comparator). 

This operator supports backpressure which means that:
* It will not fetch data from the source observable beyond of what you request. 
* It will also work fine if you are merging observables that have a different emission pace.
* You can merge sort very large or infinite sorted sequences 

#### Usage 

##### Natural Order

```java
    //You can have one or more soure observables ordered naturally
    Observable<Integer> o1 = Observable.just(2, 4, 6, 8, 10);
    //An observable source can be truly async too - let's delay one by a second
    Observable<Integer> o2 = Observable.just(1, 3, 5, 7, 9).delay(1, TimeUnit.SECONDS);

    //RxRecipes.mergeSorted() utility creates an observable as a sequence of source observables and use lift operator to "inject" OperatorMergeSorted
    Observable<Integer> merged = RxRecipes.mergeSorted(o1, o2);

    //The merged observable will emit items sorted
    System.out.println(merged.toList().toBlocking().single());
    
    //output:
    //[1, 2, 3, 4, 5, 6, 7, 8, 9, 10]
        
```

##### Custom Order

```java
    //each source observable is expected to have a sort order consistent with the custom comparator:
    Observable<Integer> o1 = Observable.just(10, 8, 6, 4, 2);
    Observable<Integer> o2 = Observable.just(9, 7, 5, 3, 1);

    //pass optional custom comparator
    Observable<Integer> merged = RxRecipes.mergeSorted(o1, o2, (v1, v2)->(v2 - v1));

    //The merged observable will emit integers sorted in descending order
    System.out.println(merged.toList().toBlocking().single());
    
    //output:
    //[10, 9, 8, 7, 6, 5, 4, 3, 2, 1]
        
```

<a name="pausableinterval"/>
### 1.3. Pausable Interval

Works exactly like RxJava's interval, but you can pause/resume it any time during subscription:

```java
    AtomicBoolean pause = new AtomicBoolean(false);
    long initialDelay = 50;
    long period = 100;
    Observable<Long> o = RxRecipes.pausableInterval(pause, initialDelay, period, TimeUnit.MILLISECONDS, Schedulers.computation());
    //...
    //somewhere in a middle of subscription
    pause.set(true); //pause
    //...
    pause.set(false); //resume
```

<a name="fastslowinterval"/>
### 3. Fast/Slow Interval

Interval that can emit in a fast or a slow pace:

```java
    AtomicBoolean fast = new AtomicBoolean(false);
    long initialDelay = 50;
    long fastPeriod = 100;
    long slowPeriod = 300;
    Observable<Long> o = RxRecipes.fastSlowInterval(fast, initialDelay, fastPeriod, slowPeriod, TimeUnit.MILLISECONDS, Schedulers.computation());
    //...
    //somewhere in a middle of subscription
    fast.set(true); //emit faster
    //...
    fast.set(false); //emit slower
```

## 1. RxGuava

A small library that helps convert Guava's ListenableFuture to Observable without having to block.
In addition to dependencies listed for the [Core Utils](#rxjavarecipescore) it also depends on Guava.

While RxJava lets you conveniently create an Observable from a Future via Observable.from(java.util.concurrent.Future), this method atually wastes a thread. It will either block a current thread or allocate a thread from a thread pool you pass via ObserveOn operator.
If you have a lot of futures to observe you may run into thread pool exhaustion. 

Possible solutions:
* Get rid of the Java 1.5 Futures in favor of other async abstractions such as callbacks, promises, etc.
* Switch to Guava's ListenableFuture
* Switch to Java 8's CompletableFuture
 
If you switch to Listenable- or CompletableFutures, using the existing Observable.from(java.util.concurrent.Future) is a mistake, because it still executes Future.get() and blocks a thread.

The correct way of building an Observable from ListenableFuture is to use the callback:

```java
...
Observable.create(new Observable.OnSubscribe<T>() {

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
    })
```

### Include as a depenency to your project

#### Maven

To use it in your Maven build add:
```xml
  <repositories>
	<repository>
	    <id>jitpack.io</id>
	    <url>https://jitpack.io</url>
	</repository>
  </repositories>
```

and the dependency:

```xml
	<dependency>
		<groupId>com.github.ybayk.rxjava-recipes</groupId>
		<artifactId>rxjava-recipes-guava</artifactId>
		<version>0.0.7</version>
	</dependency>
```

#### Gradle

```groovy
   repositories { 
        jcenter()
        maven { url "https://jitpack.io" }
   }
   dependencies {
         compile 'com.github.ybayk.rxjava-recipes:rxjava-recipes-guava:0.0.7'
   }
```

<a name="fromiterator"/>
### 2.1. RxGuava.fromIterator(...)

Create observable from an iterator future:
```java
    ListenableFuture<Iterator<MyItem>> future = someAsyncService.query(...);
    Observable<MyItem> o = RxGuava.fromIterator(future);
```
    
Create observable from an iterator future using a custom executor to handle future's callback
```java
    ListenableFuture<Iterator<MyItem>> future = someAsyncService.query(...);
    Executor executor = ...
    Observable<MyItem> o = RxGuava.fromIterator(future, executor);
```
    
Create observable from a lazy iterator future. The lazy future means it will not be evaluated until the Observable is subscribed:
```java
    Observable<MyItem> o = RxGuava.fromIterator(()->someAsyncService.query(...), future);
```

<a name="fromiterable"/>
### 2.2. RxGuava.fromIterable(...)

Create observable from an iterable future:
```java
    ListenableFuture<List<MyItem>> future = someAsyncService.query(...);
    Observable<MyItem> o = RxGuava.fromIterable(future);
```
    
Create observable from an iterable future using a custom executor to handle future's callback
```java
    ListenableFuture<List<MyItem>> future = someAsyncService.query(...);
    Executor executor = ...
    Observable<MyItem> o = RxGuava.fromIterator(future, executor);
```
    
Create observable from a lazy iterable future. The lazy future means it will not be evaluated until the Observable is subscribed:
```java
    Observable<MyItem> o = RxGuava.fromIterator(()->someAsyncService.query(...), future);
```

<a name="fromscalar"/>
### 2.3. RxGuava.fromScalar(...)

Create single item observable from a future:
```java
    ListenableFuture<MyItem> future = someAsyncService.query(...);
    Observable<MyItem> o = RxGuava.fromIterable(future);
```
    
Create single observable from a future using a custom executor to handle future's callback
```java
    ListenableFuture<MyItem> future = someAsyncService.query(...);
    Executor executor = ...
    Observable<MyItem> o = RxGuava.fromIterator(future, executor);
```
    
Create observable from a lazy future. The lazy future means it will not be evaluated until the Observable is subscribed:
```java
    Observable<MyItem> o = RxGuava.fromIterator(()->someAsyncService.query(...), future);
```
