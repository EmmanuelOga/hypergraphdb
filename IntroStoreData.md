## Store by Adding Any Java Object to a DB Instance ##

The short story is that you can just put in whatever object you have in a HyperGraphDB database:
```
  HyperGraph graph = HGEnvironment("c:/temp/test_hgdb");
  String x = "Hello World";
  Book mybook = new Book("Critique of Pure Reason", "E. Kant");
  
  graph.add(x);
  HGHandle bookHandle = graph.add(mybook);
  graph.add(new double [] {0.9, 0.1, 4.3434});
```

There is nothing "graphish" in this example - we are just adding some data in our database. The system will generally do the right thing and store your data in a way that would make it possible to reconstruct an equivalent run-time object from storage later. Storage of concrete values is handled by the HyperGraphDB type system. The type system is a completely customizable layer, but it was built to do the sensible thing by default. And in Java, the sensible thing can be summarized as follows:

  * Translate primitive types and primitive arrays into byte buffers following common industry format.
  * Interpret Java beans as record-like structures, according to the Java Beans conventions.
  * Store built-in arrays and collections as sequence of objects.
  * Store maps as sequences of pairs of objects.
  * Record type inheritance information.
  * Some sensible details that you will learn in the course of usage ;)

Note that when `mybook` is added, we assign the result to a variable of type `HGHandle`. This is essentially an identifier within the HyperGraphDB system, explained in more detail in the [Atom Handles](IntroHGHandles.md) topic. A newly added object will have its handle/identifier assigned by the system. If a Java object is already known to the system, you can update it in storage without passing the handle. But you need the handle when you want to delete some data:

```
...
// Add a new object to the database
HGHandle bookHandle = graph.add(mybook);

// At a later point, update an existing object's value:
mybook.setYearPublished(1988);
graph.update(mybook);

// At a later point, delete the object:
graph.remove(bookHandle);
```

Note that the `HGHandle` is needed to remove the object. You can also replace the object identified by that handle with a completely new object:

```
graph.replace(bookHandle, new Book(....));
```

## The Storage Model ##

Every storage medium has a meta-model, a predefined structure of what things can be stored in it. A file is a sequence of bytes. A relational database is a set of relations (or "tables"). An XML file is a tree of markup elements. A HyperGraphDB database is a generalized graph of entities. The generalization is two-fold:

  1. Links/edges "point to" an arbitrary number of elements instead of just two as in regular graphs
  1. Links can be pointed to by other links as well.

Everything that gets stored in a HyperGraphDB is either a node or a link. A node is simply some object value that does not represent a relationship, it doesn't point to anything else. A **link** is HyperGraphDB's terminology for a graph edge or arrow. A link holds pointers to other entities in the graph and in general represents some sort of relationship. In all cases, we refer to the things in HyperGraphDB as **atoms**. When an atoms is a link, we call the number of atoms it points to its **arity**. Thus, nodes are atoms that have arity 0.

As a final piece of terminology pertaining to graph linkage, we call the set of all links pointing to a given atom its **incidence set** and we call the set of atoms a given link points to its **target set**. If L is a link pointing to the atom A, we say that L is **incident** to A and that A is a **target** of L.

From a Java perspective, an atom is simply some object. From a conceptual perspective, an atom is an entity that can be related to other entities or that can represent such a relation.  The primary role of HyperGraphDB as a database is to store entities networked together by relationships. There are no restrictions on what kind of data is bound to an entity or to a relationship. That is, the data associated with a HyperGraphDB atom can be anything. And because links can point to other links, higher order relationships (i.e. relationships between relationships) are possible. In effect, relations in HyperGraphDB are automatically reified.

## Storing Graphs ##

The example above showed storing an atom in the database amounts to calling the `HyperGraph.add` method. To store an actual graph structure in a HyperGraphDB database, you would need to create links between atoms. HyperGraphDB links are objects that implement the [HGLink](http://www.kobrix.com/javadocs/hgcore/org/hypergraphdb/HGLink.html) interface. So to store a link between two entities (i.e. _atoms_), you would simply add an atom that is an instance of the `HGLink` interface.

There are a couple of general default implementations of the `HGLink` interface:

  * The [HGPlainLink](http://www.kobrix.com/javadocs/hgcore/org/hypergraphdb/HGPlainLink.html) class is a link implementation that carries no additional information with. It contains no additional data and its type is general with no special meaning.
  * The [HGValueLink](http://www.kobrix.com/javadocs/hgcore/org/hypergraphdb/HGValueLink.html) class is similar to `HGPlainLink`, but it allows you to embed an arbitrary Java object in it as "payload". Instances of such links will take on the type of the "payload" object.
  * The [HGRel](http://www.kobrix.com/javadocs/hgcore/org/hypergraphdb/atom/HGRel.html) class is a labeled link implementation with type constraints to its target set (i.e. its arguments). Use of this type of links is documented in [this topic](RefLabeledRelations.md).

Here's an example of using the `HGValueLink`:

```
  HyperGraph graph = HGEnvironment("c:/temp/test_hgdb");
  Book mybook = new Book("Critique of Pure Reason", "E. Kant");  
  HGHandle bookHandle = graph.add(mybook);
  HGHandle priceHandle = graph.add(9.95);
  HGValueLink link = new HGValueLink("book_price", bookHandle, priceHandle);
```

While in general HyperGraphDB strives for a minimal API intrusiveness, implementing meaningful representations is best done by defining your own `HGLink` implementations. Other predefined links with specific semantics and some of which are used by HyperGraphDB itself can be found in the [atom package](http://www.kobrix.com/javadocs/hgcore/org/hypergraphdb/atom/index.html).

Note the use of the [HGHandle](http://www.kobrix.com/javadocs/hgcore/org/hypergraphdb/HGHandle.html) interface in the above example. Handles are the basic means to refer to atoms in HyperGraphDB as detailed in [this topic](IntroHGHandles.md).

[<< Prev - Creating a Database](IntroDBCreate.md)  [Next - Atom Handles >>](IntroHGHandles.md)