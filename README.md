# RxJavaRecipes

A small repo with reusable custom operators and utilities on top of RxJava.

### Include as a depenency to your project



### OperatorMergeSorted

Merges source Observables into one Observable, in the order specified by the provided comparator (assuming the source Observable's have their items pre-sorted in the order consistent with the comparator). 

#### Usage
