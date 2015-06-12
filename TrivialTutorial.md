DISCLAIMER: this page is written by Users. It is not an "official" hypergraphDB document. It is continually extended. Corrections and feedback welcome.

# Intro #
This wiki gives a short introduction on the most common tasks you will do with hypergraphdb. These tasks include creating a database, store, query and retrieve objects and links, traverse a graph and create indexes.


# Overview #


# How to setup HypergraphDB? #
You just need 3 jar files in your classpath:
  * hgdb-version.jar
  * hgdbbje-version.jar
  * je-version.jar

To get them, download hgdbdist-1.2-final.tar.gz and
bdb50\_lin64\_libs.tgz from [here](http://code.google.com/p/hypergraphdb/downloads/list). Get the hypergraphDB jars out of the libs directory and the je.jar from the toplevel directory of the bdb50\_lin\_libs.tgz contents.

# How to build? #
**Reminder**:
most users skip this section. No need to build for typical usages of hypergraphDB.

## maven ##
mvn is now the build tool to use. Note, that it is not intended to be used from the top-level of the source tree. The top-level parent pom is there as a place holder for common configuration settings. Start by building 'core' and then storage/bdb-je.

## ant ##
There are working build.xml files with jar dependencies for the following sub-projects which have to be build separately:
1) core - has to be build first, the others use a relative path to
include the hgdb-1.2.jar as a dependency
2) storage/bdb-je
3) storage/bdb-native
4) p2p

Note: if you build all sources, i.e. both bdb-je and bdb-native, you will get some error. If uncertain, use bdb-je.
For in-depth info, check
[here.](http://www.hypergraphdb.org/learn?page=IntroInstall&project=hypergraphdb)


# How to instantiate HyperGraphDB? - Hello World #
Just a kind of 'Hello World' example to show up how easy it is to instantiate HypergraphDB and .
```
HyperGraph graph = new HyperGraph("/path/to/workdir/bje");
String hello = graph.get(graph.add("Hello World")); 
System.out.println(hello.toLowerCase());
graph.close();
```

Notes:
  * graph.add returns something that graph.get consumes - HGHandle, that's how atoms are referred to.
  * graph.get returns a String object with working toLowerCase method - this shows that hypergraphDB is also an object-oriented database.
  * if you manage several hypergraph instances, use HGEnvironment.get(location).
  * do not forget to close after usage, because that might lead to corruption. It's recommended to wrap your code in a try block, and ensure graph.close() in a finally block.


# How to store and retrieve data? - the trivial way #
```
String someObject = "Lorem ipsum";
HGHandle handle1 = graph.add(someObject);
System.out.println(graph.get(handleToSomeObject));
System.out.println(((String) graph.get(handle1)).toUpperCase());
```
->
Lorem ipsum
LOREM IPSUM

Notes:
  * graph.add(...) returnes a "handle" with which you can access the datum directly (as seen in graph.get a line below). A handle generally is an auto-generated database ID.
  * using add/get to store and retrieve data is not necessarily the typical way hypergraphdb is used because:
  * graph.get() requires you to know the handle. This is often not the case, especially when you want data that you did not store just moments ago (hence most of the time! :-) ). Therefore most accesses typically happen by querying.
  * If graph.add(someObject) is called more than once (for example by accident, each time you run a given program), you would end up with duplicates that can be disturbing when querying.
  * the second println demonstrate that we get a fully functional java object, in that case, it has a working toUpperCase method. This shows that hypergraphDB is not only a graph-database, but also a full object-oriented database.

