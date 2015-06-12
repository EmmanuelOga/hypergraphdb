## What Are HGDB Applications? ##

HyperGraphDB Applications are application components that rely on its meta-model and storage engine to represent various domains, implement algorithms or integrate with existing systems and standards.

Essentially, those are simply collections of classes that rely on a HyperGraphDB instance to work and that are usually organized around a set of core atom types and cover some area or domain of interest. A few simple abstractions are defined in the [org.hypergraphdb.app.management ](http://www.kobrix.com/javadocs/hgapps/management/index.html) package  to support the notion of _component_ as a whole.

The [HGApplication](http://www.kobrix.com/javadocs/hgapps/management/org/hypergraphdb/app/management/HGApplication.html) class represents an application component defined by a name and a version. Implementations must implement the [PresenceLifecycle](http://www.kobrix.com/javadocs/hgapps/management/org/hypergraphdb/app/management/PresenceLifecycle.html) interface (from the same package) with methods install/uninstall/update/reset.

A top level class with utility static methods - [HGManagement](http://www.kobrix.com/javadocs/hgapps/management/org/hypergraphdb/app/management/HGManagement.html) - helps with managing applications in a HyperGraphDB instance with methods like `isInstalled`, `ensureInstalled` and `remove`.

The general patterns for `HGApplication` implementations is to create the common HGDB types and whatever atoms are needed in the install method and be able to reverse this and cleanup the all data in the uninstall method. The other two methods, `update` and `reset`, are optional and will probably only be implemented when it is straightforward to do so. Uninstalling may be difficult and a very long operation when a lot of application specific has be stored in a given database instance. It is recommended that users perform such operations offline and that implementations split transactions to an appropriate size as well as provide ability to interrupt and resume such operations.

## Installing and Using Application Components ##

All application components reside under the `apps` folds in the code repository. It is recommended that you get the code from SVN since most have 3d party dependencies whose correct versions have been committed there. Some of the stable application components have their jar files uploaded in the [HyperGraphDB download area](http://code.google.com/p/hypergraphdb/downloads/list), but the dependencies are still in SVN (if you want to avoid chasing them around the Internet).

There's one common dependency that all components share and this apps/management module described above. You must build that module and incude it in the classpath of whatever other component you are using. Use the ant `build.xml` directly under `apps` with target `management` to build that jar. Each of the components has its own ant target in the same build file. For example, to use the Sail RDF implementation, issue the following commands

```
ant management sail
```

You will get hgdbmanagement.jar and hgdbsail.jar in the current directory. Then under `apps/sail/jars`, you will find all jars from Sesame needed to run it.

Admittedly, this is a very rough guide. If you are having trouble, please write to the [HyperGraphDB Discussion Forum](http://groups.google.com/group/hypergraphdb).