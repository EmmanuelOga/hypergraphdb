## A HyperGraphDB database is an instance of the HyperGraph class ##

HyperGraphDB manages storage as a set of files in a directory. To create a new database, you need to designate a directory that will hold the data and write some Java code that creates and initializes a database instance in that directory. Here's an example:

```
import org.hypergraphdb.*; // top-level API classes are in this package

public class HGDBCreateSample
{ 
    public static void main(String [] args)
    {
        String databaseLocation = args[0];
        HyperGraph graph; 
     	// ...
        try
	{
            graph = new HyperGraph(databaseLocation);
        }
        catch (Throwable t)
        {
            t.printStackTrace();
        }
        finally
        {
            graph.close();
        }
    }
}

```

As you can see, creating a database amounts to creating a new `HyperGraph` instance. If the database does not exist, it will be created. If it does exist, it will be opened. So the same code is used to create or open a database.

As an opened database holds operating systems resources open, it is wise to make sure it is closed in a finally block.  It is also very important to properly close a database in order to avoid any data loss or corruption of the underlying low-level storage. HyperGraphDB may throw exceptions, but very few of the API methods throw checked exceptions. Usually the exception thrown will be a `HGException`, possibly wrapping some underlying cause.

## Prefer the HGEnvironment for Managing Database Instances ##

The [HyperGraph class](http://www.kobrix.com/javadocs/hgcore/org/hypergraphdb/HyperGraph.html) in the code above is the main entry point into the database API. It represents a single database. While creating/opening a database by calling the `HyperGraph` constructor is a valid approach, it may be preferable to rely on the [HGEnvironment class](http://www.kobrix.com/javadocs/hgcore/org/hypergraphdb/HGEnvironment.html) for such high-level operations. Opening a database is instead done thus:

```
    ....
    HyperGraph graph = HGEnvironment.get(databaseLocation);
    ....
```

The main different between a call to `HGEnvironment.get` and a call to `new HyperGraph` is that the former will return an already opened database instance at that location. The `HGEnvironment` class maintains a static map of all databases currently open. If you want an "open if exists, otherwise throw an exception" behavior, `HGEnvironment` provides it through the `getExistingOnly` method.

## Closing Databases ##

**Note on closing databases** - it is generally recommended that you close a HyperGraph instance as soon as you're done with it. However, in many cases databases remain open for the lifetime of an application. To ensure proper closing in such cases, you only need to make sure that the application exits gracefully. The `HGEnvironment` registers a shutdown hook with the Java Virtual Machine to properly close all databases that are still open. A slight disadvantage is that sometimes exiting your application may take longer than usual because the system may be flushing caches and writing some remaining transactions to disk.

## Configuration Options ##

It is possible to configure several of the runtime properties of a database instance when you open it. Some of those properties have to do with how the database will behave during the entire session, others affect only the startup process.

Configuration options are specified by create an instance of the [HGConfiguration.class](http://www.kobrix.com/javadocs/hgcore/org/hypergraphdb/HGConfiguration.html) class and passing it as an extra parameter to the `HGEnvironment.get` method:

```
HGConfiguration config = new HGConfiguration();
config.setTransactional(false);
config.setSkipOpenedEvent(true);
HyperGraph graph = HGEnvironment.get(location, config);
```

The above opens (or creates if none exists at that location) a database without triggering the predefined [HGOpenedEvent](http://www.kobrix.com/javadocs/hgcore/org/hypergraphdb/event/HGOpenedEvent.html). In addition, the database will ignore any transactional directives - code that uses the transactions API, like much of the HyperGraphDB code itself, will work by using NOP stubs in place of true transactions.

Naturally, configuration options are ignored in calls to `HGEnvironment.get`  for databases that have already been opened.

**Note:** there are no creation time configuration options at this point, only runtime options which you can vary every time you open your database instance.

[<< Prev - Installing HyperGraphDB](IntroInstall.md)  [Next - Storing Data >>](IntroStoreData.md)