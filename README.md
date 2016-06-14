##### Table of Contents  
[1. RxJavaRecipes Core Utils](#rxjavarecipescore)  
[1.1. OperatorMergeSorted](#operatormergesorted)  
[1.2. Pausable Interval](#pausableinterval)  
[1.3. Fast/Slow Interval](#fastslowinterval)  
[2. RxJavaRecipes Guava Utils](#rxjavarecipesguava)  
[2.1. From Iterator](#fromiterator)  
[2.2. From Iterable](#fromiterable)  
[2.3. From Scalae](#fromscalar)  

<a name="rxjavarecipescore"/>
## 1. RxJavaRecipes

A small repo with reusable custom operators and utilities on top of RxJava.

It depends only on https://github.com/ReactiveX/RxJava (1.1.+) or (1.0.15+) and compatible with Java 6+.

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
		<version>0.0.5</version>
	</dependency>
```

#### Gradle

```groovy
   repositories { 
        jcenter()
        maven { url "https://jitpack.io" }
   }
   dependencies {
         compile 'com.github.ybayk.rxjava-recipes:rxjava-recipes:0.0.5'
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
In addition to dependencies listed for the core library it also depends on Guava.

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
		<version>0.0.6</version>
	</dependency>
```

#### Gradle

```groovy
   repositories { 
        jcenter()
        maven { url "https://jitpack.io" }
   }
   dependencies {
         compile 'com.github.ybayk.rxjava-recipes:rxjava-recipes-guava:0.0.6'
   }
```

<a name="fromiterator"/>
### 2.1. RxGuava.fromIterator(...)

TODO

<a name="fromiterable"/>
### 2.2. RxGuava.fromIterable(...)

TODO

<a name="fromscalar"/>
### 2.3. RxGuava.fromScalar(...)

TODO

