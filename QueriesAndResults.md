## Introduction ##

Here are a few idioms and code snippets that show how to do queries with HGDB and how to work with result sets. Full documentation of the API is within the Javadocs.

Unlike other parts of the HyperGraphDB, the querying system is not yet extensible because we don't have a robust framework for interpreting and optimizing queries yet. On the other, since even low-level APIs (such as indexing) are public it is possible implement any custom query conceivable, but with more work.

The current querying paradigm takes the universe of atoms stored in a HyperGraphDB as the starting result set and lets you restrict it via various conditions constraining the atoms' values, types and linkage structure. The result of a query is always a stream of atoms.

## Query API Overview ##

Because there is no query language for HGDB at the time of this writing (though we've outlined some ideas on TowardsHyperGraphQueryLanguage page), queries are build up as query conditions, classes implementing the `HGQueryCondition` interface, and submitted via a call to `HyperGraph.find`. For example:

```
HGQueryCondition cond = new And(new AtomTypeCondition(MyLink.class), new IncidentCondition(atom));
HGSearchResult<HGHandle> rs = graph.find(cond);
while (rs.hasNext()) System.out.println(rs.next());
rs.close();
```

will retrieve all links of type `MyLink` that point to `atom` (a `HGHandle`). A more coding friendly API is provided by the `HGQuery.hg` class which, with Java 5 static imports, can be used like this:

```
import org.hypergraphdb.HGQuery.hg;

HGQueryCondition cond = hg.and(hg.type(MyLink.class), hg.incident(atom));
HGSearchResult<HGHandle> rs = graph.find(cond);
// etc...
```

In both cases a `HGSearchResult` is returned which behaves roughly like a JDBC result set. It implements the standard `java.util.Iterator` interface and an extension to it for moving backwards `org.hypergraphdb.TwoWayIterator`. Thus, it can be traversed back and forth via calls to its `hasNext, next, hasPrev` and `prev` methods.

The `HGQuery.hg` interface offers a few additional convenience methods to avoid having to deal with `HGSearchResult`:

| findOne | Retrieve the first item from the query result set |
|:--------|:--------------------------------------------------|
| getOne  | Same as findOne, but implicitly dereference the item, assuming it is a HGHandle |
| findAll | Retrieve all results from a query and put them in a java.util.List |
| getAll  | Same as findAll, but implicitly dereference each item. |

In general, we strongly advise using those methods when the result set is not very large and when the whole set will have to be traversed. More on the rationale of this recommendation below.

In addition, to every `HGQueryCondition` class that one finds in the `org.hypergraphdb.query` package, there corresponds a convenience constructor method in `HGQuery.hg` interface. To get a full view of this interface, visit [its Javadocs](http://www.kobrix.com/javadocs/hgcore/org/hypergraphdb/HGQuery.hg.html). In what follows, we won't be using the "raw" conditions, but this convenience API.

## Logical Operators ##

The three standard logical operators _and_, _or_ and _not_ are supported via calls to the variable argument methods `hg.and`, `hg.or` and the `hg.not` method. It must be noted that the _not_ operator can result in inefficient queries because most of the time it cannot be translated to an index lookup and the negated condition needs to be used as a predicate while scanning a potentially large result set.

## Querying by Type and Value ##

Each atom has a type and a value. And values may be composite records. To constrain the type of an atom use the `hg.type` or `hg.typePlus` methods. The first returns a condition constraining the result set to a specific type and the second returns a condition constraining it to a all sub-types of a given type. Both take either the `HGHandle` of the type of interest or a Java `Class` that has been mapped to a HyperGraphDB type. The `hg.typePlus` condition is equivalent to an "or" between all sub-types. For example, if class (or interface) A has two derived class B and C then:

```
  List<HGHandle> result = hg.findAll(graph, hg.typePlus(A.class));
```

is equivalent to:

```
  List<HGHandle> result = hg.findAll(graph, hg.or(hg.type(B.class), hg.type(C.class)));
```

Note that (since a Java can implement mulitple interfaces), it might make sense to have a query like `hg.and(hg.typePlus(X), hg.typePlus(Y))`, but an "and" between two exact types like `hg.and(hg.type(X), hg.type(Y))` would always return an empty result set.

Atom values are constrained with the following set of operators (i.e. methods in the `HGQuery.hg` interface):

| eq(Object x) | The value must be equal to the passed in object. |
|:-------------|:-------------------------------------------------|
| eq(String part, Object x) | The property _part_ of a complex value must be equals to _x_. |
| lt, lte      | Less-than and less-than-or-equal operators. Note that this only works when atom values actually have an order relation defined on them. |
| gt, gte      | Greater-than and greater-than-or-equal operators. |

Note that all the operator in the above table work both on atom values proper and on value parts (e.g. bean properties).

For instance, here is a query that find all atoms of a hypothetical type `PathLink` whose `label` property has the value "highway" and whose `length` property is greater than 1000:

```
  List<PathLink> longHighways = hg.getAll(graph, hg.and(hg.type(PathLink.class), hg.eq("label", "highway"), hg.gt("length", 1000)));
```

## Querying the Graph Structure ##

The atoms in the result can be constrained to be links pointing to a set of atoms and/or targets to a set links. The two main operators are `hg.target(linkHandle)` and `hg.incident(atomHandle)`. The first states that an atom in the results should be a target of (i.e. a member of the outgoing set of, i.e. pointed to by) the link identified by `linkHandle`. Conversely, the second states that an atom in the result set should be a link pointing to (i.e. incident to) the atom identified by `atomHandle`.

When searching for links that have a known target set, or where a subset of the target set is known, use the `hg.link` or `hg.orderedLink` operators. Both take an arbitrary number of atom handles as parameters. The `hg.link` operator ignores the order of the atoms in a target set while the `hg.orderedLink` operator doesn't. When searching for ordered links, which is probably the most common case, one must pay attention to the link's arity in addition to listing its target set in the desired order. An expression like

`hg.orderedLink(x, y, z)`

while return all links that point to x, y and z in that order regardless of whether their target sets contain other atoms before, after or in-between x, y and z. Thus a link with a target set [a, x, b, y, z, c, d] will match. One could further constrain the link's arity:

`hg.and(hg.orderedLink(x, y, z), hg.arity(3))`

But it is much more common to have a type constraint in the conjunction:

`hg.and(hg.type(linkType), hg.orderedLink(x, y, z))`

because in general links of a given type all have the same arity and a type condition is perhaps the most direct and efficient way of reducing the set of possible results (otherwise all atoms in the graph must be scanned).

A very common situation is when you know some of the link's targets at specific positions, but not all. Take the `HGSubsumes` link for example. Such a link is added between two types A and B whenever A is a more general type (e.g. a parent class) of which B is a specific case (a derived class).  To find all `HGSubsumes` links with A is the parent the following conditions wouldn't work:

`hg.orderedLink(A)`

because it doesn't say whether A must appear in the first or in the second position of the link. To remedy the problem, one should full specify the target set by putting an _any_ indicator at positions where the actual target is unknown. This any indicator is a special `HGHandle` constant defined in the `HGHandleFactory` class and also available by calling `hg.anyHandle()`. Thus the above condition should be rewritten as:

`hg.orderedLink(A, hg.anyHandle())`

Standard graph traversals are also implemented as search operators in the form of `hg.bfs` (for breadth-first search) and `hg.dfs` (depth-first search) family of methods. Traversal search conditions are translated to the implementation in the `org.hypergraphdb.algorithms` package and use the [default adjency list generator](http://www.kobrix.com/javadocs/hgcore/org/hypergraphdb/algorithms/DefaultALGenerator.html) to do the traversal.

## Transforming the Result Set ##

The stream of atoms resulting from a query can be transformed by applying mapping to each item. This is done with the `hg.apply` operator. There are several common predefined mappings in the `HGQuery.hg` class such as `hg.deref`, `hg.linkProjection` and `hg.targetAt`. All of those method return an instance of the `org.hypergraphdb.utils.Mapping` interface which essentially defines a one argument function. For example, to obtain the actual sub-types from the set of `HGSubsumes` link resulting from the condition above, one could write:

`hg.apply(hg.linkProjection(1, hg.and(hg.type(HGSubsumes.class), hg.orderedLink(A, hg.anyHandle()), hg.incident(A))))`

This condition says: all targets at position 1 (i.e. the second target) of `HGSubsumes` links whose first target is _A_.

While it is not possible to define custom query conditions at the moment, one can easily define new mappings for use in expressions such as above.

## Closing Result Sets ##

**It is very important** that `HGSearchResult` instances be closed properly and promptly (i.e. as soon as possible). They will generally hold an open cursor on the filesystem and must be closed like any other external resource, be it an open file or a socket connection, or an SQL result set. And unlike the example above, as a good coding practice working with a `HGSearchResult` should always be enclosed in a `try ... finally` block:

```
HGSearchResult<HGHandle> rs = graph.find(cond);
try { use rs here } 
finally { HGUtils.closeNoException(rs); }
```

Note the use of `HGUtils.closeNoException` above. In the HGDB API, we've chosen not to throw any checked exceptions because it's annoying when you don't care about them. However, as with many other HGDB methods, a call the `HGSearchResult` may well throw an `HGException`. If you don't want that exception to propagate and interrupt your program, then the `HGUtils.closeNoException` is a convenient shorthand for code such as `try { rs.close(); } catch (Throwable t) { }`.

The most common case of a deadlock in HGDB happens due to a non-closed search result set. Whenever a result set is not closed, it keeps a lock on some part of the database which makes it impossible to write data to that part. This can happen in a multithreaded as well as in a single-threaded application, but it mostly happens within a single thread! In multi-threaded applications, conflicts are resolved by repeatedly retrying transactions in a random order. Thus, if there is a thread scanning a result set while another thread is trying to write some data, the latter transaction will be retried until the result set from the former gets closed and the database unlocked. But in a single thread, an open result set followed by an attempt to write will lock indefinitely because there result set never gets the chance to close. For this reason, it is a good coding practice to first read all results that one cares about (e.g. in a Java collection), close the result set, and then continue with actual processing.
