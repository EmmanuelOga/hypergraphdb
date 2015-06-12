The AtomCache, in the current implementation, is  a simple class maintaining several maps holding the live data related to an atom.  HyperGraph and HGTypeSytem both use the cache to store live information. The maps comprising the atom cache are the following:

•	liveHandles – maps HGPersistentHandles to LiveHandles
•	atoms – maps Java Object instances to LiveHandles
•	incidenceSets – maps HGPersistentHandles to incidence sets.

Incidence set are not fetched upon atom retrieval, but only when they are themselves requested.

The very first improvement that should be done here is the implementation of an eviction policy of the cache. Right, it will simple grow until memory is exausted. Many strategies could be used, but a combination of MRU (most recently used) and MFU (most frequently used) strategy should suffice to define atom importance. This, together with a pre-fixed cache size should keep things in control.