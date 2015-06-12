## Introduction ##

This topic explains the details of writing a custom type for HyperGraphDB. Every atom in HyperGraphDB has one and only one type. This create a natural partition of the atom space according to type and usually a typing constraint is the first condition one specifies in a query. In a sense, types act as a natural categorization/labeling of atoms.

Furthermore, types define the semantics of storage of their values. Those semantics are abstracted into a general CRUD-like interface where there is one operation to write a value to storage, one operation to remove a value and one operation to construct the runtime representation of a value given a storage handle. Those are fundamental operations in many software settings. They are akin, for instance, to HTTP's POST, DELETE and GET respectively. But note that there is no _update_ operation like HTTP's PUT. The reason is that values in HyperGraphDB are immutable. On can attach a different value to an atom, but one cannot directly change a value. This means that once you obtain a value handle, you can be sure that this handle always points to the same value. As a consequence, value sharing between atoms and value caching are available to type implementation as possible optimizations.

Lastly, type implementations define a partial equality relation between their instances: the _subsumption_ relation. Subsumption means that some entity include another as a special case. In other words, A subsumes B if A is more general than B, or if B can be used whenever A can be used. We call this partial equality because if A and B subsume each other, they must be equal (though not identical of course). In type theory, a type generally has to define when two of its elements are equal. But in practice, often one wants to know whether something can be used (plugged in) in place of something else, as in a subclass of a class. So subsumption is in fact a more general concept than equality, it subsumes it so to speak and we've chosen that more general predicate as part of the core type interface in HyperGraphDB.

