### A quick note about the first draft on counting result sets ###

Counting result set is implemented by the `HGQuery.hg.count()` function. The supporting class at this time of writing is sort of a first, experimental version that is not very much tied to the rest of query processing where the optimization heuristics still don't use size estimate. It is called `ResultSizeEstimation` and it basically mimics the `ExpressionBasedQuery` implementation where each condition type is mapped to an implementation of a `Counter` interface that looks like this:

```
public interface Counter
{
    long count(HyperGraph graph, HGQueryCondition cond);
    long cost(HyperGraph graph, HGQueryCondition cond);
}
```

The cost function produces a cost associated with calculating the actual count. The cost is the number of low-level DB operations needed to calculate the count. When the count is readily available from an index for example, the cost is 0. When a search by a key in some BerkeleyDB must be performed, the cost is 1 etc...

`Counter` implementation are similar to `ExpressionBasedQuery.ConditionToQuery` implementation. There's one for each type of condition. The `And` can only be counted when there's a single subcondition. The `Or` condition is counted by counting all its subconditions.

When a result set count is not readily available, the `HGQuery.hg.count` method (and the `Counter` implementations as well) will perform the actual query and scan the result set. There's no public API for estimating the cost of counting. A programmer should rely on the fact that whenever a condition is more complicated than examining an index (or a union of a few indices from an Or condition) counting cannot be done without running the full query. However, more complicated condition that can be reduced to an index such as `and(type(t), eq("someattribute", somevalue))` where the type 't' is indexed by "someattribute" are counted in constant time still.