# Strict Fibonacci Heaps

I implemented this project for the Selected Topics in Algorithms course while attending the University of Padova.

Kotlin implementation of Strict Fibonacci Heaps, introduced by Brodal et al. in 2012 as a new data structure of the heap family with the same complexity bounds as Fibonacci Heaps. The theoretical advantage of this data structure was the elimination of the amortized complexity of the Fibonacci Heaps during the delete-min operation. This allows the data structure to perform consistently during its usage, with no random spikes in execution times.

This implementation differs from the published work as I decided to relocate all the intricate behavior of the transformation methods into the link method. In this way, the adjustments to the data structures are centralized in a single method, and are triggered automatically as the API methods are invoked.

As with other alternative Heap implementations, performance resulted lackluster compared to standard binary heaps due to the high hidden constant cost in the theoretical complexities.
