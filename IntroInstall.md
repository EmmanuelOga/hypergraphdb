# Installation and Deployment #

This page describes the installation of HyperGraphDB from an official release package. If you would like to build and install the system directly from source, please refer to the [Installing from Source](CompilationAndDeployment.md) page. Note that every official release comes with source code, so it is always possible to examine the code, experiment, and apply patches if need be.

HyperGraphDB provides an embedded database library that you can use to create and manage databases in your application. With HyperGraphDB, there is no client/server communication layer, however, it is possible to create distributed databases with additional peer-to-peer (P2P) libraries. HyperGraphDB manages databases through its underlying storage mechanism, specifically Oracle's Berkeley DB (standard edition). HyperGraphDB supports both Berkeley DB's native C interface as well as the Java Edition (JE). The default storage implementation is JE, which performs even better than the native for small to moderate datasets. If you would like to use the native version, then Berkeley DB's native (JNI) library must be included in the system path of your application.

# Getting HyperGraphDB with Maven #

As of version 1.2 all HyperGraphDB and dependencies are managed in a Maven repository located at http://www.hypergraphdb.org/maven. To links HyperGraphDB to your project through Maven:

Add the HyperGraphDB repository to your pom:
```
 <repositories>
    <repository>
      <id>hypergraphdb</id>
      <url>http://hypergraphdb.org/maven</url>
    </repository>
  </repositories>
```
Add the relevant dependencies (minimal set of components with BerkeleyDB JE storage):
```
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

# Downloading and Installing HyperGraphDB #

All official release packages are located at the [Google Code downloads area](http://code.google.com/p/hypergraphdb/downloads/list). The latest build is linked to at [HyperGraphDB download page](http://www.hypergraphdb.org/download).

HyperGraphDB releases are distributed in compressed archive files  (`.tar.gz` for Unix platforms and `.zip` for Windows). To install a distribution, just unzip the archive into a directory of your own choosing.

## Distribution Archive Content ##

The unzipped archive contains the following (where ${version} stands for the current version of this distribution):

| `readme.html`          | General information about the HyperGraphDB disribution. |
|:-----------------------|:--------------------------------------------------------|
| `LicensingInformation` | The HyperGraphDB license conditions.                    |
| `lib/hgdb-${version}.jar`     | Core HyperGraphDB library. You need a storage implementation in your classpath in addition to this jar. |
| `hgdbp2p-${version}.jar`           | Additional peer-to-peer functionality.                  |
| `hgbdbje-${version}.jar`         | Storage implementation based on BerkeleyDB Java Edition. You need the je-${jeversion}.jar in your classpath as well. |
| `hgbdbnative-${version}.jar`         | Storage implementation based on BerkeleyDB native C library. You need the db-${dbversion}.jar in your classpath as well. At runtime, you need to add the relevant native libraries, depending on your platform, to the java system library path. |
| `db-${dbversion}.jar`         | The Java bindings for BerkeleyDB native.                |
| `je-${jeversion}.jar`         | The BerkeleyDB Java Edition.                            |
| `smack-3.1.0.jar`         | P2P XMPP library needed for the HyperGraphDB peer-to-peer framework. |
| `smackx-3.1.0.jar`         | P2P XMPP library extensions needed for the HyperGraphDB peer-to-peer framework. |
| `lib/native/`              | Native libraries needed to run this version of HyperGraphDB. While most Unix environments already have Berkeley DB installed in a standard location, you are strongly advised to use the version provided in the distribution. |
| `apidocs/`             | API documentation in HTML format - each component has its own folder, hgdb for the core library (you'd mostly need to consult this), p2p for the peer-to-peer framework etc.|
| `src/`                 | The source code used to create the release package. Again each component is in its own subfolder. |
| `ThirdPartyLicensing/` | Licensing information for HyperGraphDB dependencies, notably Oracle's Berkeley DB Standard Edition. |

# Deploying Applications #

In total, the HyperGraphDB library consists of its own JAR files, Berkeley DB's `db-${dbversion}.jar` file or `je-${jeversion}`, optionally the third-party JAR files for peer-to-peer communication, and the Berkeley DB JNI library if you're using the native version. The specific JARS required by your application depend on the deployment scenario.

You tell the Java Virtual Machine where to find native libraries in one of two ways:

  * Specify the location using the `java` command line option `-Djava.library.path`. For example:<br>
<pre><code>java -Djava.library.path=$HGDB_ROOT/lib/native/$PLATFORM<br>
</code></pre>
Here, $HGDB_ROOT refers to your HyperGraphDB installation directory and $PLATFORM refers to your operating system (linux, macos or windows, or their 64bit versions).</li></ul>

<ul><li>Add the location to the <code>PATH</code> environment variable (on Windows) or the <code>LD_LIBRARY_PATH</code> environment variable (on Linux/Unix).</li></ul>

<b>NOTE for Windows users</b>:<br>
The Windows native libraries may require the installatioin of the Visual C++ runtime libraries. If you get a runtime error pertaining to DLL loading, try installing the MSVC++ redistribution package. As the location changes often, we're not providing a link, but you can easily search for it.

## Configuration ##

As an embedded database, HyperGraphDB does not require any configuration outside of a few parameters that you can set at runtime. In fact, only a directory location where data will be stored is required. However, when deploying a node within a distributed environment, you will need to extra configuration depending on your particular deployment scenario. For more information, consult the DistributedHyperGraph Creating Distributed Databases] topic.

You configure HyperGraphDB by setting parameters to a [HGConfiguration](http://www.hypergraphdb.org/docs/javadoc/org/hypergraphdb/HGConfiguration.html) instance before opening a database. One important parameter there is the storage implementation (see `setStoreImplementation` method). By default, BerkeleyDB JE is used. You can also specify a different implementation class as a command line parameter:

-Dorg.hypergraphdb.storage.HGStoreImplementation=org.hypergraphdb.storage.bdb.BDBStorageImplementation

The above will force HyperGraphDB to use the BerkeleyDB native implementation.


[Next - Creating a Database >>](IntroDBCreate.md)