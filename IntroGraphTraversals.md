One of the main advantages of HyperGraphDB as a database is its ability to store and manage very large graphs of relationships. It is mixing this and regular relational and object-oriented style databases that makes HyperGraphDB a powerful tool for information management and knowledge representation. So let's examine the basic APIs for walking around a HyperGraph and the algorithms provided out of the box. All interfaces and classes talked about in this section are in the [org.hypergraphdb.algorithms package](http://www.hypergraphdb.org/docs/javadoc/org/hypergraphdb/algorithms/package-summary.html).

At the foundation of all graph related algorithms lies the basic operation of walking from node to node, or in HyperGraphDB lingo form atom to atom. This is represented by the [HGTraversal](http://www.hypergraphdb.org/docs/javadoc/org/hypergraphdb/algorithms/HGTraversal.html) interface. Traversing the graph is always done in some particular order, depending on the concrete traversing algorithm. A traversing algorithm simply produces atoms in a sequence and the `HGTraversal` interface is a specialization of the standard `java.util.Iterator` interface. The two standard graph traversal algorithms are implemented by the `HGBreadthFirstTraversal` and `HGDepthFirstTraversal` respectively.

Now, in HyperGraphDB all links are typed and have an arbitrary object attached to them. More importantly, each atom can conceptually participate in several independent structures in a graph. Therefore it is not immediately obvious which links should a traversal follow when walking from atom to atom. An essential component of the traversing  algorithms in HyperGraphDB is that very decision: which of the adjacent atoms should be next visited. That decision is delegated to the user of a traversal algorithm - you :)

The adjacent atoms that must be visited are calculated by implementations of the [HGALGenerator interface](http://www.hypergraphdb.org/docs/javadoc/org/hypergraphdb/algorithms/HGALGenerator.html). Both the breadth-first and depth-first traversal implementations take an `HGALGenerator` instance as a constructor argument. The simplest generator is the [SimpleALGenerator](http://www.hypergraphdb.org/docs/javadoc/org/hypergraphdb/algorithms/SimpleALGenerator.html) implementation. This implementation will list all adjacent atoms, disregarding types and values of atoms:

```
HGHandle myBook = ...// get the handle of a book of interest.

HGDepthFirstTraversal traversal = 
    new HGDepthFirstTraversal(myBook, new SimpleALGenerator(graph));

while (traversal.hasNext())
{
    Pair<HGHandle, HGHandle> current = traversal.next();
    HGLink l = (HGLink)graph.get(current.getFirst());
    Object atom = graph.get(current.getSecond());
    System.out.println("Visiting atom " + atom + 
                       " pointed to by " + l);
}
```

Notice that the next method of the `HGTraversal` interface returns a pair of objects. The first element of this pair is the link that led to the current atom while the second is the atom itself.

A more interesting `HGALGenerator` is the [DefaultALGenerator interface](http://www.hypergraphdb.org/docs/javadoc/org/hypergraphdb/algorithms/DefaultALGenerator.html). The `DefaultALGenerator` implementation allows control over most aspects of graph link-atom selection that one would encounter in practice. The full constructor signature looks like this:

```
DefaultALGenerator(HyperGraph graph, 
                   HGAtomPredicate linkPredicate, 
                   HGAtomPredicate siblingPredicate, 
                   boolean returnPreceeding, 
                   boolean returnSucceeding, 
                   boolean reverseOrder)
```

As you can see, the `DefaultALGenerator` can be configured with a predicate that constraints the links to be selected during traversal and a predicate that filters the atoms to be traversed. In addition, one can also specify the order in which atoms linked by a given link are visited. In classical graphs where all links are of arity 2, the order amounts to specifying directionality of the links of interest. In hypergraph, where links can potentially tie together tens of atoms, the order may be important. It all depends on the representation of your domain. Suppose that the current atom during a traversal is `x` and that x is the target of some link that looks like this:

```
  L = [a, b, c, x, d, e]
```

The AL generator will first check whether the link `L` satisfies the `linkPredicate`. Then if `returnPreceeding` is true it will examine each of the atoms a, b and c as potential siblings and it will returns all those that satisfy the `siblingPredicate`. Similarly, if `returnSucceeding` is true, it will examine and maybe return the atoms d and e. When `reverseOrder` is true, the AL generator considers the link L as if it had the following form:

```
  L = [e, d, x, c, b, a]
```

This reversed order affects which siblings are first returned. As an example, imagine a graph where you have all sorts of publications (books, articles, blogs) as nodes and you have links of type CitedBy(X, Y) which means that publication X contains a quote from publication Y. To traverse all citations of articles published in the _Science_ magazine, you could perform the following:

```
   DefaultALGenerator algen = new DefaultALGenerator(graph, 
                                                     hg.type(CitedBy.class),
                                                     hg.and(hg.type(ScientificArticle.class),
                                                            hg.eq("publication", "Science")),
                                                     true,
                                                     false,
                                                     false);
   HGTraversal traversal = new HGBreadthFirstTraversal(startingArticle, algen);
   ScientificArticle currentArticle = startingArticle;
   while (traversal.hasNext())
   {
       Pair<HGHandle, HGHandle> next = traversal.next();
       ScientificArticle nextArticle = graph.get(next.getSecond());
       System.out.println("Article " + current + " quotes " + nextArticle);
       currentArticle = nextArticle;
   }
```

[<< Prev - Querying](IntroQuerying.md)  [Next - Indexing >>](IndicesHowto.md)