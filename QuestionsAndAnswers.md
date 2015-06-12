### How can I increase HyperGraphDB's performance ###

There are a few configuration options that can help increase the database performance significantly. The first one is giving the storage system a bigger cache. This must be done before a database is opened and by working directly with the BerkeleyDB API (or whatever other storage sub-system is being used). For example, with the BerkeleyDB storage one may do:

```

HGConfiguration config = new HGConfiguration();
BDBConfig bdbConfig = config.getStoreImplementation().getConfiguration();
// Change the storage cache from the 20MB default to 500MB
bdbConfig.getEnvironmentConfig().setCacheSize(500*1024*1024);

```

Using the BerkeleyDB configuration object you can set other options such as whether to use MVCC in storage transactions, what type of key-value store (hash or b-tree) to use and others. In our experience none of those really affect performance. A hash-based storage gives a theoretical constant amortized access time, but in practice it doesn't seem faster than a b-tree.

Another configuration change that you may do and that results in a significant speed up of database operations is using a different-than-the-default ID generation schema. The default schema produces random UUIDs which means that data is distributed randomly in the key-value store, which makes the cache relatively ineffective. Since in general atoms that are created together tend to be accessed together, having a more conventional ID generation schema where IDs (i.e. HGPersistentHandles) are generated in a numerical sequence will result in much fewer cache misses. To use a different ID generation, you have to configure a different HGHandleFactory, e.g.:

```
// Generate UUID persistent handles sequentially. A UUID is made up of
two 64bit longs.
// The following constructs a handle factory that will use the startup
time as one of the two longs
// and will be counting from 0.
SequentialUUIDHandleFactory handleFactory =
                           new SequentialUUIDHandleFactory(System.currentTimeMillis(), 0);
HGConfiguration config = new HGConfiguration();
config.setHandleFactory(handleFactory);
```

You could implement your own handle factory as well. It's possible to use integers or longs as identifiers which will speed things up even further (comparisons are faster, data is smaller etc.) and there are handle factories for that too. However, if you do so you must also change the predefined type configuration file which contains preset handles for all types necessary to bootstrap the HyperGraphDB type system.

Yet another very simple change is disabling the use of "system flags". System flags are extra bits associated an atom and stored in a separate key-value store. They are kind of experimental and rarely (if ever) used, but remain on by default. This means every read and every write operation of an atom access an additional key-value store that practically has not much use. You can disable that by calling:

```
HGConfiguration config = new HGConfiguration();
config.setUseSystemAtomAttributes(false);
```

### GUI for HyperGraphDB? ###

There is no special purpose administrative GUI in the traditional sense. However, the scripting environment [Seco](http://www.kobrix.com/seco.jsp) has been built entirely on top of HyperGraphDB. You can use to interact with a database instance via scripting or by creating your own admin components in the Seco canvas and/or leveraging such components that others have created.

### Can I access HyperGraphDB from multiple threads? ###

Yes and in most cases you don't have to worry about it. If you are just using the basic API operations, such as get/add/remove, or doing queries via the [HGQuery.hg](http://www.kobrix.com/javadocs/hgcore/org/hypergraphdb/HGQuery.hg.html) API, you are fine. This is because those operation are already run within a transaction.

However if you are iterating over a [HGSearchResult](http://www.kobrix.com/javadocs/hgcore/org/hypergraphdb/HGSearchResult.html), you need to encapsulate it into a transaction by using the [HGTransactionManager](http://www.kobrix.com/javadocs/hgcore/org/hypergraphdb/transaction/HGTransactionManager.html) available with `HyperGraph.getTransactionManager()` of your HyperGraphDB instance..

### Creating a graph based on existing API ###

First of all, you need to decide which of your classes represent relationships and which represent basic data. Instances of classes that represent basic data can be added into a HyperGraphDB instance without modification at all. They will simply be atoms of arity 0,  meaning that they don't point to other atoms. Classes that represent relationships need to be transformed into HyperGraphDB links, i.e. implementations of the [HGLink](http://www.kobrix.com/javadocs/hgcore/org/hypergraphdb/HGLink.html) interface.

You can avoid implementing the HGLink interface if you find this is intrusive to your API, or if you are working with a 3d party API that you can't change, by using the [HGValueLink](http://www.kobrix.com/javadocs/hgcore/org/hypergraphdb/HGValueLink.html) wrapper class. However, you won't be able to access the target set of your link from your Java object instance and you will have to rely on the HGValueLink API to do so.

### Can I make any object into a link? ###

You can turn any object into a hypergraph link by wrapping it up as a [HGValueLink](http://www.kobrix.com/javadocs/hgcore/org/hypergraphdb/HGValueLink.html). This way you don't need to modify the Java class of the object. The object will be stored as the "payload" value of the link and it will still represent a relationship in the graph.

### How can I add an object only if it doesn't exist? ###

An object (i.e. a hypergraph atom) is uniquely identified only by its HGDB handle. Unlike in convetional RDBMS systems, there is no requirement to specify a primary key for an atom type. However it is common that certain properties of an atom identify it uniquely. At a minimum, its value and target set make it unique. Therefore, you would add a uniquely identified object by first doing a lookup by the properties that define it uniquely and then insert into the database only if no results are returned by that lookup. This is a common pattern and consequently there's an API for it:

```
import org.hypergraphdb.HGQuery.hg;

Webpage x = new Webpage(...); // the object to be insert if not existing
HGHandle xHandle = hg.addUnique(a, hg.eq("url", a.getUrl());
```

The `hg.addUnique` method takes an atom to be added and a condition to query the database with. If the condition yields some results, the handle of the first one is returned. Otherwise the atom is added to the graph and its new handle is returned.

### When should I use transactions? ###

All primitive operations offered by the HyperGraphDB API are automatically encapsulated in a transaction. In addition, HyperGraphDB supports nested transactions where child transactions can fail and be handled gracefully while a parent transaction still succeeds. There are three common cases where you may want to start and end a transaction yourself via the `HyperGraph.getTransactionManager()`:

  1. You are iterating over a query result set yourself, instead of fetching all data in a Java collection.
  1. You have several operations that you want to treat as a unit - this is the most common case. For example, you need to insert two atoms and a relationship between them, and neither the atoms nor the relationship make sense individually in your data model. Wrap the three calls to `HyperGraph.add` in a transaction.
  1. You have a data intensive portion of your application that adds hundreds of atoms in a loop. In this case you want to wrap those additions in a transaction for performance reasons. Here is why: when a primitive operation is performed, HyperGraphDB will create a transaction only if there is none currently in effect; otherwise, HyperGraphDB will reuse the current transaction. If you do, say, a hundred atom additions in a loop without creating a transaction, HyperGraphDB will create and commit a hundred separate transactions. This is much more expensive than creating and committing a single transaction for all hundred addition. The flip side is, of course, that either all or none of your atoms will be added. The optimal number of such primitive operations to be bundled in a single transaction varies from system to system and from the complexity of the atom values themselves. Our own (not very rigorous) experiments reveal that a value somewhere b/w 20 and 100 is best.

### How long may an HyperGraph instance stay open / how aggressively should it be closed? ###
If hypergraphdb is killed before the clean shutdown process has succeeded, there might be data loss. In production systems, you are recommended to ensure graph.close by wrapping code in a try block, graph.close() in a finally block.
Since in most situations there is no real danger for data loss, and since opening and closing are associated with a cost, you can usually leave the database open until the application finished (see [here](https://groups.google.com/d/topic/hypergraphdb/GVo4rGGFQfs/discussion)).