## Introduction ##
HyperGraphDB is an embedded database and as such it is meant to be used within the same process as your application. Therefore, learning to use HyperGraphDB (or HGDB for short) amounts to learning its API and the concepts behind it. The implementation is based on the very robust and ubiquitous [BerkeleyDB database](http://www.oracle.com/database/berkeley-db/index.html). There are very few configuration options that you need to be aware of. And there are virtually no setup/installation steps to get up to speed and use the database. You only need to incorporate the library in your application. In case of problems, such as if your application experiences a serious crash and the default recovery mechanism is not sufficient, you may need to rely on the BerkeleyDB tool suite for troubleshooting. Other than this, you will not need to know about any BerkeleyDB particularities.

While HyperGraphDB aims to be a very general and flexible storage framework in which it is in principle possible to represents various formalisms for organizing data, it can be used "out of the box" as an object-oriented Java database. To ease the introduction into its APIs, the present tutorial focuses mostly on that particular aspect.

The following short sections go through the basics of installing HyperGraphDB, creating a new database instance and playing with fundamental operations such as storing and retrieving information.

The pages are written mostly in tutorial style. And while there are no accompanying sources with all the examples, you are encouraged to copy and paste code from the tutorial and run it and play with it.

  1. [Installing HyperGraphDB](IntroInstall.md)
  1. [Creating a Database](IntroDBCreate.md)
  1. [Storing Data](IntroStoreData.md)
  1. [Atom Handles](IntroHGHandles.md)
  1. [Atom Types](IntroHGTypes.md)
  1. [Querying](IntroQuerying.md)
  1. [Graph Traversals](IntroGraphTraversals.md)
  1. [Indexing](IndicesHowto.md)
  1. [Transaction Essentials](IntroTransactions.md)

[Start Here >>](IntroInstall.md)