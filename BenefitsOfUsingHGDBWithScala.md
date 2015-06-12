**Disclaimer**: _this is user-created content and not an official HyperGraphDB document. Theses are experimental hacks and as of yet unsuitable for production._ **This page requires a revision.**
For up-to-date information, check [here](http://scalahypergraph.blogspot.de/2012/12/a-shift-of-perspective-hacking.html) and following blog-posts.
(Majority of hacks here work. However, although TypedHandle instances are valid HGHandle, the type information does not survive serialization & deserialization)

Index
  * 1) Intro
  * 2) Demo
  * 3) Commented scala implementation


# 1. Intro #
HyperGraphDB is directly usable by other JVM-languages such as Scala. Scala is interesting not only as an everyday JVM-frontend that dramatically reduces java boilerplate, but also provides a versatile, powerful but no-risk toolset to use and easily adapt hypergraphDB in completely new ways, beyond mere syntactic sugar.
For a more detailed introduction into scala and interesting features, checkout [this blogpost](http://scalahypergraph.blogspot.de/2012/07/interesting-scala-features.html) written with hypergraphDB in mind.

Please note that the features listed below work only within scala.


This post describes some simple hacks that allow to:
  * call methods of the referenced java object on a typed version of HGHandles. Alternatively, short parametrized dereferencing methods on regular HGHandle
  * starting traversals as method calls on a HGHandle, using that handle as starting atom, strongly simplified by (overridable) default parameters, or parametrized as a shorthand for traversing specific Links / Siblings. Benefitting of scala's collection functions such as map/filter or intersecting different traversals.
  * pseudo-operator for quick link creation (`<->`) available after HGHandle
  * shorthands for querying the atomtypecondition of the type of the given handle, optionally combined with a further condition
  * typesafe check if handle references a HGLink using Option (can be flatMap-ed over in collection functions)
  * treat HGLink instances as collections of handles, again allowing use of wealth collections functions
  * use java objects in places where their handles are required. Alternatively, ultrashort methods on anyObject for obtaining an existing handle, assert or force add to graph.
  * calling update on a mutated object


# 2.ShortDemo #

```
    //
    // TypedHandle & Autodereference
    //

    val tht = graph.put("hallo Welt")
    val thtr = tht.replaceAll("hallo", "hello").replaceAll("Welt", "World")
    print(thtr)
    // "hello World"

    //
    // Traversing on Handle
    //
//creating some atoms
    val a = "hello".hh
    val b = "pink World".hh

//creating some links with a, b and other atoms
    val l1 = a <-> b
    val l2 = "screwed up World".hh   <->   ( "Optimism is a lack of information".hh ,b)
    val l3 = 13.hh                   <->   ( false.hh, b,  (-14).hh)
    val ll1 = l1 <-> (l2, a, l3, b)

    print("\ntesting traverse default. Filtering to Strings then toUpperCase ")
    b.traverse().filter(p => p.getSecond.getType.equals(classOf[String])).map(p => p.getSecond.d[String]).map(_.toUpperCase).foreach(print)
//    HELLO
//    OPTIMISM IS A LACK OF INFORMATION.
//    SCREWED UP WORLD

    print("\ntesting typeTraverse. reverse")
    b.typeTraverse[HGPlainLink, String]().map(p => p.getSecond.d[String]).map(_.reverse).foreach(print)
//    dlroW knip
//    .noitamrofni fo kcal a si msimitpO
//      dlroW pu dewercs

    print("\ntesting traverse. Other strings containing World ")
    val containsWorld = b.traverse(sibling = new AtomTypeCondition(classOf[String])).filter(p => p.getSecond.d[String].contains("World"))
    containsWorld.foreach(p => print(p.getSecond.d[String]))
//   screwed up World


    print("\ntesting traverse. intersect all vs containing world")
    val ii =  b.traverse().toSet
    val i = ii.intersect(containsWorld.toSet)
    i.map(pair => pair.getSecond.d[String]).foreach(s => print("intersect: " + s))


    print("\nTesting neighbours")
    b.neighbours.foreach(link => link.foreach(h=> print(h.d[String])))
//    screwed up World, Optimism is a lack of information.

    print("\n Testing typeBrothers")
    b.typeAlikes.foreach(handle => print(handle.d[String]))
//    Weltscrewed up Worldhallo Weltpink WorldhelloOptimism is a lack of information.    

    print("\n Testing neighbourBrothers")
    b.neighbourBrothers.foreach(handle => print(handle.d[String]))
//  Brothersscrewed up WorldhelloOptimism is a lack of information.

    print("\n Testing indexOf handle b in c:")
    print(l2.getLink.get.indexOf(b))
//  2
    
    print("\n Flatmaping for links on link c:")
    ll1.getLink.get.flatMap(h => h.getLink).foreach(link => link.foreach(h => print("printing " + h.d[String] + "\t\t on Link" + link)))
//  printing hello		 on LinkPlainLink([2],weakHandle(a02........) printing pink World on Link....................

    print("\n Handle.getLink, testing link getPreceeding/succeeding:")
    l2.getLink.get.getPreceeding(b).foreach(print)
    print("succceeding should be none")
// (none)

```


| **operation** | **HGDB API / java** | **HGDB scala wrapper** |
|:--------------|:--------------------|:-----------------------|
| get Handle    | ` graph.getHandle(someObject) ` |  ` someObject.h ` or  ` someObject ` |
| assert Atom   | ` hg.assertAtom(graph, someObject) ` | ` someObject.hh ` or  ` someObject ` |
| add atom      | ` graph.add(someObject) ` |  ` someObject.hhh ` or  ` someObject ` |
| get atom      | ` ((T) graph.get(someHandle)) ` |  ` someHandle.d[T] ` or  ` someHandle ` |
| create Link of handles a,b,c | ` graph.add(new HGPlainLink(a,b,c) ` |  ` a <-> (b,c) `       |
| traversal on HGPlainLinks and Strings | `  HGALGenerator alGen = new DefaultALGenerator(graph, hg.type(HGPlainLink.class), hg.type(String.class)); HGTraversal trav= new HGDepthFirstTraversal(someHandle, alGen); while(trav.hasNext()){ Pair<HGHandle, HGHandle> pair = trav.next(); SomeType t = ((SomeType) graph.get(pair.getSecond()));` |  ` someHandle.typeTraverse[HGPlainLink, String](dfs = true).map(p => p.getSecond.d[String])` |

# 3. Commented scala implementation #
A HGHandle is similar to a Java object reference: it is a pointer to some data stored in memory or on disk. Although HypergraphDB atoms are strictly typed, hghandles themselves are not typed in the java world. Furthermore, since hypergraphDB does some sophisticated form of serialization, objects obtained by graph.get(somehandle) have to be cast to their respective type.
The following hacks aim to add intuitive usage patterns, for example by treating handles as java objects (and vice versa if required).
This is achieved by introducing a class _TypedHandle_ which is simply parametrized by the type of the object that it references. Using three equally simple implicit conversions:
a) the HyperGraph class is extended to provide two methods which create/accept TypedHandle instances.
b) TypedHandle instances are converted to the objects they references.
c) Any object can be converted to HGHandle instances, which are accepted by hypergraphDB methods requiring handles.


