  * Migration to Berkeley DB 11g Release 2
  * Made HGRelTypeConstructor into a HGComplexType so that relations can be found by name more easily.
  * Added the HGTransactionContext to the HGTransaction instances. Added the HyperGraph instance to its associated HGTransactionContext.
  * Added a few more events for finer grained control of atom addition and replacement. Listeners can throw strongly-typed exceptions to abort the transaction.
  * Added collection and List based constructors for LinkCondition and OrderedLinkCondition respectively.
  * Added HGQuery.hg.atomAssert API for "insert ignore" semantics.
  * XMPP interface allows communicating with peers from a chat room only instead of having to manage every peer's roaster separately.
  * Added toString method to HGValueLink for tracing/debugging purposes.
  * Fixed a bug when removing a link with an empty target set.
  * Fixed a bug in SimpleALGenerator and DefaultALGenerator returning incorrect link when it changes.
  * Added HGUtils.dropHyperGraphInstance method to delete a HGDB instance from the filesystem
  * Implemented MVCC (snapshot isolation) transactions, which are now default.
  * Added size() method to HGCache interface and implementations.
  * EnumType supports indexing (i.e. enum properties can be indexed by)
  * Abstracted storage implementation into HGStoreImplementation interface
  * Abstracted persistent handle management - HGHandleFactory is now an interface rather than a class.
  * Added some more configuration options to HGConfiguration:
    * max size of in-memory incidence sets - maxCachedIncidenceSetSize
    * option to ignore system attributes (improves performance when set to false which is not the default!)
    * New HGTypeConfiguration class to customize type system bootstrapping.

  * Removed storage cache configuration option from HGConfiguration - this is not separately available from BDBStorageImplementation.getConfig() which returns BDBConfig.
  * Added 'goBeforeFirst' and 'goAfterLast' methods to HGRandomAccessResult interface.\
  * HGALGenerator now returns a Pair<HGHandle, HGHandle> result set holding both the link to the adjacent atom and the atom itself, while the getCurrentLink method was removed.
  * Added HGHandleHolder, similar to HGGraphHolder - atoms implementating this interface will have their own handle managed as a bean property by a HyperGraph instance (set during add/load/replace).
  * Added HGTypeHolder interface that behaves similarly  HGGraphHolder and HGHandleHolder - atoms implementing this interface will have their own type handle stored as a bean property (set during add/load/replace).
  * Added Date related primitive types for storing java.util.Date, java.sql.Date, java.sql.Timestamp and java.util.Calendar - value are stored as longs in GMT according to the local time zone.
  * To construct HGLink Java beans, it is now possible to use constructors of the form A(HGHandle x, HGHandle y, ..., HGHandle z) in addition to the A(HGHandle [.md](.md) targetSet) form. This makes it easier to name the actual link target arguments and improves code readability and Javadocs.
  * Added support for private default and link based constructors.
  * Fixed a bug with sub-type indices not being used.
  * Fixed a bug with OrderedLinkCondition constructed from a List of HGHandles.
  * Completely removed dependency on JXTA for P2P implementation - this is unmaintained, confusing new users and just taking space in the distribution,
  * Other bug fixes and improvements that weren't formally tracked or reported...

### API Changes ###

A couple of minor changes to the public API were made in this release. Those changes **do not** remove functionality and are peripheral enough that they should impact only a few lines of code (if any) on projects using HyperGraphDB. Here is the list of them together with an explanation of the rationale behind the change and what you should do if you are impacted:

  * The `HGHandleFactory` class is now an interface. It used to be a class with static methods dealing with persistent handles. The choice of static methods assumed that a JVM instance would only need one kind of persistent handles generated. This may prevent several co-existent HyperGraph object to have different handle schemas, e.g. a RAM only instance with int handles and a UUID-based HyperGraphDB peer that's part of a distributed network . So the `HGHandleFactory` is now an object associated with the HyperGraph instance of which it manages handles. This means all persistent handle construction (new handles, from string, from byte[.md](.md)) are to be done by the object returned from HyperGraph.getHandleFactory(). The handle factory itself is part of the `HGConfiguration` bean. The default remains `UUIDHandleFactory` constructing random UUIDs. If you need to define persistent handles statically and if you've done so already, and you are comfortable with a commitment to UUID-based handle, you can make call to the `UUIDHandleFactory.I` instance in static contexts.
  * The storage caching related attributes where removed from the `HGConfiguration` bean. This is because we'd like to implement different back-ends in the future (than BerkeleyDB) and not all will have a configurable cache. Instead:
    1. The `HGStore` now relies on an implementation of HGStoreImplementation.
    1. Each `HGStoreImplementation` offers a configuration object through its `getConfiguration` method.
    1. Thus, one can configure the storage implementation before instantiating a HyperGraph instance via HGConfiguration.getStoreImplementation().getConfiguration(). What this means is that you might need to gain some familiarity with the particulars of the storage back-end being used. In particular, for BerkeleyDB you'd have to do `getEnvironmentConfig().setCacheSize(...)` on the store implementation configuration object.

  * The `HGIndexer` class was refactored into an interface with direct `index` and `unindex` methods. This allows for truly arbitrary indexing, including many-to-many models that weren't possible with the previous API (each atom could only yield a single key). The previous `HGIndexer` class is now called `HGKeyIndexer` and it implements the new `HGIndexer` interface. This change won't impact any usage of the predefined indexers. However, if you have implemented a custom indexer, you need to change it to extent `HGKeyIndexer` instead of `HGIndexer` and everything should work as before.
  * The `HGALGenerator`  now returns a Pair<HGHandle, HGHandle> result set holding both the link to the adjacent atom and the atom itself, while the getCurrentLink method was removed. This way, a single generator instance can be reused multiple time, recursively or within a loop because it doesn't maintain state anymore.
  * Range queries from `HGSortedIndex` no longer return `HGRandomAccessResult`, but just a `HGSearchResult`. This was essentially a bug in the API: random access can't be implemented efficiently in general in this case. That said, concrete implementation may still return random access results whenever possible.