# How to design your classes... custom types? #
HypergraphDB has a powerful and highly customizable type system that can represent any Java type (and more). However, the custom type details can be confusing.
**The good news is that you don't need to fiddle with custom type implementations at all, as long as you define your classes according to the [Java Bean standard](http://en.wikipedia.org/wiki/JavaBeans#JavaBean_conventions)**: this simply means that your class must provide a null-argument-constructor and each property must have a getter and setter.
For example, the java Bean definition of the class of the book example from [here](http://www.hypergraphdb.org/learn?page=IntroStoreData&project=hypergraphdb) would look like this:

```
public class Book {
    String title;
    String author;
    public Book() {}  // nullary-constructor

    public String getTitle() {return title; }
    public void setTitle(String title) {this.title = title;}

    public String getAuthor() {return author;}
    public void setAuthor(String author) {this.author = author;}
}
```

That really is all there is to it!


# How to store and retrieve data without knowing the handle? - querying basics #
Querying is done conveniently by using the static helper class "hg":
```
import org.hypergraphdb.HGQuery.hg.*;
```


We stored only one element in our database, so we would immediately find "Lorem ipsum" simply by querying for type String:
```
System.out.println(hg.getOne(graph, hg.type(String.class)));
```
-> "Lorem ipsum";


hg.getOne returns you any one (of possibly many) matching result as a ready-to-use object, just as did graph.get. But often we have several items that match a certain criteria. We get can get those packed in a List, with hg.getAll:
In order to have two atoms, we just add the same object as above:
```
HGHandle handle2 = graph.add(someObject);
```

and then query for all Strings:
```
        for (Object s : hg.getAll(graph, hg.type(String.class)))
            System.out.println(s);
```

->
Lorem ipsum
Lorem ipsum

We get two results here, that happen to be distinct duplicate copies of the same data (we prove that later).

As you see, querying is generally used in one of this ways:
```
hg.getOne (graphInstance, QueryCondition); // ->  any one matching object.
hg.getAll (graphInstance, QueryCondition); // ->  all matching objects as a List.
```

where "QueryCondition" in our example is hg.type(String.class), but of course there is more.

For official query documentation see [here](https://code.google.com/p/hypergraphdb/wiki/IntroQuerying)


# How to query? #
Sometimes you need handles and also you do not want to dereference and deserialize all results of a query into memory. To query by returning handles is easy. It's the same as with getOne/getAll, but instead of h.getOne you use hg.findOne. Instead of hg.getAll, use hg.findAll.

We use this to confirm that we created actual duplicate atoms in the "Lorem ipsum" example above. We printout the handles, and check for equality with the handles obtained before:

```
         for (Object s : hg.findAll(graph, hg.type(String.class)))
        {
            System.out.println(s);
            System.out.println((s.equals(handle1) || s.equals(handle2)));
        }
```

->
259b3dbd-4e4f-4566-b850-1029f99e6d1b
true
dceadb0c-318b-4249-917a-559d2f077fcc
true

For official query documentation see [here](https://code.google.com/p/hypergraphdb/wiki/IntroQuerying)


# How to store data uniquely? #
How to make sure that a given data is stored only once, even when -by accident or not- the data is stored twice?

```
String object2 = "dolor sit amet";
HGHandle noDup1 = hg.assertAtom(graph, object2);
HGHandle noDup2 = hg.assertAtom(graph, object2);  //trying hard to duplicate
System.out.println("Are those two handles duplicates, i.e. two distinct handles? : " + (!noDup1.equals(noDup2)));
```

->
"Are those two handles duplicates, i.e. two distinct handles? : false"

Note that logically there is a cost associated with checking if a given datum already exists. If you don't need unique atoms, graph.add is faster.

# How to create Links and query for Links? #

Till now there was nothing graph, only object-oriented database functionality. We also did not do particularly interesting queries.

Let's make a link and query for it:
```
        HGHandle duplicateLink = graph.add(new HGPlainLink(handle1, handle2));
        List<HGHandle> dupsList = hg.findAll(graph, hg.link(handle1, handle2));
        System.out.println("querying for link returned that duplicate Link? :" + dupsList.contains(duplicateLink));
```
=>
querying for link returned that duplicate Link? :true

For official query documentation see [here](https://code.google.com/p/hypergraphdb/wiki/IntroQuerying)

# How to make your own link? #
You might wonder where typical graph / hypergraph edges are in HypergraphDB.
Hgdb relies on a more general concept of a graph, which is based on tuples rather than the more common definition of sets of pairs.
The main difference is that you must define the meaning of the position in the tuple yourself. You do that by implementing the HGLink interface, or simply by extending HGPlainLink. Hgdb itself is "unaware" which element(s) constitute the head / tail respectively.

As an example, consider the definition of the HGBergeLink class, which implements the more common Hyperarc definition in terms of HGPlainLink:
```

public class HGBergeLink extends HGPlainLink
{
	private int tailIndex = 0;
	
	public HGBergeLink(HGHandle...targets)
	{
	    super(targets);
	}
	
	public HGBergeLink(int tailIndex, HGHandle...targets)
	{
		super(targets);
		this.tailIndex = tailIndex;
	}
	
	public HGBergeLink(HGHandle [] head, HGHandle [] tail)
	{
		HGHandle [] targets = new HGHandle[head.length + tail.length];
		System.arraycopy(head, 0, targets, 0, head.length);
		System.arraycopy(tail, 0, targets, head.length, tail.length);
		tailIndex = head.length;
	}

	public Set<HGHandle> getHead()
	{
		HashSet<HGHandle> set = new HashSet<HGHandle>();
		for (int i = 0; i < tailIndex; i++)
			set.add(getTargetAt(i));
		return set;
	}
	
	public Set<HGHandle> getTail()
	{
		HashSet<HGHandle> set = new HashSet<HGHandle>();
		for (int i = tailIndex; i < getArity(); i++)
			set.add(getTargetAt(i));
		return set;		
	}
	
	public int getTailIndex()
	{
		return tailIndex;
	}

	public void setTailIndex(int tailIndex)
	{
		this.tailIndex = tailIndex;
	}	
}

```

# How to make relationships? #
In the example above, we are created a link, but the information that the contained atoms are duplicates is silent.
In order to create a meaningful relationsship, there is -among other ready-made links- HGRel. Since HGRel is part of a more powerful mechanism, you first define a HGRelType (which is comparable to a SQL table definition). In there you define the (~table) name, and the types of the atoms participating in the relationship. Since being duplicate or not is general, we use the Top-Type here.

```
        HGTypeSystem hts = graph.getTypeSystem();
        hts.getTop();
        HGHandle duplicateRelType = graph.add(new HGRelType("duplicates!", hts.getTop(), hts.getTop()));
        HGHandle hgrelDuplicateLink =graph.add(new HGRel(handle1, handle2), duplicateRelType);
  System.out.println("does handle2 have a duplicate? : " +
                hg.findAll(graph, hg.and(hg.link(handle2), hg.type(duplicateRelType))).contains(hgrelDuplicateLink));

```
=> does handle2 have a duplicate? : true


# How to query for properties? #
Consider you have a Bean like this:

```
public class Name {
    private String surname;
    public String getSurname() { return surname; }
    public void setSurname(String surname) { this.surname = surname; }

    private String firstName;
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName;  }

    public Name(){};
}
```

and you stored some names:
```
          Name b = new Name();
            b.setSurname("Hugo");
            b.setFirstName("Victor");
            graph.add(b);

            Name c = new Name();
            c.setSurname("Chavez");
            c.setFirstName("Hugo");
            graph.add(c);

            Name d = new Name();
            d.setSurname("Camus");
            d.setFirstName("Albert");
            graph.add(d);
```

then in this query we ask for people that 1) do have and 2) that don't have "Hugo" either as a surname or as a first name:
```
        List<Name> hugos = hg.getAll(graph, hg.and(hg.type(Name.class), hg.or(hg.eq("surname", "Hugo"), hg.eq("firstName", "Hugo"))));
        List<Name> noHugos = hg.getAll(graph, hg.and(hg.type(Name.class), hg.not(hg.or(hg.eq("surname", "Hugo"), hg.eq("firstName", "Hugo")))));

        for(Name n : hugos)
                System.out.println("hugo: surname:" + n.getSurname() + " . first name: " + n.getFirstName());

        for(Name n : noHugos)
            System.out.println("Not a Hugo: surname:" + n.getSurname() + " . first name: " + n.getFirstName());

```

output:
hugo: surname:Hugo . first name: Victor
hugo: surname:Chavez . first name: Hugo
Not a Hugo: surname:Camus . first name: Albert

For official query documentation see [here](https://code.google.com/p/hypergraphdb/wiki/IntroQuerying)


# How to and why index? #
Indexing is not required for querying, but done for performance reasons. For the above example, you would define an index like this before running the query:
```
    HGHandle bTypeH = graph.getTypeSystem().getTypeHandle(Name.class);
        graph.getIndexManager().register(new ByPartIndexer(bTypeH, "surname"));
        graph.getIndexManager().register(new ByPartIndexer(bTypeH, "firstName"));
        graph.runMaintenance();
```

# How to traverse a graph in HypergraphDB? #
```
         String[] strings = "Look here, this is a String, that is gonna be split into words and linked together".toLowerCase().replaceAll(",", "").split("\\s+");
        HGHandle[] stringsHandles = new HGHandle[strings.length];
        for (int i = 0; i<strings.length; i++){
            stringsHandles[i] = graph.add(strings[i]);
        }
        for (int i = 0; i<strings.length; i++){

                if (i< strings.length-2){
                HGPlainLink link = new HGPlainLink(stringsHandles[i], stringsHandles[i+1], stringsHandles[i+2]);
                graph.add(link);
            }
        }
        HGALGenerator alGen = new DefaultALGenerator(graph, hg.type(HGPlainLink.class), hg.type(String.class),false, true, false);
        HGTraversal trav= new HGBreadthFirstTraversal(stringsHandles[0], alGen);
        while(trav.hasNext()){
            Pair<HGHandle, HGHandle> pair = trav.next();
            System.out.println("\nTraversing. Current word: " + graph.get(pair.getSecond()));
        }
```



# How to find the shortest path? #
Assuming you have a graph with stored atoms A to Z, and some links that interconnect them in some way, such that A and Z are connected. For finding the shortest path between A and Z:

```
HyperGraph graph = ...;
HGHandle A,B,C,D,Z,ab,bz,ac,cd,dz;

// adjacency list (AL) generator describes which links and atoms are visited for the search. In many cases you might want to define a custom one
HGALGenerator adjGen =  new DefaultALGenerator(graph);

// Unless you only care about the length of the shortest path, you have to create a result map beforehand, so you can access the result later.
Map<HGHandle, HGHandle> predecessorMap = new HashMap<HGHandle, HGHandle>();

double paths = GraphClassics.dijkstra(
                       A,        // start atom
                       Z,        // goal atom
                       adjGen,
                       null,     // weights of links: null if all links count equal
                       null,     // distance Matrix: In most cases it's ok to put null here, since you don't need to know the distances between all atoms. The length of the shortest path will be returned by the method

                       predecessorMap  // not null, unless only length of shortest path is required);


```

However, this doesn't give you directly the shortest path, it has to be extracted from the predecessor map (which also contains other visited atoms that are not part of the shortest path). Here is a (not thoroughly tested) method for extracting it:

```
  public static List<HGHandle> extractShortestPath(Map<HGHandle, HGHandle> predecessorMap, HGHandle start, HGHandle goal){
        List<HGHandle> shortestPath = new LinkedList<HGHandle>();
        HGHandle currentPredecessor = goal;
        while(currentPredecessor != start){
            currentPredecessor = predecessorMap.get(currentPredecessor);
            shortestPath.add(currentPredecessor);
        }
        return shortestPath;
    }
```



# How and when to create custom types? #
Generally you don't have to create a custom HGDB type yourself. If you just add an object of your type to the DB, it will create a corresponding HGDB type for you. If you make that choice to let HGDB create the type, you have to provide a default constructor (that is, a constructor with zero arguments) for your class.
If you want to create the type yourself, say because you want to optimize how the objects are stored, then for a property-based query to work you'd have to implement the HGCompositeType and HGAtomType interfaces.

# When to close, when to leave open? #
If hypergraphdb is killed before the clean shutdown process has succeeded, there might be data loss. In production systems, you are recommended to wrap your code in a try block, and ensure graph.close() in a finally block.
Since in most situations there is no real danger of ungraceful shutdown, and since opening and closing are associated with a cost, you can usually leave the database open until the application finished (see [here](https://groups.google.com/d/topic/hypergraphdb/GVo4rGGFQfs/discussion)).


# How to and why use transactions? #
All DB operations already ensure that a transaction is in effect so when you're doing single, isolated operations you don't have to wrap the code in a transaction. But if you want multiple operations to be executed atomically, then you should. Generally, transactions should be always used.

```
   //EITHER:
  graph.getTransactionManager().ensureTransaction(new Callable<HGHandle>() {
                public HGHandle call() {
                    return graph.add("Hello World.");
                }
            });

  // OR (especially for void methods
   graph.getTransactionManager().beginTransaction();
        try
        {
           graph.add("hello World");
           graph.getTransactionManager().commit();
        }
        catch (Throwable t)
        {
            graph.getTransactionManager().abort();
        }
```
For details, see [here](https://code.google.com/p/hypergraphdb/wiki/IntroTransactions)

# How to represent my data? #
One point of confusion is what role links play in the representation of beans. The answer is: none, links are independent. This means the graph structure is something orthogonal  to, i.e. independent of, the representation of complex objects. If one wants to represent bean structures at the level of the atom graph, one can always write custom types to do it. However the current interfaces don't make it easy to do this in a generic way.
So, it is recommended, that you make all your classes JavaBeans (and make sure they are recognized as such by the type system). This is the minimum to expose your "fields" and avoid you having to manually manage projection and custom types, etc.
One pitfall is that non-primitive "fields" that are contained in your class, will be hidden and opaque and will prevent you from querying on them. In order to expose those relations, the easiest solution is to use links (alternatively, the HGAtomRef construct may help in some cases).
In summary, a good way is save primitive values in your beans, save each component separately and use links (HGLink) to relate the different objects. Then you can really start enjoying HG with all its power. (snippets taken from [this forum thread](https://groups.google.com/d/topic/hypergraphdb/oYb5VZQV5cc/discussion), thanks to Alain and Boris).

# How to increase performance? #
  1. Unless you need strong guarantees, consider disabling transactions (!)
  1. When using transactions, consider wraping coherent chunks of ~100 operations into a transactions. This can reduce transaction overhead significantly. (not shown above)
  1. Unless in a distributed environment, consider using SequentialUUIDHandleFactory, which keeps related data close in the Btree used by BerkeleyDB
  1. Increase BerkeleyDB-Cache size, default value is a bit low

```
HGConfiguration config = new HGConfiguration();
SequentialUUIDHandleFactory handleFactory = new SequentialUUIDHandleFactory(System.currentTimeMillis(), 0);
config.setHandleFactory(handleFactory);
config.setTransactional(false);
BJEConfig storeConfig = (BJEConfig) config.getStoreImplementation().getConfiguration();
storeConfig.getEnvironmentConfig().setCacheSize(1024 * 1024 * 1000);
graph = HGEnvironment.get(HGDB_LOCATION, config);
```


# How to bulk import data? #
  1. avoid assertAtom, instead use a simple Map to avoid duplicates
  1. for addition of lots of objects of the same type, use graph.add(object, typeHandle)
  1. if using transactions, wrap chunks of coherent data into transactions
  1. if import fails, try temporarily disabling maintenance (config.setSkipMaintenance(true))

Example bulk import demonstrating above points (untested code):
```
   
    int transactionChunkSize = 100;
    HGConfiguration config = new HGConfiguration();
    config.setSkipMaintenance(true);  //only if import fails otherwise
    HyperGraph graph = null;
    graph.setConfig(config);

    List<MyObject> a = ...;
    bulkImport(a);

    public <T> void bulkImport(final Collection<T> at) {
        if (at.isEmpty())
            return;

        final Map<T, HGHandle> handleMap = new HashMap<T, HGHandle>(at.size());
        final T first = at.iterator().next();
        HGHandle firstH = graph.add(first);
        handleMap.put(first, firstH);
        final HGHandle typeHandle = graph.getType(firstH);

        if (config.isTransactional())
            graph.getTransactionManager().ensureTransaction(new Callable<Boolean>() {
                public Boolean call() {
                    return assertAtomLight(at, typeHandle, handleMap);
                }
            });
        else
            assertAtomLight(at, typeHandle, handleMap);

        return;
    }

    public <T> Boolean assertAtomLight(Collection<T> at, HGHandle typeHandle, Map<T, HGHandle> handleMap) {
        for (T o : at) {
            HGHandle result = handleMap.get(o);
            if (result == null) {
                result = graph.add(o, typeHandle);
                handleMap.put(o, result);
            }
        }
        return true;
    }

```

Note: If your collection is very big (say >1000), and you have Transactions enabled, you may consider spliting your collection into chunks such that a single failure does not force the entire import to be rolled back and repeated.

# Real World Example #
see [here](http://code.google.com/p/hypergraphdb/wiki/RealWorldExample)