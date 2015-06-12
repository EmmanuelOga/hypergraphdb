# Introduction #

One of the predefined atom types that comes with HyperGraphDB is the HGRelType that allows you to managed labeled relationships between atoms. This is a very common case and there is absolutely nothing special in its implementation. It is provided both as an example of the notions of `atom type` and `atom type constructor`, and as a convenience.

The goal of the `HGRelType` is the support of generic semantic relationships. Relationships in HyperGraph are generally represented as links between atoms. Plain links don't tell much about the nature of the
relationship. So some sort of attributes are needed. The most simple and common case is where the relationship has a single attribute: its name. In addition, certain kind of relationships will always be between the same number of entities and between the sames types of entities. This is reflected in the new API in the org.hypergraphdb.atom package

# Details #

```
public class HGRel extends HGPlainLink

{

    public String getName()
{ ... }

}

```

HGRel represents a single instance of a relationship, relating
concrete entities. For example

```
color("my honda", green)
```

is a HGRel. Then we have the type of a relationship:

```
public class HGRelType extends HGAtomTypeBase implements HGLink
{
    public String getName()  { ... }
    public void setName(String name) { ... }

}
```

The HGRelType is a link between the _types_ of entities that the
relationship links. Thus the HGRelType of the example above would be:

```
color(CarType, ColorType)
```

Finally, the type of HGRelType is a HGRelTypeConstructor:

```
public class HGRelTypeConstructor extends HGAtomTypeBase                                                   implements HGSearchable<HGRelType, HGPersistentHandle>
{
 ....

}

```

This is a relatively simple atom type that just records the name of
the relationships as strings. It also maintains an index from names to
HGRelType instances.

This schema allows relationships to be generic and independent of
records, beans and the like. The essential properties, the name, the
arity, the type of arguments, are clearly represented in a natural
HGDB way: the HGRelTypeConstructor manages HGRelType as links between
argument types and with a single property called 'name'. In turn,
HGRelType manages particular HGRel instances as links between
entities. The 'name' property is readonly in runtime HGRel instances.

This will all be refined through a little bit of use. The intent is to
avoid creating a separate Java class for each new relationship that we
want to represent and also enforce some type safety. If it turns out
important, we could add arbitrary attributes to HGRel and manage it as
a sort of record of slots as well. But I'd rather have a different way
of handling such "heavyweight" relationships. Right now, HGRel
instances use no extra storage at all, from storage perspective they
have no more overhead than a plain link. And they a strongly typed
which should facilitates querying.