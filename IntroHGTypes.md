## HyperGraphDB is a Typed World ##

Understanding the details of the typing system is not essential for many uses of HGDB, but it is always helpful to have a general intuition of how things work in a piece of software so we present a brief introduction here.

Every value stored in HyperGraphDB is typed. All programming languages and database systems have types in one form or another, but what sets HyperGraphDB apart is that types themselves are atoms in the graph just like regular data. This makes HyperGraphDB into a reflexive database. So the type of every atom is also an atom and, as all atoms, is identified with a `HGHandle`, it is indexed, cached, can participate in graph relationships etc. The HGDB type is a different entity than the Java class of an atom. In fact, the Java class of an atom and the HGDB type of the same atom can be completely unrelated. However, in practice there is a correspondence between the two in that to most Java classes with instances stored as HGDB atoms, by default there are HGDB types that manage the low-level storage. And in most cases in practice you don't even need to know the `HGHandle` of an atom's type since the API in general accepts a Java `Class` and looks up the corresponding HGDB type automatically.

When you add an atom with code like this:

```
A a = new A(....);
graph.add(a);
```

the first thing the system does is try to find out whether there's a HGDB type corresponding to the Java class `A`. And if there is already a HGDB type associated with the class `A` it will simply use it. If not, it will create such a type and store it as a new _type atom_ before continuing with the storage of `a`. This new atom type will be henceforth associated with the Java class `A.class` and reused when more instances of this class are added to the graph. To obtain the handle of the HGDB type corresponding to a Java class, you can make the following call:

```
HGHandle handleA = graph.getTypeSystem().getTypeHandle(A.class);
```

You can then link to that handle or use wherever a type atom is expected. If you already have a type handle and you'd like to store a particular atom under that type, call:

```
A a = new A(...);
HGHandle typeHandle = ...//some means to obtain the atom type
graph.add(a, typeHandle);
```

This will work even if there's already some different atom type associated with the class `A`. Well, it will work provided the atom type identified by the `typeHandle` variable knows how to deal with Java instances of `A`. In fact, the main responsibility of an atom type is serializing runtime objects into a low-level database representation and converting them back from this low-level representation into runtime objects. Types are implementations of the [HGAtomType](http://www.kobrix.com/javadocs/hgcore/org/hypergraphdb/type/HGAtomType.html) interface. Take a look at the methods of that interface - they resemble a conventional CRUD API.

Since we've mentioned that HGDB types are "just" atoms, you may wonder what the types of those atom types are. They are atoms too, of course. The system is bootstrapped with a set of predefined types whose type is the special type `top` from [HGTypeSystem](http://www.kobrix.com/javadocs/hgcore/org/hypergraphdb/HGTypeSystem.html) class. But what about the type of a type created on the fly? For example, upon adding an instance of `A` above for the first time, a HGDB type corresponding to `A.class` is created and stored in the database, as an atom. The type of this newly created atom type is not `top`, but rather something called a _type constructor_ : an atom type whose value range consists of other atom types. If you are familiar with functional programming or C++ templates, the concept shouldn't sound foreign.

For practical purposes, there are predefined implementations to handle Java POJOs, arrays, collections, maps and objects implementing the `java.io.Serializable` interface. If you want to store a Java objects that is neither serializable, nor does it follow the Java Beans naming conventions to expose its state, then you will need to develop a custom type for that Java class. Developing a custom type amounts to implementing the `HGAtomType` interface and is not hard at all - see the [writing a custom type](RefCustomTypes.md) topic. Once you've developed a custom type, you can plug it into the system with a call to  `HGTypeSystem.addPredefinedType`.

As a last point about type management in HyperGraphDB, we note that inheritance is represented by special purpose link atoms of type [HGSubsumes](http://www.kobrix.com/javadocs/hgcore/org/hypergraphdb/atom/HGSubsumes.html). The system uses such links when asked to, for example, to find all atoms of a given type or of any of its sub-types. `HGSubsumes` links are created between a class and its superclass and between a class and all of the interfaces it implements.

[<< Prev - Atom Handles](IntroHGHandles.md)  [Next - Querying >>](IntroQuerying.md)