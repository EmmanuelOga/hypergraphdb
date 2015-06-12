  * Fixed HGEnvironment.isOpen.
  * Added global configuration option for the default "keep incident atoms" parameter of HyperGraph.remove.
  * Added predefined type for the immutable Pair class.
  * Fixed bug with AtomReference annotation not being found in super classes.
  * Added HGUtils.clone method for cloning arbitrary objects.
  * Some cleanup and documentation of the P2P framework and APIs.
  * Fixed bug with buffer reuse in scanning results sets.
  * Added getPersistent() to HGHandle interface. A reference to the HyperGraph instance is no longer needed to obtain a persistent handle out of a generic handle. While more general and open for different implementations, it was rather annoying to have to call HyperGraph.getPersistentHandle(...) and to have to carry a HyperGraph reference around just for that.
  * Added a new HyperNode interface, abstracting the basic HyperGraph operations (CRUD on atoms and queries and incidence sets).
  * Added an implementation PeerHyperNode of the HyperNode interface that connects to a remote peer and performs all operations remotely.
  * Added an implementation of HGSubgraph of the HyperNode interface that lets associated a set of atoms together as a subgraph of the main hypergraph. The subgraph is itself an atom, which allows for a form of nesting. All atoms are still accessible from the global database handle space though. Two new conditions hg.contains and hg.memberOf allow lookup of atoms by a subgraph they belong to, or lookup of subgraphs containing a particular atom.
  * Got rid of the HGRemoveRequestEvent listener present by default to track dangling HGAtomRefs. This is now done directly in the HyperGraph.remove method. Now, there are no "system" listeners as overhead in any atom operation.
  * HGEventManager is now an interface who's concrete implementation can be configured in the HGConfiguration initialization object. The default implementation is called HGDefaultEventManager.
  * Removed extra check in cache for atoms when resolved through a live handle. This prompted an implementation of "abort actions" that can be attached to a HGTransaction to execute in case of abort. Also, live handles are invalid if a database instance is closed and re-opened.
  * Moved the ClassLoader configuration parameter from HGTypeSystem to HGConfiguration, where it makes more sense.
  * Fixed bug with null AtomRef projections.
  * Fixed bug with delete of self-referential structures.
  * Added regex string matching as a query predicate.
  * Added identity (hg.is) query condition.
  * Generalized Class->HGDB type mapping to URI->HGDB type with the new HGTypeSchema interface.
  * Converted HGQuery.hg API methods getAll, getOne, findAll, findOne to use read-only transactions. This will generated less conflicts, improve memory footprint in large queries and concurrent applications. Some care must be taken for query conditions not to have side-effects that modify the database.
  * Fixed several deadlock bugs
  * Fixed a bug not allowing replacement of Java bean-based types with their updated versions.
  * Fixed a bug not allowing refreshing a predefined, evicted from the cache, from the database.
  * New PositionedIncidentCondition (see hg.incidentAt, hg.incidentNotAt) to search for incident links pointing to a target at a specific range of positions.
  * Pre-compiled queries with variables can boost performance in many cases by a factor of 2. See http://kobrix.blogspot.com/2012/05/variables-in-hypergraphdb-queries.html
  * Fixed a bug with nested transactions not updating cached incidence sets.
  * Implementation of storage based on the BerkeleyDB Java Edition. This storage is now made the default in order to make it easy for newcomers to start with HyperGraphDB. Test so far show the JE performs much better for data sets up to about 2-3 million atoms. Above that, the native version seems better.
  * Integration with the Maven build system. HyperGraphDB artifacts are available from http://www.hypergraphdb.org/maven