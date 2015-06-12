One refers to object on a memory heap with memory addresses. In a relational database, one uses primary keys. On the internet, it's URIs. With HyperGraphDB, one uses HyperGraphDB handles - instances of [HGHandle](http://www.kobrix.com/javadocs/hgcore/org/hypergraphdb/HGHandle.html). When you add an atom with the `HyperGraph.add` method, you get back a `HGHandle`. When you query for atoms, you get back a set of handles. HyperGraphDB links also point to atoms by using handles. In other words, handles are to HyperGraphDB what object references are to Java or pointers to C/C++.

If you have a `HGHandle`, you can get the actual atom by calling `HyperGraph.get`. For example:

```
HGHandle handle = // some means to obtain a handle to an atom
Book book = (Book)graph.get(handle);
```

In general, when working with objects that reside in HyperGraphDB, it is preferable to use handles and retrieve the actual object on a need-by-need basis. This recommendation is based on:

  1. The way caching of objects works in HyperGraphDB. The system will cache all currently loaded atoms and maintain a map b/w `HGHandle`s and Java object references. When a Java object is garbage collected, the cache clears its maps from it after a while. But the handle remains valid and the object will be automatically re-loaded upon the next `HyperGraph.get(handle)` request.
  1. The fact that links in the graph are based on handles, not plain Java references. The `HyperGraph.get` method is pretty fast (most of the time it doesn't even involve a hash lookup), so it's ok to use `HGHandle` as your way of referring to objects stored in HyperGraphDB instead of plain Java references.

You will notice that [HGHandle](http://www.kobrix.com/javadocs/hgcore/org/hypergraphdb/HGHandle.html) is actually just a marker interface. There are several implementation of it that may change in the future, so you shouldn't be relying on any concrete implementation. The only time you may be interested in a variety of a HyperGraphDB handle is if you want to persist the handle somewhere else. In those cases, you can get a persistent version of it by calling `HyperGraph.getPersistentHandle(handle)`.  Persistent handles are represented by the [HGPersistentHandle](http://www.kobrix.com/javadocs/hgcore/org/hypergraphdb/HGPersistentHandle.html) interface. They can be converted to byte buffers and then read back with
[HGHandleFactory.makeHandle](http://www.kobrix.com/javadocs/hgcore/org/hypergraphdb/HGHandleFactory.html#makeHandle()).

## From Objects to Handles ##

If you have a Java object reference that is also a HyperGraphDB atom, you can obtain its handle through the `HyperGraph.getHandle(object)` method. Thus despite the advice given above, you're not required to refer to hypergraph atoms only through their handles. It would be cumbersome to have to obtain the object with calls to `HyperGraph.get` every time you need it.

However, you must be careful that the object whose handle you're trying to obtain is actually in the HyperGraphDB cache. It will be there either if it was loaded from HyperGraphDB through a previous `get` or if it was just recently added by a call to `add`.

In general, you would use the Java object reference whenever you have to work with the actual data value and/or you want to isolate an application layer from the HyperGraphDB API. And you would use the `HGHandle` interface when you are mindful of memory consumption and/or you are working mainly with the graph structure of your data, rather than individual atoms.

[<< Prev - Storing Data](IntroStoreData.md)  [Next - Atom Types >>](IntroHGTypes.md)