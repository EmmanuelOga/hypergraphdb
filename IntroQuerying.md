There is no special purpose query language for HyperGraphDB, yet. Therefore, querying for atoms is performed with a special purpose API. Like in many other database systems, the API is based on conditional expressions that you create, submit to the query system and get back a set of atoms as the result.

## Query API Overview ##

The conditional expressions for querying are build up out of classes in the [org.hypergraphdb.query package](http://www.kobrix.com/javadocs/hgcore/org/hypergraphdb/query/package-summary.html). Some of the conditions apply to atom values (the Java object) while others apply to the graph structure (how atoms are link between each other). You can create a query condition by instantiating those classes. Here is an example:

```
    HGQueryCondition condition = new And(
              new AtomTypeCondition(Book.class), 
              new AtomPartCondition(new String[]{"author"}, "George Bush", ComparisonOperator.EQ));
    HGSearchResult<HGHandle> rs = graph.find(condition);
    try
    {
        while (rs.hasNext())
        {
            HGHandle current = rs.next();
            Book book = graph.get(current);
            System.out.println(book.geTitle());
        }
    }
    finally
    {
        rs.close();
    }
```

The condition should be self-explanatory: it asks for atoms of type Book and whose author is George Bush. The [AtomPartCondition](http://www.kobrix.com/javadocs/hgcore/org/hypergraphdb/query/AtomPartCondition.html) lets you constrain the value of an atom by one of its object properties.

As you can see, the result is returned in the form of  [HGSearchResult](http://www.kobrix.com/javadocs/hgcore/org/hypergraphdb/HGSearchResult) instance. Search results are basically bi-directional iterators (you can go back to a previous item) that you need to close once you are done with them, similarly to a JDBC `ResultSet` object. If a search result is not closed, it will maintain locks on certain parts of the database which in turn is likely to cause a deadlock in your application.

That's simple enough, albeit a bit verbose. But there is a much more convenient syntax to work with HyperGraphDB queries.  The [HGQuery.hg](http://www.kobrix.com/javadocs/hgcore/org/hypergraphdb/HGQuery.hg.html) class defines an extensive list of so called "factory methods" (methods that instantiate objects in lieu of constructing them directly) for creating and composing query conditions. It also defines several other general purpose utility methods which you are encouraged to examine.

Here is the above condition rewritten in this style:

```
// Use Java 5 static import facility to import the HGQuery.hg 
// namespace.
import org.hypergraphdb.HGQuery.hg;

//
// Given some HyperGraph instance that has some Books added to it.
//
HyperGraph graph = new HyperGraph(tutorialHyperGraphLocation);
List<Book> books = hg.getAll(hg.and(hg.type(Book.class), hg.eq("author", "George Bush")));
for (Book b : books)
    System.out.println(b.getTitle());
```

For example, the `hg.and` takes an arbitrary number of arguments, each a query condition and returns a composite condition representing the conjuction of all sub-conditions. There  are several methods for comparing values: `hg.eq`, `hg.lt`, `hg.gte` etc. and they all will construct a corresponding condition for comparing against an atom's value or an atom's property value.

Of note in the above example is the `hg.getAll` method. It will load from storage and put in a standard Java list all atoms of the search result set. This is convenient when you don't expect a large result set or when you need the whole of it in RAM anyway. The `hg.findAll` method does the same, but it returns a list of `HGHandle`s instead of pre-loading the atom from disk.

You can create query objects and execute them repeatedly:

```
HGQuery query = HGQuery.make(graph, condition);
rs = query.execute();
//etc…
```

The advantage of creating a [HGQuery](http://www.kobrix.com/javadocs/hgcore/org/hypergraphdb/HGQuery.html) object is that the condition is analyzed and a query plan is created and stored as part of that object, thus increasing performance each time the query is executed.

The following sections give a list of common query factory methods thematically grouped.

## Logical Operators ##

The three standard logical operators _and_, _or_ and _not_ are available:

| **hg Factory Method** | **Description** |
|:----------------------|:----------------|
| and(c1, c2, ...)      | performs a logical conjunction of conditions c1, c2...etc |
| or(c1, c2, ...)       | performs a logical disjunction of conditions c1, c2...etc |
| not(c)                | performs a logical negation of the conditions c |

## Querying Atom Values ##

The usual comparison operators are available when constraining the values of atoms in the result set. They are "enumed" in the [ComparisonOperator class](http://www.kobrix.com/javadocs/hgcore/org/hypergraphdb/query/ComparisonOperator.html). For every operator, the `HGQuery.hg` supplies a factory method as a shortcut. In addition, value comparing factory methods come in two flavors: comparing atom values or comparing atom property values (a.k.a. bean properties in the Java world). Here's a summary:

| **hg Factory Method** | **Description** |
|:----------------------|:----------------|
| value(x, op)          | compare the atom's value against x using the ComparisonOperator op|
| part(path, x, op)     | compare the atom's property referred by _path_ against x using the ComparisonOperator op |
| eq(x), eq(path, x)    | the atom (or its property identified by _path_) must be equal to x|
| lt(x), lt(path, x)    | the atom (or its property identified by _path_) must be less than x|
| gt(x), gt(path, x)    | the atom (or its property identified by _path_) must be greater than x|
| lte(x), lte(path, x)  | the atom (or its property identified by _path_) must be less than or equal to x|
| gte(x), gte(path, x)  | the atom (or its property identified by _path_) must be greater than equal to x|

Naturally, the operators lt, lte, gt and gte can only be applied to values that are comparable.

## Querying Graph Structure ##

A few conditions deal with the linkage between atoms. You can constrain an atom to be a link having a certain form or to be the target of a link. Here is table summarizing those conditions:

| **hg Factory Method** | **Description** |
|:----------------------|:----------------|
| target(x)             | the atom must be a target of the link x. |
| incident(x)           | the atom must be a link pointing to x. |
| link(h1, h2, ...)     | the atom must be a link whose target set includes atoms h1, h2...etc.|
| orderedLink(h1, h2, ...) | the atom must be a link whose target set is ordered and where h1, h2...etc appear in exactly that order.|
| arity(n)              | the atom has arity n.|
| disconnected()        | the atom has no links pointing to it. |

Note that:

```
    hg.link(h1, h2, h3) == hg.and(hg.incident(h1), hg.incident(h2), hg.incident(h3))
```

In fact, this is how link lookups are implemented: by intersecting incidence sets of the desired link target set.

## Typing Constraints ##

Whether you are querying atoms by their value or relationships in the graph, it is frequently necessary to include a typing constraint. For example, given an `AtomValueCondition`, the query system will easily infer the desired atom type from the Java class of the value. But this is impossible with a single `AtomPartCondition` for example.

Asking for atoms with a specific type is akin to a _from_ clause in an SQL query because all atoms are automatically indexed by their type. This is done with `hg.type` factory method. If you don't care about specific type, but only about a super-type (i.e. base class or an interface in Java), you can use the `hg.typePlus` condition.

| **hg Factory Method** | **Description** |
|:----------------------|:----------------|
| type(class), type(handle) | the atom must have the specified type, either as a Java class or as a HGDB handle |
| typePlus(class), typePlus(handle) | the atom's must be either of the specified type or a of a type inheriting the specified type |
| subsumes(x)           | the atom must subsume (i.e. be more general) the atom specified in the condition |
| subsumed(x)           | the atom must **be** subsumed (i.e. be more specific) by the atom specified in the condition |

[<< Prev - Atom Types](IntroHGTypes.md)  [Next - Graph Traversals >>](IntroGraphTraversals.md)