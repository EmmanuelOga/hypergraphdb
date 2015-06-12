## What is RDF? ##

The Resource Description Framework (RDF) is wide spread W3C standard for semantic annotations of web resources. More can be found at the [W3C RDF Home Page](http://www.w3.org/RDF/) or at the [RDF Wikipedia Page](http://en.wikipedia.org/wiki/Resource_Description_Framework)


## What is Sesame? ##

Sesame is an open-source framework and API for the implementation of RDF storage engines. More can be found at http://www.openrdf.org. Sesame define several related APIs

## Implementation ##

The implementation is relatively straightforward and it follows the Sail API closely. HyperGraphDB acts as a quad-store (in math lingo, this is called a 4-uniform hypergraph) and thus contextual RDF graphs are implemented.

Since the XML Schema component is incomplete, the implementation doesn't use it, though that would be highly desirable in the future.

The component is located under apps/sail in the code repository. All its dependencies (including supported version of Sesame) and test cases can be found there as well.