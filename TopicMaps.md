## What Are Topic Maps ##

Topic maps are a knowledge representation standard, an RDF rival that did not get as much traction, but is nonetheless supported by an active and enthusiastic community. Whereas in RDF the basis is a subject-predicate-object relationship stemming from traditional logic, topic maps are based on the intuition that knowledge is best represent as a network of interdependent concepts. Topic maps were conceived as a meta-model to talk about information resources. Key concepts are _topics_, _associations_ between topics and _occurrences_ of topics within specific information resources. In a way, RDF makes a commitment about the existence of real world objects that must be described through a series of predicates while topic maps remains within the realm of pure discourse (the information space).

More on topic maps and pointers to further reading can be found on the [Wikipedia Topic Maps Page](http://en.wikipedia.org/wiki/Topic_maps). Latest from the topic maps community can be found at [Topic Maps Labs Home Page](http://www.topicmapslab.de/home).

## Installation ##

To use the topic maps implementation in a HyperGraphDB instance, you will need the hgdbtm.jar and the tmapi-1.0SP1.jar in your classpath. Then install the HGTM application with the following:

```
HyperGraph graph = HGEnvironment.get(your HyperGraphDB instance location);
HGTMApplication app = new HGTMApplication();
app.install(graph);
```

Then you can create a `TopicMapSystem` based on that HyperGraphDB instance with:

```
HGTopicMapSystem tmSystem = new HGTopicMapSystem(graph);
TopicMap tm = tmSystem.createTopicMap("http://mycompany.com/topic-map");
//etc....use the standard topic map API and get familiar with the extra offered
// by the HGTM implementation
```

## Details ##

The HGTM (HyperGraphDB Topic Maps) implementation is based on the older 1.0 version of the topic maps API and data model. The specification can be found [here](http://www.tmapi.org/). HGTM has some extra methods specific to the HGTM representation that you can look up in the [HGTM API JavaDocs](http://www.kobrix.com/javadocs/hgapps/tm/index.html).

In HGTM, all topic maps constructs are represented as HGDB atoms. The Java classes implementing those atoms are in the package org.hypergraphdb.apps.tm. The API is an almost complete implementation of the 1.0 specification. Everything except merging is implementing. Merging wouldn't be hard, but I haven't found the need for it yet.

As anyone familiar with both technologies HyperGraphDB and Topic Maps would easily realize, the representation of Topic Maps within HyperGraphDB is rather straightfoward:

  * Topic, names, variants and occurrences are represented as node (atoms with arity 0).
  * Associations are represents as links between _association roles_.
  * Association roles are represented as links between the role player (a topic) and the type (a topic again).

In addition, information that a topic carries such its type (if any), the set of constructs that it reifies (if any), its occurrences (if any) etc. are represented by HyperGraphDB links. The [HGRel](http://www.kobrix.com/javadocs/hgcore/org/hypergraphdb/atom/HGRel.html) atom type is used to represent such links. All of those link types are listed as handle constants in the [HGTM](http://www.kobrix.com/javadocs/hgapps/tm/org/hypergraphdb/app/tm/HGTM.html) class, so we won't repeat them here. For instance, the fact that a type X is the type of topic Y is stored as follows:

```
graph.add(new HGRel(HGTM.TypeOf, X, Y), HGTM.hTypeOf)
```

In the above `HGTM.TypeOf` is just a convenient name of the relation whereas `HGTM.hTypeOf` is the handle of an atom type predefined when the HGTM application was installed. Similarly, the link between an occurrence and the topic of which it is an occurrence is represented by a `HGRel` of type `HGTM.hOccurrence`. Those implementation details may be important to know if you need to bypass the standard topic maps API, which is rather minimalistic, and perform more complex HGDB queries.