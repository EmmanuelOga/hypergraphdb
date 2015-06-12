# Introduction #

[JSON](http://json.org) became popular as an XML alternative for human readable structured communication between software components. However, lately it has been touted as a convenient storage data structure by various database systems labeled as "document-oriented databases" where a document is a JSON object stored as a blob and possibly indexed by various properties for quicker retrieval.

The **HGDB mJson** component takes a different approach in storing JSON structures and records them as graphs where each JSON primitive value is stored as a HyperGraphDB node and each object and array as HyperGraphDB link.

The representation is based on a very small (1 source file) and concise Json library, called mJson developed in the context of the [Sharegov](http://sharegov.org) project:


[Goto mJson Official Website](http://bolerio.github.io/mjson)

# Get It #

Maven dependency:

```
    <dependency>
      <groupId>org.hypergraphdb</groupId>
      <artifactId>hgdbmjson</artifactId>
      <version>1.2</version>
    </dependency>
```

If you're not using Maven, here are direct links to get mJson and the HGDB storage component:

  1. [mjson-1.2.jar](http://repo1.maven.org/maven2/org/sharegov/mjson/1.2/mjson-1.2.jar)
  1. [hgdbmjson-1.2.jar](http://www.hypergraphdb.org/maven/org/hypergraphdb/hgdbmjson/1.2/hgdbmjson-1.2.jar)

And if you don't yet have HyperGraphDB in your project, make sure also you have the following (see IntroInstall):

```
 <repositories>
    <repository>
      <id>hypergraphdb</id>
      <url>http://hypergraphdb.org/maven</url>
    </repository>
  </repositories>
 <dependency>
    <groupId>org.hypergraphdb</groupId>
    <artifactId>hgdb</artifactId>
    <version>1.2</version>
 </dependency>
 <dependency>
    <groupId>org.hypergraphdb</groupId>
    <artifactId>hgbdbje</artifactId>
    <version>1.2</version>
 </dependency>
```
# Usage #

Before reading this manual, please go over the documentation of the `mJson` library to get at least some familiarity with the API.

To use HyperGraphDB as a Json database, include the two jars above in your project (or the Maven dependency), create a `HyperGraph` instance as you would usually, then create an instance of a [HyperNodeJson](http://www.hypergraphdb.org/docs/apps/mjson/mjson/hgdb/HyperNodeJson.html) passing in the `HyperGraph` object and use that `HyperNodeJson` object to talk to the database. Here's an example:

```
import static mjson.Json.*;
import mjson.hgdb.HyperNodeJson;
import org.hypergraphdb.*;

// We need to configure HGDB with the JSON type schema so
// it knows how to manage JSON entities in storage
HGConfiguration config = new HGConfiguration();
config.getTypeConfiguration().addSchema(new JsonTypeSchema());
HyperGraph graph = HGEnvironment.get(dbLocation.getAbsolutePath(), config);

HyperNodeJson jsonNode = 
     new HyperNodeJson(HGEnvironment.get("/tmp/hgdbjson"));

// Add a JSON object with two properties to the database
jsonNode.add(object("name", "Pedro", 
                     age, 28));

// ... later, do a lookup for all objects 
// with name="Pedro" the results are returned 
// as a Json array:
Json A = jsonNode.findAll(object("name", "Pedro"));

// delete the object with name="Pedro" and age=28:
jsonNode.remove(jsonNode.exactly(
    object("name", "Pedro", age, 28)));
```

As hinted by its name, the `HyperNodeJson` class implements the [HyperNode](http://www.hypergraphdb.org/docs/javadoc/org/hypergraphdb/HyperNode.html) interface so it is an interface to the underlying graph as well. This means you can use it to interact with other types of atoms. Nevertheless, `HyperNodeJson` is a view of the database as a graph of JSON structures. The _Implementation_ section below gives some implementation details of this wrapper and why it is necessary to have it. In addition to all the standard `HyperNode` interface methods, it provides several additional methods related to the representation and covering common use cases. The methods for finding Json elements:

<table>
<tr><td nowrap>match(Json j, boolean exact)<br>
<br>
Unknown end tag for </td><br>
<br>
<br>
<td>Performs a pattern matching lookup returning the first entity in the databases that matches <i>j</i>. The <i>exact</i> parameter specifies whether objects should be matched in full (true), or whether only the properties in the pattern should be matched.</td></tr>
<tr><td nowrap>exactly(Json j)<br>
<br>
Unknown end tag for </td><br>
<br>
<br>
<td>A shorthand for <i>match(j, true)</i></td></tr>
<tr><td nowrap>unique(Json j)<br>
<br>
Unknown end tag for </td><br>
<br>
<br>
<td>A shorthand for <i>match(j, false)</i>. This is intended for lookup of objects that are identified uniquely by a set of properties.</td></tr>
<tr><td nowrap>find(Json j,<br>
<blockquote>boolean exact)<br>
<br>
Unknown end tag for </td><br>
<br>
<br>
<td>Same logic as <i>match</i> except a full HyperGraphDB <a href='http://www.hypergraphdb.org/docs/javadoc/org/hypergraphdb/HGSearchResult.html'>HGSearchResult</a> is returned rather than the first match.</td></tr>
<tr><td nowrap>find(Json j)<br>
<br>
Unknown end tag for </td><br>
<br>
<br>
<td> Shorthand for <i>find(j, false)</i>.</td></tr>
<tr><td nowrap>findAll(Json j)<br>
<br>
Unknown end tag for </td><br>
<br>
<br>
<td>Do a <i>find(j, false)</i> and return all results in a <code>List</code> of <a href='http://www.hypergraphdb.org/docs/javadoc/org/hypergraphdb/HGHandle.html'>HGHandle</a>s.</td></tr>
<tr><td nowrap>getAll(Json pattern)<br>
<br>
Unknown end tag for </td><br>
<br>
<br>
<td>Same as <i>findAll(Json pattern)</i> but return a <code>List</code> of <code>Json</code> entities.</td></tr>
</table></blockquote>

And there's a family of methods to search for JSON properties, represented as name/value pairs by the class [JsonProperty](http://www.hypergraphdb.org/docs/apps/mjson/mjson/hgdb/JsonProperty.html):

<table>
<tr><td nowrap>findProperty(HGHandle name, HGHandle value)<br>
<br>
Unknown end tag for </td><br>
<br>
<td>Find the property atom with the given name (a String atom) and value (a Json atom).</td></tr>
<tr><td nowrap>findProperty(T1 name, T2 value)<br>
<br>
Unknown end tag for </td><br>
<br>
<td>A bunch of overloaded <i>findProperty</i> methods that take different types of arguments (e.g. a String and a Json) and will do essentially what you would expect them.</td></tr>
<tr><td nowrap>findPropertyValues(HGHandle name)<br>
<br>
Unknown end tag for </td><br>
<br>
<td>Retrieve all Json values that appear in properties with the given name atom.</td></tr>
</table>

All above methods have to do with searching the database and pattern matching on JSON structures. Adding entities also has several variants. And they have to do with how you decide to deal with repeated data (i.e. duplicates). One way to view a database, especially one build on HyperGraphDB, is as a knowledge base where each atom represents a unique and immutable piece of knowledge. In that view, you would not allow for duplicate data and every JSON structure (or pimitive value) that you store will have exactly one copy. You ensure uniqueness by calling `HyperNodeJson.assertAtom` method:

```
Json x = array(1, 2, 3);
// The assert effectively performs an exact match lookup of
// the passed in Json and adds it to the database only when
// no data with the exact same value has been found.
HGHandle handleX = jsonNode.assertAtom(x);
// A second call of assertAtom with the same value
// will return the handle of the existing atom:
assert handleX.equals(jsonNode.assertAtom(x.dup()));
```

Another way to view a database is as a collection of data where each entity is unique simply by virtue of being stored independently. Every newly added HyperGraphDB atom gets its own unique `HGHandle` regardless of its value so that's easily achieved by simply using the standard `HyperNode.add` method:

```
Json x = object("entity", "movie", "title", "Underground");
HGHandle handleX = jsonNode.add(x);
// A second call with the same value will return a the handle
// of a newly created atom
assert !handleX.equals(jsonNode.add(x));
```

Now, the main advantage of storing JSON structures as a graph is that this creates an implicit index of all objects and arrays by their elements. That is, given any JSON property, you can easily find all the objects that have that property simply because the property is a node in the graph and all objects with that property are links pointing to it. And given any JSON value, primitive or not, you can quickly find all arrays that contain it. You can do such a query by pattern matching as shown above, or by first looking up the `JsonProperty` in question and then examining its incidence set. For example:

```
HGHandle propHandle = jsonNode.findProperty("person", 
    object("firstName", "Pedro", "lastName", "Pena");
List<Json> L = jsonNode.getAll(hg.incident(propHandle));
```

Clearly, that strategy works only when Json values are unique in the database. Whenever there are duplicates, results from each duplicate have to be aggregated together in order to properly answer a query.

So what would be the right view for the "average" application - a knowledge base or a collection of data allowing duplicates? The answer is a mix of both. Conceptually, the question of whether a given datum should be duplicated bears on whether the data is representing distinct entities from the application domain. Distinct entities may very well yield the same representation at a certain point in a time, and be equivalent in that sense, yet without being identical. For example imagine you are storing information about job positions where a job position has a title, a salary and a boolean flag indicating whether it is vacant. You may well have two positions with identical information, yet they are distinct entities each subject to change on its own. So at a more practical level, the question of repeated data is related to the question of immutability - if a datum can change independently of other, equivalent data, then it should be stored separately. In an application, one frequently thinks in terms of _business entities_ or _business objects_ that are usually (but not always!) subject to change. And this is a sensible criteria to decide whether something should be stored as its own "record": when it is representing a real-world entity with mutable properties. Everything else is a "mere" value regardless of how complex it is as a data structure. Commonly, business objects are top-level entities in whatever nested representation one has defined as the model of the application domain. As top-level entities, they are mutable, but their components are generally immutable values. And this is the motivation behind the third way of inserting `Json` atoms into HyperGraphDB, the `HyperNodeJson.addTopLevel` method which will create a new atom for the passed in object, but recursively perform an `assertAtom` operation for each of its compnents. For example:

```
// Job benefits as a composite, 
// but immutable value, asserted in the db
Json benefits = object("vacationWeeks", 2, 
                       "healthInsurance", "Aetna");
HGHandle benefitsHandle = jsonNode.assertAtom(benefits);

// Job position as a top-level entity with the same benefits,
// value-wise as defined above
Json jobPosition = 
    object("title", "Dishwasher", 
           "salary", 12000, 
           "vacant", true,
           "benefits", object("vacationWeeks", 2, 
                              "healthInsurance", "Aetna"));
HGHandle accountHandle = jsonNode.addTopLevel(account);

// The same benefits value we added at the beginning 
// is reused in the job position object
HGHandle benefitsProperty = 
   jsonNode.findProperty("benefits", benefitsHandle);
assert jsonNode.getIncidenceSet(benefitsProperty)
         .contains(jobPosition);
```

Finally, note that because `Json` primitives are immutable anyway, it always makes sense to call `assertAtom` when storing primitives. This is the approach that HyperGraphDB's own primitive type implementations take: it's a time-space tradeoff where one sacrifices insert speed for more compact storage. You are probably not going to be adding `Json` primitives directly to the database, but in case you do it is strongly recommended that you use `assertAtom`.

# Implementation #

The implementation is fairly straightforward. `Json` entities are each managed by its own type:

  * Primitives are stored be delegating to HyperGraphDB's own predefined primitive Java type management where each value is stored only once and is being reference counted for removal.
  * Arrays are stored as HyperGraphDB links and they contains no additional payload value. Therefore the cost of retrieving an array is the cost of retrieving a simple link plus the cost of retrieving each of its elements where caching plays an important role.
  * Objects are also stored as links, again with no additional access cost besides link retrieval and the object's properties which are potentially cached.

The sharing of immutable properties plays an important performance role here because recurring values and properties are atoms shared between the various `Json` entities and usually cached. The cost of the additional lookup during insertion is paid of by the ability to cache duplicate values more aggressively. This is the motivation behind the choice to represent Json properties with the `JsonProperty` class - it allows them to be independently cached by the atom cache and it allows JSON objects to be represented as plain links.

To accommodate the mJson API which is not coupled with the HyperGraphDB API, the `HyperNodeJson` implementation has to maintain its own atom cache in order to maintain the caching semantics expected from HyperGraphDB atoms. Here's how that comes about. To avoid implementing the standard `HGLink` interface, arrays and objects are stored as `HGValueLink` instances, even though there's no actual value. This way, HyperGraphDB recognizes them as links. However, we still want to associate a `Json` Java runtime object with the atom `HGHandle` in the cache instead of the `HGValueLink` Java object so that a call to `getHandle(Json)` will return the atom handle. While the `HyperNodeJson` correctly does the wrapping/unwrapping of `HGValueLink`s that represent `Json` objects and arrays, the HyperGraphDB main cache doesn't do that (maybe it should..., but currently it doesn't) so a separate transactional cache is maintained in the `HyperNodeJson` implementation. The need to transparently manage those non-`HGLink` objects as links is one of the main reasons of creating a special purpose database view of the JSON storage. And the existence of the private extra cache is the main reason that it is not advisable to instantiated multiple `HyperNodeJson` instances against the same underlying `HyperGraph` instance.

If you care to look, you may notice some extra classes in the codebase, such as JsonTypeSchema, JsonAtomFactory etc. Those are currently not used and can be considered work in progress for future extensions of the library.

# Future Work #

Possibly enhancements of this HyperGraphDB storage component include:

  * More extensive pattern matching capabilities with partial matching of nested structures, support for wildcards, comparison operators etc.
  * HyperGraphDB specific implementations of `Json` entities (using the Json.Factory) interface that would allow us to get rid of this extra cache and perhaps offer other opportunities for a tighter integration.
  * Implementation of a `Json` based type system that would allow one to specify some meta, type-like information for the otherwise schemaless `Json` entities.
  * Based on this custom type system, implement better control over assert vs. add in nested objects. Currently the distinction is made only b/w top-level vs. nested objects. However, it is not uncommon to have mutable nested business objects as properties of other business objects. In practice we deal with this by storing HyperGraphDB `HGHandle`s as string properties, but this leads to cumbersome and error prone code.