### TypedHandle & auto-dereferencing ###
```
class TypedHandle[T](@BeanProperty val handle:HGHandle) extends HGPersistentHandle {
  def toByteArray = handle.getPersistent.toByteArray
  def toStringValue = handle.getPersistent.toStringValue
  def compareTo(p1: HGPersistentHandle) = handle.getPersistent.compareTo(p1)
  def getPersistent = handle.getPersistent
}

  implicit def hypergraph2richhypergraph(graph: HyperGraph) = new {
    def put[T](someObject: T): TypedHandle[T] = new TypedHandle[T](graph.add(someObject))
    def gety[T](richHandle:TypedHandle[T]):T  = graph.get(richHandle).asInstanceOf[T]   //richHandle.d(graph)
  }
    // this adds two simple methods to HyperGraph which return /accept TypedHandle

    implicit def richHandle2T[T](handle:TypedHandle[T])(implicit graph :HyperGraph):T = graph.get(handle).asInstanceOf[T]
    // treat typedhandle of T as a T 


```

### Hacks on HGHandle ###
```
   implicit def richHandle(handle:HGHandle)(implicit graph :HyperGraph) = new {

    def d[T](implicit graph:HyperGraph):T = graph.get(handle).asInstanceOf[T]
        // dereference and type cast - may fail

    def ds[T](implicit graph:HyperGraph):Option[T] = try {Option(graph.get(handle).asInstanceOf[T])} catch {case _ => None}
        // save alternative of d[] using Option to flatMap / pattern match on

    def <->(handle2:HGHandle*)(implicit graph:HyperGraph):HGHandle = graph.add(new HGPlainLink(handle :: handle2.toList :_*))
        // new HGPlainLink with vararg handle2.
        // <-> as pseudo-Operator

    def newRel(relation: String, handle2:HGHandle*)(implicit graph:HyperGraph):HGHandle = graph.add(new HGRel(relation, handle :: handle2.toList :_*))
        // new HGRel

    def getType(implicit graph:HyperGraph):Class[_] = graph.getTypeSystem.getClassForType(graph.getType(handle))  // what is Class<?> / Class[_] ? that's a type constructor no?
        // get Class file of atom

    def typeAlikes(implicit graph:HyperGraph):java.util.List[HGHandle] = hg.findAll[HGHandle](graph, sameTypeQC)   //(ev.erasure))
        // return all atoms of same type

    def sameTypeQC:HGQueryCondition = new AtomTypeCondition(getType)
       // shorthand query condition of same type

    def queryOnSameType(queryCondition: HGQueryCondition)(implicit graph:HyperGraph):java.util.List[HGHandle] = hg.findAll[HGHandle](graph, hg.and(sameTypeQC, queryCondition))
        // query only on atoms of same type

    def traverse (onLink:HGAtomPredicate  = null,
                  sibling:HGAtomPredicate = null,
                  prec: Boolean = true,   suc: Boolean = true,
                  rev:  Boolean = false,  dfs: Boolean = true
                 )(implicit graph:HyperGraph):HGTraversal = {
      val alGen: HGALGenerator = new DefaultALGenerator(graph, onLink, sibling,prec, suc, rev)
      val trav: HGTraversal = if (dfs) new HGDepthFirstTraversal(handle, alGen) else new HGBreadthFirstTraversal(handle, alGen)
      trav
    }
        // start traversal departing from handle, optionally override named default params

    def typeTraverse[L <: HGLink, A] ( prec: Boolean = true,  suc: Boolean = true, rev: Boolean = false,  dfs: Boolean = true )(implicit graph:HyperGraph, evL:Manifest[L],evA:Manifest[A]):HGTraversal =
          traverse(onLink = new AtomTypeCondition(evL.erasure), sibling=new AtomTypeCondition(evA.erasure), prec =prec, suc = suc, rev=rev, dfs = dfs)(graph)
        // parametrized traversal, traverses on Links of Type L and on siblings of Type A

    def incidentLinks:java.util.List[HGHandle] = hg.findAll[HGHandle](graph, hg.incident(handle))
        // returns handles to all links pointing to this handle

    def neighbours = incidentLinks.map(linkHandle => hg.findAll[HGHandle](graph, hg.target(linkHandle)).filter(h => !h.equals(handle)))
        // returns siblings of each Link pointing to this handle

    def neighbourSet = incidentLinks.view.map(linkHandle => hg.findAll[HGHandle](graph, hg.target(linkHandle)).filter(h => !h.equals(handle))).flatten.toSet
        // returns siblings flattend into a set

    def neighbourBrothers = neighbourSet.intersect(typeAlikes.toSet)
        // intersection of type brothers in the neighbourhood

    def getLink:Option[HGLink] = try {Option(graph.get(handle).asInstanceOf[HGLink])} catch { case _ => None }
        // return an Option of HGLink referenced (or not) by handle.
  }

```


### Hacks on HGLink and any Object ###
```
  //
  // HGLink hacks
  //
  implicit def link2traversable(link:HGLink)(implicit graph :HyperGraph, ev:Manifest[HGHandle]) = new IndexedSeq[HGHandle]{
    def length = link.getArity
    def apply(idx: Int) = link.getTargetAt(idx)
  }
    // implicit conversion that implements a collection trait which allows treating HGLink as a collection

  implicit def richLink(link:HGLink)(implicit graph :HyperGraph, ev:Manifest[HGHandle]) = new {
    def getPreceeding(handle:HGHandle):IndexedSeq[HGHandle]  = link.take(link.indexOf(handle))  // off-by one here?
    def getSucceeding(handle:HGHandle):IndexedSeq[HGHandle]   = link.drop(link.indexOf(handle)+1)
  }
    // two examples usages of scala collection functions on HGLink

  //
  // Hacks on any Object
  //
  implicit def pimpAny(any:Any) = new {
      def update = graph.update(any)
  // update changes to object in graph
      def h:HGHandle = graph.getHandle(any)
      def hh:HGHandle = hg.assertAtom(graph, any)
      def hhh:HGHandle = graph.add(any)
  }

```