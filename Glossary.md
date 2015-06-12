DISCLAIMER: this page is written by a User. It is not an "official" hypergraphDB document. It is as of yet, incomplete.

# Atom #

# arity #
arity: The number of atoms a link points to. Nodes have an arity of zero.
# Composite Type #
A type that has properties, for example a Bean. A String or an Integer are not composite types.

# Dimension #
# incidence set #
The set of all links pointing to a given atom.

# HGAtomType #
Java classes implementing the HGAtomType interface are HypergraphDB types.
A HGAtomType must provide:
- make(...): create instances of that type, i.e. deserializes from store into a runtime object. It's therefore an object factory.
- store(..): store
- release (... ): remove from store
- subsumes(A1, A2): define subsumption relationship, aka inheritance

# HGCompositeType #
The java interface to be implemented for record-style structures.


# Link #
# Projection #
# Record - general #
"In computer science, a record (also called tuple, struct, or compound data) ... is a value that contains other values, typically in fixed number and sequence and typically indexed by names. The elements of records are usually called fields or members." (wikipedia)

# Record - hypergraphDB specific #
"Record-style structures with named parts are so common that we have defined an abstract interface for them called HGCompositeT ype that views complex values as multidimensional structures where each dimension is identified by
a name and has an associated HGP rojection implementation which is able to
manipulate a value along that dimension." [paper](http://www.hypergraphdb.org/docs/hypergraphdb.pdf)



# RecordType #
# RecordTypeConstructor #
# Slot #
# target set #
The set of atoms a given link points to.

# Type -general #
“...plugging instructions. A term of a given type T is both something that can be plugged somewhere as well as a plug with free, typed variable" (Jean-Yves Girard)

# Type - HypergraphDB-specific #
  1. A type is an atom capable of storing, constructing and removing runtime
representations of its instances to and from the primitive storage layer.
> 2) A type is capable, given two of its instances, to tell whether one of them can
be substituted for the other (subsumption relation). [paper](http://www.hypergraphdb.org/docs/hypergraphdb.pdf)

The java class defining an hypergraphDB type must implement the HGAtomType interface.

# Type Constructor #