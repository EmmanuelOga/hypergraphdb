# Introduction #

This is experimental code so subject to frequent changes.

# Recognizing a custom indexer #

When you implement a HGIndexer, you are presumably doing it to speed up some query processing. However, the query compiler doesn't know about your indexer or how it works. One way is to directly use the index in a query expression:

```
HGQuery<HGHandle> q = HGQuery
  .make(HGHandle.class, graph).
  .compile(hg.and(hg.type(someType), new IndexCondition(yourindex, lookupKey)));
for (HGHandle h : q.findAll())
{
  // etc...
} 
```

And this is a perfectly valid way to make use of the index. But clearly an index is not the right abstraction to use within a query expression. The `IndexCondition` is kind of a backdoor telling the query system "I want you to use that index right here". It would be more appropriate to use a condition on the form of the data and make use of an index if available.

Since there is no way for the system to know what exactly a custom indexer is doing, you have to instead provide means to recognize when such an indexer can be used. Given the `IndexCondition` class, all that is needed is means to transform some condition expression into an `IndexCondition`. Such a transformer is a `Mapping<HGQueryCondition, HGQueryCondition>`. You can implement a transformer like this and add it to a list that is going to be applied by the query system before it does initiates the query compilation process:

```
  HGConfiguration config = new HGConfiguration();
  config.getQueryConfiguration().addTransform(new ApplyMyIndex());
```

The `ApplyMyIndex` would implement the algorithm that given a query expression, with logical operators etc. nested at any level, finds if a query sub-expression can be replaced with usage of the custom index. For example here is how a simple transformer for a bean property index could be implemented:

```
public class ApplyMyIndex implements<HGQueryCondition,HGQueryCondition>
{
    public HGQueryCondition eval(HGQueryCondition in)
    {
       // 
       if (in instanceof And)
       {
           AtomTypeCondition typeCondition = null;
           AtomPartCondition partCondition = null;
           And and = (And)in;
           for (HGQueryCondition cond : and)
           {
               if (cond instanceof AtomTypeCondition)
                   typeCondition = (AtomTypeCondition)cond;
               else if (cond instanceof  AtomPartCondition)
                   partCondition = (AtomPartCondition)cond;
           }
           And out = new And();
           if (typeCondition != null && partCondition != null)
           {
               HGIndex idx = graph.getIndexManager().getIndex(new ByPartIndexer(typeCondition.getTypeHandle(),
                                 partCondition.getDimensionPath());
               if (idx != null)
               {
                   out.remove(typeCondition);
                   out.remove(partCondition);
                   out.add(new IndexCondition(idx, partCondition.getValue());
               }
           }
           return out;
       }
       else return in;  
    }
}
```

The code above hasn't been test or even compiled. The idea is that the query expression is traversed (a real implementation should actually go in recursively), and certain conditions or combinations thereof would be replaced by an index lookup.