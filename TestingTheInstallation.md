# Testing the Installation #

Now that you have HyperGraphDB installed, you will want to do some basic testing to make sure all the
relevant files are located properly in your environment. We will build a small application that tests some basic functionality, and from this you can verify that you have HyperGraphDB installed properly, as well as get a flavor of how HyperGraphDB works.

The rest of this topic assumes that you have read the [Installation instructions](IntroInstall.md), and that you have configured your development environment to find the required HyperGraphDB JAR files. Don't forget that HyperGraphDB uses Berkeley DB's JNI libraries, so you will also have to configure your development environment to locate them (in addition to the JAR files)<sup>1</sup>.

## Hello, HyperGraphDB ##

Let's begin simply by creating a skeleton class for our first application.

```
/*
 * HyperGraphDB Test Program.
 */

package hgdbtest;

import org.hypergraphdb.*;

/**
 *
 * @author punisher
 */
public class Main {

    static final String dbLocation="./HGTestDB";

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

    }

}
```

The salient points in this snippet are the `import` statement for the `org.hypergraphdb` package, and
the static variable we will use for the location of our database.

The `org.hypergraphdb` package contains most of the classes and interfaces you will work with as you write your application. Of immediate interest are the `HyperGraph`, and the `HGEnvironment` classes, as we'll see in the next section.

The `dbLocation` string designates the directory where the database files will live. This is the name of a _directory_, and not the name of an individual database file. In this case
HyperGraphDB (more specifically, Berkeley DB) will create the directory relative to the current
working directory from where we run the little program.

Note that if you are using an IDE such as NetBeans, the current working directory is the directory where the project is located.

## Creating a Database ##

There are two ways to create a HyperGraphDB database. The first way requires you to manage the database handles yourself. The second way creates an **environment**, where HyperGraphDB manages the handles. When you need to perform an operation, you can request the handle by name, and HyperGraphDB will look up that name in the environment and give you back the associated handle. Creating an environment is the perferred way to manage your database connections. As your application grows, it will likely need to manage multiple databases, and the environment gives you a clean way to do that.

But first let's start from the ground up and look at the case of creating a single database instance.

We will add the following code to our `main` method:

```
        HyperGraph graph = null;

        try {
            graph = new HyperGraph(dbLocation);
        }
        catch(Throwable t) {
            t.printStackTrace();
        }
        finally {
            graph.close();
        }
```

## Adding, Removing and Updating Graph Nodes ##

From the HyperGraphDB perspective, everything you put into the database
is one of two things: A **node**, or a **link**. A node is simply some object
(a Java object reference, or a primitive datatype) that does not
represent a relationship. A link is the HyperGraphDB term for a graph
edge, or arrow. A link joins two or more nodes, and represents some
relationship between them. Your application determines the nature of
the relationship.

Everything in the HyperGraphDB database, regardless of whether
it is a node or a link, is called an **atom**. Note that since a link
is also an atom, links too can also have relationships (i.e. other links)
attached to them.

These two points about links in HyperGraphDB are important and bear repeating:

  1. Links (edges) "point to" an arbitrary number of elements instead of just two as in regular graphs.
  1. Links can be pointed to by other links as well.

You can easily add a reference to a Java object to a HyperGraphDB database. The next code
snippet adds a Java String, an object of type Book, and an array of
the primitive type double. These objects represent data, not a
relationship, so they are nodes.

```
      HyperGraph graph = HGEnvironment(dbLocation);
      HGHandle   stringHandle, bookandle, arrayHandle;

      String x = "Hello World";
      stringHandle  = graph.add(x);

      Book mybook = new Book("Critique of Pure Reason", "E. Kant");
      bookHandle  = graph.add(mybook);

      arrayHandle = graph.add(new double [] {0.9, 0.1, 4.3434});
```