## The HGAtomType Interface ##
So, here is the [HGAtomType interface](http://www.kobrix.com/javadocs/hgcore/org/hypergraphdb/type/HGAtomType.html):

```
public interface HGAtomType extends HGGraphHolder
{
    Object make(HGPersistentHandle handle, LazyRef<HGHandle[]> targetSet, IncidenceSetRef incidenceSet);
    HGPersistentHandle store(Object instance);
    void release(HGPersistentHandle handle);
    boolean subsumes(Object general, Object specific);   
}
```

First, note that a `HGAtomType` is a [HGGraphHolder](http://www.kobrix.com/javadocs/hgcore/org/hypergraphdb/HGGraphHolder.html) which means that each type will hold a reference to the HyperGraphDB instance to which it belongs. The [HGAtomTypeBase](http://www.kobrix.com/javadocs/hgcore/org/hypergraphdb/type/HGAtomTypeBase.html) abstract class implements the `HGGraphHolder` interface with a protected variable `graph` pointing to the `HyperGraph` instance.

The `store`, `release` and `make` methods work with low-level storage to manage value persistence. The low-level storage is accessible via the `HyperGraph` instance held by the type. For example, storing a byte[.md](.md) could look like this:

```
public MyType implements HGAtomType
{
    private HyperGraph graph;
    
    public void setHyperGraph(HyperGraph graph) { this.graph = graph; }

    public HGPersistentHandle store(Object instance)
    {
        byte [] value = (byte[])instance;
        return graph.getStore().store(value);
    }

    // etc...
```

The `store` and `release` methods are complimentary. The `store` method is responsible for recording the given object instance to permanent storage within the current transaction (if any) and returning the identifier of that object value. Note that you normally don't need to worry about starting a new transaction here. Transactions are created either at the application or atom management level and the `HGStore` implementation is responsible for using the current thread-bound transaction. It is possible to initiate a new, nested transaction, but we can't think of a case where this would be warranted.

The `release` method is responsible for removing a value from storage. For example:

```
public MyType implements HGAtomType
{
    // ...
    public void release(HGPersistentHandle handle)
    {
        graph.getStore().removeData(handle);
    }

    // etc...

```

It must be noted that nothing requires the creation of a new storage entry for a particular value. The only requirement is that the handle returned by a call to `store` results in the same object returned by `make` as long as `release` on that handle is not called. In particular, multiple calls to store the same value may return the exact same `HGPersistentHandle`. The actual storage entry may be reference counted (see http://en.wikipedia.org/wiki/Reference_counting) for the release method to know when to actually remove it. This is precisely what the default primitive type implementations do: every time you store a primitive Java type, say a boolean or an integer, the type implementation will first lookup if that value was already stored and if so return its handle. This is accomplished by maintaining a separate storage index within the type (see the [HGStore](http://www.kobrix.com/javadocs/hgcore/org/hypergraphdb/HGStore.html) class for low-level index management APIs). Of course, there is performance hit to this.

The `make` method constructs a runtime object from storage. This runtime object can represent an atom or it can be simply some nested value within a complex-valued atom. In the latter case, the second and third arguments will be null and must be ignored. In the former, that is when an actual atom instance must be constructed, the second argument will contain the atom's target set and the third argument its incidence set. The target set is essential for constructing runtime `HGLink`s. Usage of the incidence set is optional and depends on type implementations. Incident links can provide information about an atom in the form of annotations, relationships etc., and a type can use that. An example is when complex object structures are represented as hypergraphs at the atom level, instead of at the low storage level. In that case, the fact that an atom is a property of another atom would be represented as a link (e.g. with a  _propertyOf_ label) and the incidence set of the parent atom will provide access to all its properties. Here is a very simple example of a `make` method:

```
public MyType implements HGAtomType
{
    // ...
    public Object make(HGPersistentHandle handle, LazyRef<HGHandle[]> targetSet, IncidenceSetRef incidenceSet)
    {
        byte [] data = graph.getStore().getData(handle);
        // if we were dealing with something else than byte[], we'd "deserialize" here
        return data;
    }

    // etc...
}
```

Finally, the `subsumes` is simply a predicate that return _true_ if and only if its first argument is a more general entity than its second argument. Whatever "more general" means depends on the type. Usually, if this a type-constructor (i.e. the instances of this type are types as well), you'd return _true_ if the second argument is a subtype of the first argument. Otherwise, this is usually implemented using  Java's own `equals` method:

```
public boolean subsumes(Object general, Object specific)
{
    return general.equals(specific);
// or use HGUtils which checks for nulls and does deep comparison for arrays etc:
// return HGUtils.eq(general, specific); 
}
```

## The HGCompositeType Interface ##

Composite types are an abstraction of types that store complex, multi dimensional values. The most prominent example of such a type is the classical record structure. Each dimension is identified with a name and represented as a _projection_ along that dimension. A projection allows you to manipulate a specific dimension (e.g. record slot) of a complex value.

Implement the [HGCompositeType](http://www.kobrix.com/javadocs/hgcore/org/hypergraphdb/type/HGCompositeType.html) interface for complex types:

```
public interface HGCompositeType extends HGAtomType
{
    Iterator<String> getDimensionNames();
    HGProjection getProjection(String dimensionName);
```

The `getDimensionNames` method returns all dimensions of the type and the `getProjection` returns a projection instance along a specific dimension. For example, the `getDimensionNames` of a record type will return an iterator over all slot names and then `getProjection` will return individual slots. The projection instance is implemented in the following interface:

```
public interface HGProjection
{
    // return the name of the projection (e.g. record slot)
    String getName();  

    // return get projection type
    HGHandle getType();

    // get the value along that dimension (e.g. the record slot value)
    Object project(Object atomValue);

    // set the value along that dimension
    void inject(Object atomValue, Object value);

    // specify the storage layout along that dimension - unused for now
    int [] getLayoutPath();
}
```

The methods of `HGProjection` should be obvious. The `getLayoutPath` is currently not used and may actually be removed in the future, so please ignore it. Otherwise, it provides the name of the dimension, its type (i.e. dimensions are typed) and it allows you to read and write the value along that dimension with the `project` and `inject` methods respectively.

## The Storage Interface ##

Any type implementation will likely rely on low-level storage to persist and retrieve a value. The interface to low-level storage of HyperGraphDB is the [HGStore](http://www.kobrix.com/javadocs/hgcore/org/hypergraphdb/HGStore.html) class. Of interest to type implementors are two sets of methods:

  * Methods for storage management of low-level ID tuples (`store, getLink, removeLink`) or byte [.md](.md) (`store, getData, removeData`).
  * Methods for managing storage indices (`getIndex, removeIndex`).

Managing ID tuples is usually done by complex types when the value is better broken down into pieces in storage . Otherwise, types simply perform some sort of serialization/deserialization as raw byte buffer.

The indexing facilities are useful for doing value sharing and reference counting, for example. But a type implementation is free to use them for whatever other appropriate purposes.

Note that a type implementation shouldn't be concerned with managing incidence sets of atoms. However, from storage perspective, the incidence related API (see methods `addIncidenceLink` etc.) can be used independently of the atoms abstraction. In fact, the only reason incidence sets of values are not automatically maintained for every low-level datum is because there hasn't been a strong need for them. But this might change in the future.

## Adding Your Type to a HyperGraphDB Instance ##

Once you've written a custom type, you need to let HyperGraphDB know about it. This is done with the [HGTypeSystem.addPredefinedType](http://www.kobrix.com/javadocs/hgcore/org/hypergraphdb/HGTypeSystem.html#addPredefinedType(org.hypergraphdb.HGPersistentHandle,%20org.hypergraphdb.type.HGAtomType,%20java.lang.Class)) method. Naturally, you must add the type to the database before the first time you use it. Adding a type is a bit like defining an atom: you need to do it only once, not every time you open the database, and you need to provide its persistent handle, the Java `HGAtomType` implementation and (optionally) an associated Java class. On each subsequent opening of the HyperGraphDB instance, the type implementation will be instantiated based on its recorded classname. As a consequence, **predefined types must be default constructible**.

Note the third parameter of the `addPredefinedType` method: a `Class` to be associated with your HyperGraphDB type. When you add a Java object as a HyperGraphDB atom with a call to `HyperGraph.add(Object)`, first the system must determine what HyperGraphDB type implementation to use. For this, it maintains a mapping between Java class and HGDB types. If there is no entry in this map, the type system will try to automatically construct a HGDB type for the concrete class of the atom being added and then add it to the map. The third argument of `addPredefinedType` is a way to explicitly populate that Java class -> HGDB type mapping. Say for example, you have some class in your application called `Foo` and you've written a custom type for it called `FooType`. You would add that type like this:

```
    FooType fooType = new FooType();
    HGPersistentHandle fooTypeHandle = graph.getHandleFactory().makeHandle();
    graph.getTypeSystem().addPredefinedType(fooTypeHandle, fooType, Foo.class);
```

Now, every graph operation on `Foo` atoms will use your own `FooType` type.

Note that you can associate several Java classes with the same HGDB type. The association is many-to-one.

## Replacing an Existing Predefined Type ##

HyperGraphDB comes with a set of predefined types that handles all Java primitives, Java beans, maps, collections, arrays, dates and some of the core atoms as well. Those predefined types are listed in a configuration resource built into the library under `org/hypergraphdb/types` (this is in the `config` source folder). When a new database instance is created, all those types are automatically added to it via calls to `addPredefinedType`. You can override any or all of the predefined types at any time by calling `addPredefinedType` with the appropriate Java class as the third argument. For example, you could implement your own custom type storing Java integers as strings:

```
public static class MyIntType extends HGAtomTypeBase
{
    public Object make(HGPersistentHandle handle,
            LazyRef<HGHandle[]> targetSet, IncidenceSetRef incidenceSet)
    {
        byte[] B = graph.getStore().getData(handle);
        String s = new String(B);
        return Integer.parseInt(s);
    }

    public void release(HGPersistentHandle handle)
    {
        graph.getStore().removeData(handle);
    }

    public HGPersistentHandle store(Object instance)
    {
        Integer x = (Integer) instance;
        return graph.getStore().store(x.toString().getBytes());
    }
}
```

And then replace the existing type for integers like this:

```
graph.getTypeSystem().addPredefinedType(graph.getHandleFactory().makeHandle(), new MyIntType, Integer.class);
```

Note that this will simply associate a different HGDB type with the `Integer` Java class. The original integer type will still be there, with its own atom handle so any previous integer values added with the original type will still be in the database. However, to get to them, you'd need to know the handle of the original type and use it in a query instead of the `Integer.class`. Of course, you also replace all atom typed with the original type by atoms typed with your new type. This will easily work since both old and new types deal with the same Java class. For example:

```
    HGHandle oldType = graph.getTypeSystem().getTypeHandle(Integer.class);
    HGHandle newType = graph.getHandleFactory().makeHandle();
    graph.getTypeSystem().addPredefinedType(newType, new MyIntType, Integer.class);    
    List<HGHandle> current = hg.findAll(graph, hg.type(oldType));
    for (HGHandle h : current)
        graph.replace(h, graph.get(h), newType);
```