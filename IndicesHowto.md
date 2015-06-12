HyperGraphDB allows you to index atoms by their attributes. Internally, various indices are maintained around the basic organizational layout of the hypergraph data. For example, because every atoms X has an associated incidence set holding all links pointing to it, the set of those links is readily available and can be efficiently intersected with other incidence sets. But to quickly retrieve a set of atoms based on their values, one needs to explicitly create an index.

At the lowest level, indices are just key-value tables that the storage layer manages. There are also bi-directional indices where a set of keys matching a given value can be retrieved. Some type implementations work directly with the storage layer to maintain internal indices normally hidden from the user. Such internal indices are of no concern to us here. Suffice it to mention that given a unique name, you can create an index using the [HGStore](http://www.kobrix.com/javadocs/hgcore/org/hypergraphdb/HGStore.html) API and then put whatever you want in it as long as you can translated your data to/from byte buffers.

Indexing at the level of atoms is supported by an [HGIndexManager](http://www.kobrix.com/javadocs/hgcore/org/hypergraphdb/HGIndexManager.html) that is associated with every `HyperGraph` instance. Every time an atom is added, removed or replaced, the `HyperGraph` will trigger an event with its `HGIndexManager` to update all relevant indices.

Indices themselves are created by registering _indexers_, which are implementations of the [HGIndexer](http://www.kobrix.com/javadocs/hgcore/org/hypergraphdb/indexing/HGIndexer.html) class, with the index manager. An `HGIndexer` is essentially responsible for creating a key given an atom. It is always associated with a specific atom type. So indices are always type-based. Moreover, sub-types are automatically indexed when an index is registered for a super-type.

In practice, the two most frequently used `HGIndexer` implementations are [ByPartIndexer](http://www.kobrix.com/javadocs/hgcore/org/hypergraphdb/indexing/ByPartIndexer.html) and [ByTargetIndexer](http://www.kobrix.com/javadocs/hgcore/org/hypergraphdb/indexing/ByTargetIndexer.html). The `ByPartIndexer` lets you create an index based on some atom property. For example if you have a `SiteUser` Java bean, with a bean property called `email`, you can index all users by their email like this:

```
HGHandle siteUserType = graph.getTypeSystem().getTypeHandle(SiteUser.class);
graph.getIndexManager().register(new ByPartIndexer(siteUserType, "email");
```

Now, when you query for site users by email (e.g. `hg.and(hg.type(SiteUser.class), hg.eq("email", "bill@microsoft.com"))`), the index will be used.

The `ByTargetIndexer` lets you index links by targets at specific positions. Take the predefined `HGSubsumes` link as an example which links something _general_ (target at position 0) to something _specific_ (target at position 1). You can index all `HGSubsumes` atoms by their second target like this:

```
graph.getIndexManager().register(new ByTargetIndexer(graph.getTypeSystem().getTypeHandle(HGSubsumes.class), 1));
```

Note that indexing by link targets is only useful when doing queries on ordered links. Otherwise, for unordered links the implicit indexing by incidence sets suffices. To take advantage of the index above, you would write a query like this:

```
List<HGHandle> L = hg.findAll(hg.and(hg.type(HGSubsumes.class), hg.orderedLink(hg.anyHandle(), someHandle)));
```

Note the use of `hg.anyHandle()` at position 0 of the ordered link condition. It is important to be explicit about the exact form of an ordered link in your query. Otherwise, the query system will not be able to associate the provided value (`someHandle` in the example above) with an available index.

When such indexers are registered with the system, an automatic indexing process is triggered the next time the database is opened. If you want to force the indexing to happen right now, call the following API:

```
graph.runMaintenance();
```

If you have existing atoms of the type specified in the indexer, they will all be added to the index and this can take some time. Indexer can also be removed by calling `HGIndexManager.unregister`. Remove an indexer doesn't take much time.

Note that `HGIndexer` instances are stored as HGDB atoms. For instance, one can list all by-value-part indices with the following query:

```
List<ByPartIndexer> byPartIndexers = hg.getAll(graph, hg.type(ByPartIndexer.class));
```

That said, removing an `HGIndexer` atom without going through the `HGIndexerManager.unregister` method would be a bad idea because the underlying storage won't be cleaned up.

[<< Prev - Graph Traversals](IntroGraphTraversals.md)  [Next - Transaction Essentials >>](IntroTransactions.md)