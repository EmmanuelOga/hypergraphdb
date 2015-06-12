# **UNDER CONSTRUCTION** #


# Real World Example #

The following code should be treated as a real world example, by providing the source to the Book example of the official tutorial. It demonstrates how a hypergraphdb can be created and how objects of type Book will be added to the database. Further it shows how a query is done.

The code below shows the JavaBean Book:
```
public class Book {

    private String title;
    private String author;
   
    public Book() {
    }

    public Book(String title, String author) {
        this.author = author;
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

}
```

The code below shows the main class:
```
import java.util.List;
import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGQuery.hg;
import org.hypergraphdb.HyperGraph;


public class HGDBSimpleSample {
    public static void main(String[] args) {
        String databaseLocation = "/path/to/hypergraphdb";
        HyperGraph graph = null;
        try {
            graph = new HyperGraph(databaseLocation);
            Book mybook = new Book("Critique of Pure Reason", "me");
            HGHandle bookHandle = graph.add(mybook);

            List<Book> books = hg.getAll(graph, hg.and(hg.type(Book.class), hg.eq("author", "me")));
            for (Book book : books)
                System.out.println(book.getTitle());
        } catch (Throwable t) {
            t.printStackTrace();
        } finally {
            graph.close();
        }
    }
}
```


planned:
compare
1) Typical Java approach - entities.
Person: firstname, lastname, birthday (, birthplace)
Author: person, books
Book: title, year, >=1 authors of type Author, references(>=1 other books)
Section:....
Library: collection of books

2) Typical "Property graph" approach....

3) Typical HGDB approach - keep entities as minimal as possible, and encode information as much as possible in terms of links
Person: firstname, lastname, birthday (, birthplace)
Book: title
Links: authorOf(Person,books...)