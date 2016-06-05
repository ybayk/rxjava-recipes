# RxJavaRecipes

A small repo with reusable custom operators and utilities on top of RxJava.
It depends only on https://github.com/ReactiveX/RxJava and compatible with Java 6+.

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
		<groupId>com.github.yurgis2</groupId>
		<artifactId>rxjava-recipes</artifactId>
		<version>v0.0.1</version>
	</dependency>
```

#### Gradle

```groovy
   repositories { 
        jcenter()
        maven { url "https://jitpack.io" }
   }
   dependencies {
         compile 'com.github.yurgis2:rxjava-recipes:v0.0.1'
   }
```

### 1. OperatorMergeSorted

This operator is very similar to the standard RxJava's MergeOperator but its implementation is loosly based on ZipOperator.
It merges source Observables into one Observable, but does it in the order specified by the provided comparator (assuming the source Observable's have their items pre-sorted in the order consistent with the comparator). 

This operator supports backpressure which means that:
* It will not fetch data from the source observable beyond of what you request. 
* It will also work greate if you are merging observables that have a different emission pace.
* You can merge sort very large or infinite sorted sequences 

#### Usage 

##### Natural Order

```java
    //You can have one or more soure observables ordered naturally
    Observable<Integer> o1 = Observable.just(2, 4, 6, 8, 10);
    //An observable source can be truly async too - let's delay one by a second
    Observable<Integer> o2 = Observable.just(1, 3, 5, 7, 9).delay(1, TimeUnit.SECONDS);

    //create an observable as a sequence of source observables and use lift operator to "inject" OperatorMergeSorted
    Observable<Integer> merged = Observable.just(o1, o2)
        .lift(new OperatorMergeSorted<Integer>());

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

    //create an observable as a sequence of source observables and use lift operator to inject OperatorMergeSorted
    Observable<Integer> merged = Observable.just(o1, o2)
        .lift(new OperatorMergeSorted<Integer>(new Comparator<Integer>() {

          @Override
          public int compare(Integer o1, Integer o2) {
            return o2.compareTo(o1);
          }
          
        }));

    //The merged observable will emit integers sorted in descending order
    System.out.println(merged.toList().toBlocking().single());
    
    //output:
    //[10, 9, 8, 7, 6, 5, 4, 3, 2, 1]
        
```
