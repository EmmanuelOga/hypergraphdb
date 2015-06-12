## What is TuProlog ##

TuProlog a pure Java Prolog interpreter developed developed at the University of Bologna, Italy, see its [home page](http://alice.unibo.it/xwiki/bin/view/Tuprolog/) for more information. The architecture of the interpreter is modular which makes relatively easy to extend. It also has a nice interface to Java and small memory footprint. A good, concise description of its features can be found at the [TuProlog Wikipedia Page](http://en.wikipedia.org/wiki/TuProlog).

## TuProlog + HyperGraphDB ##

The integration of TuProlog and HyperGraphDB has the following goals:

  1. Ability to store Prolog facts and rules (i.e. Prolog programs) in a HyperGraph.
  1. Ability to perform HyperGraphDB queries from Prolog. This amounts to being able to represent query conditions as Prolog terms.
  1. Ability to represent hypergraph atoms as prolog terms so they participate in the unification process natively.

The idea is to work with HyperGraphDB data in a natural way, as if it was part of the Prolog  system.

Full API Javadocs (of the original tuProlog and our extensions) [can be found here](http://www.kobrix.com/javadocs/hgapps/prolog/index.html).

## Codebase Fork ##

To achieve the stated goals, we had to fork the TuProlog codebase. The modularity of TuProlog permits additions of _predicate libraries_ implemented in Java, but it doesn't permit pluggable implementations of the interpreter's rule base (`ClauseStore`s, in TuProlog terms).

## Implementation ##

### Clause Stores ###

The implementation relies on a newly added `ClauseStoreManager` that maintains a list of `ClauseFactory`s. When a Prolog term must be translated into set of clauses to be verified (possibly one by one through backtracking), TuProlog creates a `ClauseStore` instance. The original TuProlog implementation uses the current theory stored in RAM. The modified implementation tries all factories in the clause manager and returns the result as soon as one of the factories is able to construct a clause store from the Prolog term, falling back to the default RAM implementation.

This strategy allows arbitrary HGDB conditions to be treated as Prolog predicates and thus one can have a Prolog program backtrack through a HGDB result set. For this to work, however, an application must bind predicates to HGDB conditions explicitly so that they can be recognized by the HGDB clause factory. Code samples of this are given below. Thus, the integration provides for a very large factbase for the Prolog engine with efficient database style indexing etc.

Besides the HyperGraphDB backed clause factory, we've added a factory for arbitrary Java collections and Java maps. Usage samples below.

### The HyperGraphDB Atom Term ###

We've added another variety of Prolog term to TuProlog in addition to the `Var` (representing variables),`Number` (representing numbers) and `Struct` (representing strings, symbols and compounds): the `HGAtomTem` which represents an atom in the HyperGraphDB instances. The `HGAtomTerm` will unify with another term `T` if

  1. `T` is also an atom term representing the same atom (the HGDB handles are equal).
  1. `T` is bound to the HGDB handle (as a Java object) of the same atom.
  1. `T` represents a Java object that is `Object.equals`  to the atom's value.
  1. The atom is a string that is equal to the symbol represented by `T`.
  1. The atom is a Prolog term the unifies with `T` (not implemented yet).

### Prolog Terms Storage ###

HyperGraphDB types have been created to store Prolog terms. See the `alice.prolog.hgdb` package. Numbers, ground symbols and variables are represented in a trivial way by storing their values (the value of a variable is its name). Structs (compound terms) are represented as HGDB links with values the functor names.

## Usage ##

To use additional clause factories, you need to explicitly add them to a Prolog interpreter instance. Here is an example:

```
import alice.tuprolog.*;
import alice.tuprolog.clausestore.*;
			
Prolog prolog = new Prolog();
prolog.getEngineManager()
  .getClauseStoreManager().getFactories().add(new JavaCollectionStoreFactory());
prolog.getEngineManager()
  .getClauseStoreManager().getFactories().add(new JavaMapStoreFactory());
```

The above installs factories to work with Java collections and maps. You can implement and plug your own factories in a similar way. For instance, you could implement a factory that queries a SQL database server or some other form of persistent storage. Here's an example of using Java collections and maps as predicates:

```
collection_item(C, Item),
map_entry(Map, Key, Value)
```

To plug HyperGraphDB into a Prolog interpreter, you need to attach the `HGPrologLibrary` following the standard TuProlog extension mechanism. That library will plug the HyperGraphDB clause store factory and it will also export some predicates related to HyperGraphDB processing:

```
HGPrologLibrary lib = HGPrologLibrary.attach(graph, prolog);
Map<String, HGQueryCondition> map = lib.getClauseFactory().getPredicateMapping();
map.put("myrelation/3", hg.type(MyRelation.class));

//... etc.

```

The `HGPrologLibrary.attach` method expects a `HyperGraph` instance and `Prolog` instance. A HyperGraphDB based clause factory will be plugged into the interpreter, but you need to associate predicates with HGDB conditions in order to make use of it, as the following lines show. The `MyRelation` class is presumably a `HGLink` implementation of some 3-way relationship (i.e. links of arity 3). In Prolog it will be available as a `myrelation/3` predicate. The predicates and functions exported by the `HGPrologLibrary` are listed in the following table:

| **Predicate** |**Description** |
|:--------------|:---------------|
| hg\_atom/1    | Succeed if its argument is a HGDB atom and fails otherwise. |
| hg\_bind/2    | Bind a predicate to a HGDB condition. Has the same effect as additing to he HGDB clause store factory predicate mapping |
| hg\_unbind/2  | Remove a predicate binding from the HGDB clause store factory. |
| hg\_count/1   | Returns the number of atoms matching the condition specified as the argument |
| hg\_find\_all/1 | Returns a Prolog list of all atoms matching the argument condition |
| hg\_clause\_condition/1 | Returns the HGDB condition objects corresponding to the Prolog clause passed as the argument |

Note that `hg_find_all` and `hg_count` recognize HGDB conditions as Prolog terms. For example:

```
L is hg_find_all(and(type(MyRelationType), incident(SomeAtom)))
```

The following functors are recognized as forming HGDB conditions with their obvious meanings: `type/1`, `typePlus/1`, `incident/1`, `target/1`, `and`, `or`, `link/n`, `orderedLink/n`.

So far, we've described mostly how a Prolog interpreter is extended with HGDB storage and representations. This doesn't require installing the Prolog HGDB application into your HyperGraphDB instance. But to store Prolog terms (facts or rules) into a HyperGraphDB instance, first you will need to install the module like so:

```
import alice.tuprolog.hgdb.*;

HyperGraph graph = HGEnvironment.get(....);
new PrologHGDBApp().install(graph);
```

The above will install TuProlog typing in your database instance.