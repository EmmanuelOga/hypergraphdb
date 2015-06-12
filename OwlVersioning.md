# Introduction #

Here you will find documentation on the distributed versioning implementation of OWL ontologies. This should familiar to any programmer with experience with a DVCS (http://en.wikipedia.org/wiki/Distributed_revision_control).

We do have one generalization of how things are usually done in the DVCS word in that we want to support what one might call _composite versioning_ where one create an aggregate of several versioned objects and tracks versions of the aggregate as a separate entity. More on that below.

# Concepts #

The ontology is set of axioms. Modifications to an ontology amount to adding and removing axioms from that set. Or adding/removing imports or prefixes or annotations, which are extra-logical things that are simple structured elements nevertheless. A list of such modifications is a **ChangeSet**. Note that even though we call it a set it's actually a list. That's because the order of changes matters. Given an ontology in some initial set, one can apply a `ChangeSet` to create a new **Revision**, which essentially represents a version of the versioned entity. The term revision is used in version management because a revision also represents the act of creating a version. So the word "revision" has this double meaning of sorts in this context.

Revisions are connected to each in a parent-child relationship. They form a DAG (directed acyclic graph).

Change sets also form a DAG, but because of composition they don't necessarily result in a revision. For example, when a creating a new revision of a project comprised of several modules, each module will have a change set committed, but it will be the project that will acquire a new revision, not the modules. Modules can be versioned independently.