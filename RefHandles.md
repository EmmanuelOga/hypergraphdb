Handles are HyperGraph’s notion of a reference to an atom. Within the HyperGraph API, each atom if referred to by its handle. A handle is represented by the marker HGHandle interface. To obtain an atom instance, the application needs its handle. To construct a link between atoms, all of their handles are needed.

Handles are of two types: HGLiveHandle and HGPersistentHandle. Live handles refer to atoms loaded in main memory. Persistent handles refer to atoms in permanent storage. HGPersistentHandle is an abstract class and allows for potentially different implementations of persistent handles as long as they are able to convert themselves to byte arrays. HyperGraph comes with a predefined UUIDPersistentHandle implementation based on universally unique identifiers.

Because the relationships between the different types of handles and atom instances are so basic and likely to seriously impact overall performance and API usability, we will describe them a little bit more detail, justifying our design choice.

All atoms loaded in main memory are maintained in a HyperGraph’s atom cache. Because an application generally refers to an atom by its handle, it is insulated from a concrete atom’s location. HyperGraph manages the loading/unloading of atoms from the cache entirely at its discretion. An eviction policy from the cache has not been implemented so far. A pluggable policy mechanism will be defined at some point.

Regarding the mappings of atoms to/from live handles to/from persistent handles, we have the requirement that they should all be constant time and fast in both directions. There are two basic strategies to achieve this:

1.	Make the atom reference in the host language (Java only, so far) and its handle be one and the same thing, thus mandating that all atoms implement the HGHandle interface. Or,
2.	Create a separate LiveHandle that holds the run-time reference to the atom.

In the first, we have the following access time characteristics:

LiveHandle  Atom : a typecast
Atom  LiveHandle: a typecast
LiveHandle  PersistentHandle: a hash lookup
PersistentHandle  LiveHandle: a hash lookup

In the second, we have a LiveHandle object holding references to both the atom and its persistent handle:

Atom  LiveHandle: a hash lookup
LiveHandle  Atom: an inlined function call
LiveHandle  PersistentHandle: an inlined function call
PersistentHandle  LiveHandle: a hash lookup

In both cases, we have the memory overhead of two run-time hash tables and similar access times. We choose the second implementation because it is less intrusive to the application’s APIs and because the LiveHandle will be useful as a reified abstraction to assist in the run-time cache management (e.g. by recording access statistics). There’s a slight per atom memory overhead caused by the LiveHandle objects, but I believe it’s tolerable.