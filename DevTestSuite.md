# Introduction #

The test suite is written using TestNG (testng.org), which is a bit of a better jUnit though it suffers from the same verbosity problem, but it's a well-known and popular framework.

Because of Maven peculiarities, there are two test sub-projects:

  * `test` which contains common test related utility classes, and data (in the form of Java beans and datasets) to use in other tests.
  * `testcore` which contains the test suite of the core package.

# Running the Core Test Suite #

First make sure the version in the POM files are all in synch. You need to build core, the default storage engine (BerkelyDB Java Edition) and the `test` package:

```
cd hypergraphdb/core
mvn install
cd ../test
mvn install
cd ../storage/bdb-je
mvn install
cd ../../testcore
mvn test
```

If you want to run with another storage engine, you have to first modify your local copy of pom.xml to comment the default dependency on `hgbdbje` and declare the other storage engine as a dependency. For BerkeleyDB native C implementation that would be:

```
    <dependency>
      <groupId>org.hypergraphdb</groupId>
      <artifactId>hgbdbnative</artifactId>
      <version>1.3-SNAPSHOT</version>
      <scope>test</scope>
    </dependency>
    <dependency>
      <groupId>org.hypergraphdb</groupId>
      <artifactId>hgbdbnative</artifactId>
      <version>1.3-SNAPSHOT</version>
      <type>test-jar</type>
      <scope>test</scope>
    </dependency>

```

Also, replace the last line `mvn test` above by:

```
mvn -Dorg.hypergraphdb.storage.HGStoreImplementation=org.hypergraphdb.storage.bdb.BDBStorageImplementation test

```

In general you can pass system properties with the JVM -D flag like this. However, specifying the native library path in the mvn command line doesn't work because...well...it's one of the many issues with mvn. Instead the native path is specified in the top pom.xml so it's available for all sub-projects if needed:

```
    <configuration>
      <forkMode>always</forkMode>
      <argLine>-Djava.library.path=${project.build.directory}/lib</argLine>        
    </configuration>
```

The default location is thus 'target/lib' which means you have to manually copy the native BDB libraries there. You can also change the location in your working copy of the pom.xml.