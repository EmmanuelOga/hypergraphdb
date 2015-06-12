The HGStore class implements the basic storage operations for the hypergraph structure. At the level of HGStore, there is a plain, untyped hypergraph structure with nodes containing raw (byte array) data and links pointing to other nodes/links. Typing and application-level HyperGraph atoms are overlaid on top of that basic structure.

Each record in the hypergraph store is a key-value pair. The key is always a HGPersistentHandle (in the current implementation always an UUIDPersistentHandle). The store distinguishes between two kinds of values: raw data and links. From a HGStore perspective, links are simply arrays of HGPersistentHandles. Thus the value of link record is always a multiple of sizeof(UUID) bytes and parsed very efficiently.

As explained above, the HGStore defines a layout structure upon which the higher-level HyperGraph structure is based. The layout maps directly to the underlying Berkeley DB storage mechanism and serves as an insulation layer between the latter and the HyperGraph itself. Thus, a different physical storage implementation can be chosen in the future without much impact. The rest of this section documents the layout mapping of HyperGraph, typed atoms to HGStore key-value pairs. In what follows, all handles are UUIDPersitentHandles of size 16 bytes.
Primitive Values
The format of primitive values is simply:

Handle  byte array

The predefined primitive type of the atom with that value interprets the byte array to construct a run-time object.
Non-Primitive Values
Non-primitive values depend on the interpretation of their type very much like primitive values. Generally, the type implementation will define a layout structure with handles possibly pointing to other records in the HGStore. The predefined compound types that come with HyperGraph do so, as documented below.
Nodes
Nodes (HyperGraph atom with an empty outgoing set) have the following layout format:

Handle  [TypeHandle, ValueHandle]

The TypeHandle points to the type of the node’s value. The ValueHandle points to the type instance.
Links
Links are stored thus:

Handle  [TypeHandle, ValueHandle, TargetHandle1, TargetHandle2, … ,
> TargetHandleN]

The TypeHandle points to the type of the link’s value. The ValueHandle points to the type instance. Follow the atom handle of all atoms in the outgoing set of this links. This structure obviously yields an implicit order of a link’s targets so that HyperGraph links will always be ordered as an implementation artifact.
Type Layout
Primitive HyperGraph types and predefined type constructors are configured through separate configuration files (with pre-generated UUID persistent handles). All other types are recorded in the same underlying store by using the relative free form layout structure of HGStore. Each non-predefined type is simply the value of a meta-type (or a type constructor). Thus, recording new types in the HGStore is done the same way as recording compound values.

The predefined compound types that come with HyperGraph have the following HGStore layout:

ArrayType:

Array store a set of elements all of the same type. The resulting run-time object is an Object[.md](.md).

ValueHandle  [ElementType, HandleValue1, HandleValue2, … ,
> HandleValueN]

SlotType:

Slot types manage run-time objects of type org.hypergraphdb.type.Slot. Slots are used as aggregate elements of records. Slots are global for the HyperGraph. They are defined by a label, of any type and a value type.

SlotHandle  [LabelTypeHandle, LabelValueHandle, ValueTypeHandle]

RecordTypeConstructor:

A RecordTypeConstructor manages RecordType instances. Each record type is simply a set of slots:

RecordTypeValue [SlotHandle1, SlotHandle2, …, SlotHandleN](.md)

RecordType:

A record type manages run-time objects of type org.hypergraphdb.type.Record. Each record is map between slots and values. A org.hypergraphdb.type.LinkRecord represents a record that is also a HGLink.

RecordValueHandle  [ SlotValueHandle1, SlotValueHandl2, …,
> SlotValueHandleN]