HyperGraphDB stores each atom (nodes and links), and you use the APIs
to reconstruct them again later, through queries, traversals, and so
on. The next snippet demonstrates updating and removing the nodes we
just added.

```
      // myBook was created and previously added to the database.
      // Now, we will update one of the existing object's attributes:
      mybook.setYearPublished(1988);
      graph.update(mybook);

      // ...

      // Now, we need to delete the object from the database.
      graph.remove(bookHandle);
```

Note that the `HGHandle` is needed to remove the object. You can also
replace the object identified by that handle with a completely new object:

```
      graph.replace(bookHandle, new Book(....));
```

## Creating a Graph: Adding Links ##

The nodes we added in the previous section do not constitute a very
intersting graph. All we have so far are simply objects in the
database, with no relationships between them. Now we are going to see
some short examples of adding links between nodes.

In terms of the HyperGraphDB API, links are objects that implement the
`HGLink` interface. To store a link between two entities (i.e. atoms,
recall that a link is also an atom), you add a new atom that is an
instance of the `HGLink` interface.

The HyperGraphDB library contains some default implementations
of the `HGLink` interface:

  * The [HGPlainLink](http://www.kobrix.com/javadocs/hgcore/org/hypergraphdb/HGPlainLink.html) class is a link implementation that carries no additional information with. It contains no additional data and its type is general with no special meaning.
  * The [HGValueLink](http://www.kobrix.com/javadocs/hgcore/org/hypergraphdb/HGValueLink.html) class is similar to `HGPlainLink`, but it allows you to embed an arbitrary Java object in it as "payload".
  * The [HGRel](http://www.kobrix.com/javadocs/hgcore/org/hypergraphdb/atom/HGRel.html) class is a labeled link implementation with type constraints to its target set (i.e. its arguments). Use of this type of links is documented in [this topic](RefLabeledRelations.md).

Here is an example of using the `HGValueLink` to create a relationship between two Book objects. We have two books, "The Critique of Pure Reason", by E.Kant, and "Kant's System of Perspectives" by Stephen Palmquist. Palmquist's book contains a glossary of Kant's technical terms, and we wish to make a statement to that effect in our database. Furthermore, there is a web site where we can find the glossary online; we will use that as the value of the link.

The way we do it is to add nodes for the two books, and then use an `HGValueLink`

```
  HyperGraph graph = HGEnvironment(dbLocation);

  Book cpr = new Book("Critique of Pure Reason", "E. Kant");  
  Book sop = new Book("Kant's System of Perspectives", "Stephen Palmquist");  
  HGHandle cprHandle = graph.add(cpr);
  HGHandle sopHandle = graph.add(sop);

  // Now link the two nodes. The URL of the glossary website is the value.
  HGValueLink link = new HGValueLink("http://www.hkbu.edu.hk/~ppp/ksp1/KSPglos.html", cprHandle, sopHandle);
```

While in general HyperGraphDB strives for a minimal API intrusiveness, implementing meaningful representations is best done by defining your own `HGLink` implementations. Other predefined links with specific semantics, some of which are used by HyperGraphDB itself, can be found in the [atom package](http://www.kobrix.com/javadocs/hgcore/org/hypergraphdb/atom/index.html).

## Summary ##

This section has presented some short code snippets to help you ensure you have HyperGraphDB installed correctly, and to get you started with some basic terms and APIs of the library. These concepts and APIs will be presented in much more detail in later chapters.

For now, we leave you with a few more terms you will encounter frequently when working with HyperGraphDB:

  * **arity**: The number of atoms a link points to. Nodes have an arity of zero.
  * **incidence set**: The set of all links pointing to a given atom.
  * **target set**: The set of atoms a given link points to.

If _L_ is a link pointing to the atom _A_, we say that _L_ is **incident** to _A_ and that _A_ is a **target** of _L_.


---

<sup>1</sup>Usually this amounts to adding the `-Djava.library.path` setting to the JVM options your IDE  uses when it runs your program.