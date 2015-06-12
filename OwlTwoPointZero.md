## What is OWL 2.0 ##

The Ontology Web Language (OWL) 2.0 is a semantic web standard based on Description Logics (DL). More information on the [W3C OWL 2.0 home page](http://www.w3.org/TR/owl2-overview/) as well as at the [OWL 2 Wikipedia topic](http://en.wikipedia.org/wiki/Web_Ontology_Language).

## The HGDB-OWL App ##

_The code is located under the apps/owl in the codebase._

The HyperGraphDB implementation of OWL 2.0 is based on the [OWL API 2.0](http://owlapi.sourceforge.net/) which is the _de facto_ standard for OWL. The OWLAPI is made up of interfaces representing the constructs defined by the [W3C standard](http://www.w3.org/TR/owl2-syntax/) completely and faithfully. The OWLAPI itself offers a reference implementation of those interfaces where all data is in memory. The HGDB-OWL implementation is a drop-in replacement that has all data automatically and transparently persisted in a HyperGraphDB instance.

In the same way that the default OWLAPI handles multiple ontologies at the same time, HGDB-OWL can store multiple ontologies in a single database instance. Each ontology is represented as a [HGSubgraph](http://www.hypergraphdb.org/docs/javadoc/org/hypergraphdb/atom/HGSubgraph.html).

One can use the standard `OWLReasoner` API and any of the available reasoners such Hermit, Fact++ and Pellet. However, it must be noted that reasoners usually load all axioms into their own data structures depending on the reasoning algorithms used and a large ontology may easily blow out the memory. In other words, while HyperGraphDB will handle an unlimited data set, swapping things in out of the cache and allowing you to query arbitrarily large database, a reasoner will need everything in RAM before it can make any inference. To overcome this limitation a reasoner working directly with the database needs to be implemented, but that's a large project on its own.

## Protege Integration ##

While one can find several editors for ontologies, the most popular is by far the [Protege Editor by Stanford University](http://protege.stanford.edu/). It is based on the OWLAPI standard which made it natural and straightforward to develop a HyperGraphDB plugin for it given the HGDB-OWL implementation. The plugin essentially dispenses you from the need to manage files, the need to save your work etc. Everything you do is automatically persisted. In addition you can have each ontology be versioned controlled in your database.

The plugin was developed in the context of the Sharegov.org _Citizen Relationship Management Project_. More about the plugin and download links for the various versions can be found on the ProtegePlugin page.
## Usage - a Quick Guide ##

You can use HGDB-OWL exclusively as an OWLAPI implementation. This approach is recommended if you want to leave the possibility open to easily switching to a different implementation. You can also write code using the HGDB API directly, for instance in order to take advantage of the querying facilities. Either way, first you start by populating with data and for this it's definitely easier to work at the OWL abstraction level.

First you need to create a HGDB-based OWLOntologyManager:

```
import org.hypergraphdb.app.owl.* ;
import org.semanticweb.owlapi.model.*;

HGDBOntologyRepository.setHypergraphDBLocation("/tmp/owldb");
OWLOntologyManager manager = HGDBOWLManager.createOWLOntologyManager();
```

This will create a new HyperGraphDB instance at _/tmp/owldb_. Note that if you don't specify a database directory before anything else, with the `HGDBOntologyRepository.setHypergraphDBLocation` method, one will be created at some random hard-coded place that you probably don't want. So don't forget to set the db directory.

After that you use the `OWLOntologyManager` as you would with the OWLAPI. So getting familiar with the OWLAPI and going through whatever tutorials you find around the internet is a good idea. Another important implementation class is the OWLDataFactory - you must use the one provided by HGDB-OWL. You can get it with `manager.getOWLDataFactory` or if you don't have a reference to the manager, use `OWLDataFactoryHGDB.getInstance()`.

So here is a full program that creates an ontology, adds one subclass declaration to it and then prints it on stdout in the OWL functional syntax:

```

import org.hypergraphdb.app.owl.HGDBOWLManager;
import org.hypergraphdb.app.owl.HGDBOntologyRepository;
import org.hypergraphdb.app.owl.core.OWLDataFactoryHGDB;
import org.semanticweb.owlapi.io.OWLFunctionalSyntaxOntologyFormat;
import org.semanticweb.owlapi.io.StreamDocumentTarget;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

public class Tutorial
{

    public static void main(String [] args)
    {
        try
        {
            HGDBOntologyRepository.setHypergraphDBLocation("/tmp/owldb");
            OWLOntologyManager manager = HGDBOWLManager.createOWLOntologyManager();
            OWLDataFactory factory = OWLDataFactoryHGDB.getInstance();
            OWLOntology O = manager.createOntology();            
            manager.addAxiom(O, factory.getOWLSubClassOfAxiom(
                    factory.getOWLClass(OWLRDFVocabulary.OWL_THING.getIRI()), 
                    factory.getOWLClass(IRI.create("http://example.com#Movie"))));
            manager.saveOntology(O, new OWLFunctionalSyntaxOntologyFormat(),
                    new StreamDocumentTarget(System.out));
        }
        catch (Throwable t)
        {
            t.printStackTrace(System.err);
        }
    }
}
```

## Setting Up a Versioned Ontology Repository ##

The HGDB-OWL implementation provides support for version management of ontologies. One can use it to deploy version control of OWL ontologies that work the same way as conventional source control that developers are familiar with. Both a central repository model (ala SVN) and a distributed repository (ala Github) are supported. The versioning is performing using the standard OWLAPI change objects. Committing, rolling back, merging etc. are safely performed just like you would with SVN ot Github. There is a programmtic interface to this functionality and naturally it is all integrated in Protege.

If you want to run a database server with versioned ontologies, there's a small program in the codebase that does it:

`org.hypergraphdb.app.owl.versioning.distributed.VDHGDBOntologyServer`

The server uses the HGDB-P2P package for communication. That is, it is a server configured as a peer in an XMPP network. You read up on that at the PeerToPeerTutorial page. Then you can run this program by providing a JSON configuration file as with any other HGDB peer. Here is an example of such a configuration file:

```
{
"interfaceType"	: "org.hypergraphdb.peer.xmpp.XMPPPeerInterface",
"peerName"	: "HGDBPeer",
"bootstrap" : [ {"class" : "org.hypergraphdb.peer.bootstrap.AffirmIdentityBootstrap", "config" : {}},
                {"class" : "org.hypergraphdb.peer.bootstrap.CACTBootstrap", "config" : {}}
              ],
"interfaceConfig" :
{
    "user" : "hgdbowl",
    "password" : "password",
    "serverUrl"	: "http://hgdbowl.mycompany.com",
    "autoRegister" : true,
    "ignoreRoster" : false
},
"OntologyServer" : "true"
}
```