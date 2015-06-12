# Installing from Source Code #

The HyperGraphDB source code resides in the [Subversion repository](http://code.google.com/p/hypergraphdb/source/checkout) and releases are structured following standard Subversion conventions:

  * The main branch containing latest development code can be found under `trunk`.
  * Each release can be found under `tags`.

HyperGraphDB consists of a core component implementing the nuts and bolts of the database itself, together with several example applications that use HyperGraphDB in different domains. Some of the applications have dependencies on other applications, and all have a dependency on the `management` component.

## Source Code Directory Structure ##

Source code is organized in the following top-level directories:

| `apps` | All HyperGraphDB application modules. There is one directory per application module under `apps`. |
|:-------|:--------------------------------------------------------------------------------------------------|
| `core` | The core of HyperGraphDB, including the distributed database functionality.                       |
| `viewer` | A component for visualization of HyperGraphDB graphs.                                             |
| `test` | Unit tests, mainly for the core component.                                                        |

The directory structure under `core`, `viewer`, and each application module roughly follows this pattern:

| `src/` | The source code. Sub-divided under `java`, possibly `config` and possibly other languages. |
|:-------|:-------------------------------------------------------------------------------------------|
| `jars/` | Third-party Java library dependencies for that particular component.                       |
| `etc/` | Optional, self-explanatory directories depending on the component.                         |


# Compiling the Source #

HyperGraphDB uses the [ANT build system](http://ant.apache.org). You can compile the entire package, or individual components with the top-level ant script. You can also compile individual components from their own home directories by running their respective ant scripts.

**Tip**: Use the command `ant -p` to list all available targets for a project script.

## Compiling the HyperGraphDB Core ##

There are a number of different targets that can be built from `core`, depending on whether you will use HyperGraphDB as a distributed database, or simply as an embedded database. The following table describes the three targets used to build HyperGraphDB for deployment:

| **Target** | **Result** | **Contents** |
|:-----------|:-----------|:-------------|
| `full-jar` | `hgdbfull.jar` | Contains both the database management and the distributed version code. |
| `core-jar` | `hypergraphdb.jar` | Contains just the embedded database version. |
| `peer-jar` | `hgpeer.jar` | Contains just the peer/distributed version related code. |

The `full-jar` target merges `hypergraphdb.jar` with `hgpeer.jar` to give you all the functionality you need to create both local and distributed databases. The code for `hgpeer.jar` is contained entirely in the `org.hypergraphdb.peer` package.

## Compiling the Application Modules ##

Each application module is essentially contained in one `.jar` file. The `build.xml` file in the `apps` directory contains the target for each respective application.  The next table describes each of these targets:

| **Target** | **Description** |
|:-----------|:----------------|
| `loadWordNet` |  Loads WordNet into a HyperGraphDB instance. |
| `management` |  Builds the HyperGraphDB Management JAR. |
| `prolog`             | Builds the tuProlog JAR.  |
|  `sail`                 | Builds the HyperGraphDB Sail JAR, for use with the [http://www.openrdf.org Seseme RDF framework. |
| `tm`       |     Builds the HyperGraphDB TopicMaps JAR |
| `wordnet`   |   Builds the HyperGraphDB WordNet Jar |
| `xsd`      |      Build HyperGraphDB XSD JAR |


## Compiling with GCJ ##

The following has only been tested on Ubuntu 8.10, but should work on any system where GCJ is installed.

First, compile the Berkeley DB Java interface with GCJ as follows. In the Berkeley DB distribution top folder run:
```
gcj -fjni -o dbjava.o -c ``find java/src -name '*.java' | grep -v debug``
```

This command produces an object file, `dbjava.o` that needs to be linked to HyperGraphDB. If you're compiling on a 64-bit system, add the option `-fPIC` to the above command line. Then in 'hypergraphdb/core' run:
```
gcj -shared --classpath=jars/db.jar dbjava.o hypergraphdb.jar -o hgdb.so
```

Again, add `-fPIC` if you are building on a 64-bit system. This will produce a shared library called `hgdb.so` that can be loaded from C++ applications.


# Deployment #

Deployment of HyperGraphDB and/or its application modules is described in [Deploying Applications](IntroInstall#Deploying_Applications.md).