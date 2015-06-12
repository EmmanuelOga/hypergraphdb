**Disclaimer: work in progress, do not use for critical data.**

# Overview #


# Introduction #
This page provides info about an experimental HypergraphDB storage implementation based on the java data/processing grid [Hazelcast](http://www.hazelcast.com/). Hazelcast was chosen because it offers a plethora of advanced features, good performance all while being easy to use.
The Hazelcast storage implementation -from now on called **"Hazelstore"**- allows to share a read/write hypergraphDB database by forming a peer-to-peer network with backups and auto-failover, but without central servers and without single points of failure. Besides simple sharing of HGDB databases, another advantage is that in theory the size of the HypergraphDB database can be much larger than the amount of RAM or even the diskspace available on a single cluster member (because data is evenly partitioned in the cluster).
Hazelstore was designed such that number of network hops and amount of data transferred over the network are minimal. Furthermore, in the constrains of the contract, an asynchronous mode allows operations that do not return values to immediately return without waiting for the network (for example index.addEntry/removeEntry).


# Setup #
All you need is the four things on the classpath: hypergraphdb-core (at least version 1._3_), hazelcast-{Version}.jar (not "hazelcast-client"), scala-libraries (at least scala.2.10) and hazelstore. You can do that in two ways: either checkout current trunk [here](http://code.google.com/p/hypergraphdb/source/checkout), or download three jars:
  * [current hypergraphDB-1.3-Snapshot jar](http://hypergraphdb.googlecode.com/files/hgdb-1.3-SNAPSHOT.jar)
  * [Hazelcast jar](http://www.hazelcast.com/downloads.jsp).
  * [scala jars](http://www.scala-lang.org/downloads)
  * [hazelstore jar](https://hypergraphdb.googlecode.com/svn/trunk/storage/hazelstore/jars/hazelstore.jar)

Note: Hazelstore is written mostly in scala, but you are _not_ required to do any setup for scala unless working with scala source code. If you use the jars, you just need the scala jars to be on your classpath, that's it.

# Basic Usage #
Usage of HypergraphDB with Hazelstore is simple. Use it like this:

```
    val graph = new HyperGraph()
    val config = new HGConfiguration
    config.setTransactional(false)
    config.setStoreImplementation(new Hazelstore)
    graph.setConfig(config)
```
Closing of Resultsets, wrapping code in try/finally or shutdown are not necessary.

Hazelstore provides some important configuration settings:
  * asynchronous mode: non-blocking mode where possible. Works reliable only after hypergraphDB instance has been opened. Default = false.
  * transactionalCallables: provides a higher degree of consistency by using Hazelcast transactions for each individual hypergraphDB operation, but also increases the risk of deadlocks. Default = false.
  * Hazelcast configuration: Hazelcast provides a multitude of configuration options relevant for performance, security etc.


```
  val hazelconf = new HazelStoreConfig()
  hazelconf.setUseTransactionalCallables(true)
  val hazelcastConfig = hazelconf.hazelcastConfig

   // gazillion of Hazelcast configuration options. 
   // For more details: http://hazelcast.com/docs/2.6/manual/single_html

  val hs = new Hazelstore(hazelconf)
  val config:HGConfiguration = new HGConfiguration
  config.setStoreImplementation(hs)
  config.setTransactional(false)
  graph.setConfig(config)
  graph.open("anyString")
  hazelconf.setAsync(true)    // activate async after opening hypergraphDB 
  graph.add(...)
```


# Status & current Limitations #
  1. Hazelstore has been tested a lot, but it does not yet pass the entirety of the HypergraphDB tests. Do not use for critical data.
  1. Currently there is no support for transactions, since Hazelcast does not provide 2-phase-commit / XA transactions. While XA is planned for next Hazelcast version 3, it cannot be garantueed Hazelstore will ever have transactions. It is likely that there will be some transaction support but with greatly reduced performance.
  1. Hazelstore does currently not yet provide persistence to disk, but only "persistence" to the cluster - it is as of yet purely in-memory!
However, here are some detailed descriptions including code necessary for persisting your clustered hypergraphDB:
| Persistence| link|
|:-----------|:----|
| MongoDB    |http://blog.codepoly.com/hazelcast-and-mongodb|
|HBase       | http://blog.codepoly.com/distribute-with-hazelcast-persist-into-hbase|
| SimpleDB   | http://blog.codepoly.com/integrating-hazelcast-and-simpledb |
| Amazon EC2/AWS | http://blog.codepoly.com/distribute-your-data-over-amazon-ec2-by-hazel |


# Hazelstore compared to BerkeleDB / HypergraphDB-P2P-Framework? #
Hazelstore is more comparable to BerkeleyDB than to the existing P2P-Framework. As such, it is less powerful but also less complicated. It provides a single hypergraphDB in a network, and is very easy to use and setup. Now tests have been made, but I speculate that Hazelstore performance maybe better than the existing HGDB-Peer-to-Peer framework (HGDB-P2P). However, HGDB-P2P provides many additional abstractions and is not constrained to the contract of HGStorageImplementation etc. Hence, it could be interesting to combine Hazelstore with the HGDB-Peer-to-Peer framework. Furthermore, Hazelcast could be used to reimplement components of HGDB-P2P.