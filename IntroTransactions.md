HyperGraphDB is transactional. For performance reasons, HyperGraphDB's transactions are by default ACI, but not D. That is, they are atomic, consistent and isolated, but not durable. This means that upon failure, some of the recently committed transactions may be lost. This is generally acceptable, especially in a Java environment where JVM crashes are relatively rare. If you need durability, that's possible as well.

In addition, transactions can be nested so that sub-operations of more complex operations can fail, while the top-level transaction succeeds. Every update operation that you perform is automatically encapsulated in a transaction so most of the time you don't have to worry about it. However, when you want to perform several operations as a transactional unit, you can wrap them in a top-level transaction as shown below.

In order to alleviate the API, none of the public HyperGraphDB methods take a transaction object as a parameter. Instead, a current transaction is associated with every thread. It is theoretically possible to have multiple threads share the same current transaction, by working directly with the transactions API, but this is error prone and it should be avoided.

To wrap a long, complex operation in a transaction, the usual coding pattern works:

```
HyperGraph graph = …;

graph.getTransactionManager().beginTransaction();
try
{
    // do your work here…
    // …
    // end work unit

    graph.getTransactionManager().commit();
}
catch (Throwable t)
{
   // false means the transaction failed.
   graph.getTransactionManager().abort();
}

```

However, there is a slight caveat. HyperGraphDB handles locking conflicts leading to deadlocks by randomly aborting one of the transactions involved. Thus if there is a deadlock situation between two transactions, it will be automatically detected and one of the transactions will be aborted with a `DeadlockException` which indicates that it must be retried. Because there is no predetermined order in which deadlocked transactions are aborted, they are all guaranteed to eventually succeed if retried enough times. This approach works quite well in practice, but the above coding pattern must be modified to accommodate the retries. Such a retry loop is implemented in the HGTransactionManager.transact method which you can call like this:

```
graph.getTransactionManager().transact(new Callable<Object>() 
{
    public Object call()
    {
        // transaction unit of work
    }
}
```

A common situation is when you want a piece of code to be encapsulated in a transaction, but you'd like to "reuse" the current transaction if there is one in effect (remember transactions are bound to the current thread) or create a new transactions if there is no current one in effect. This use case is implemented in the following API:

```
graph.getTransactionManager().ensureTransaction(new Callable<Object>() 
{
    public Object call()
    {
        // transaction unit of work
    }
}
```

As mentioned above, deadlock detection is an important aspect of ensuring seamless concurrent access to the database. However, it is not a panacea against deadlocks in your application. If you are using locks on Java runtime data structures, deadlock may result due to the interplay between them and underlying database locking. Suppose you have a regular Java lock L and a database lock D. Suppose thread A acquires L and is then waiting for D while thread B acquires D and is then waiting for L. This sort of deadlock is not going to be automatically detected because L is not managed by the database. In general, you should be mindful of this sort of situation and simply avoid it. You can also use an implementation of the `ReadWriteLock` Java interface that is based on database locks: the `org.hypergraphdb.transaction.BDBTxLock` class. You need to provide a unique `byte[]` that identifies your lock. This implementation participates in deadlock detection and simplifies  somewhat mixed concurrent access to RAM structures and disk data. However, caution must be used again. First, the implementation needs the same current transaction to be in effect when locking and unlocking. Second, you must make sure that any code that is executed within a transaction and that modifies RAM data structures is reentrant and can be safely aborted and retried an indefinite number of times.

The full transaction API is documented in the `org.hypergraphdb.transaction` package.

[<< Prev - Indexing](IndicesHowto.md)