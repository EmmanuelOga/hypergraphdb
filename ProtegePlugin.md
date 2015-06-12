# What Is It? #

The HGDB Protege Plugin integrates HyperGraphDB backed storage for OWL 2.0 ontologies with the popular Protege OWL editor (http://protege.stanford.edu/). The core features supported are:

  1. Automatic ontology persistence in HyperGraphDB back-end. Import/Export from/to standard OWL textual formats. All modifications in the editor are automatically persisted in a transactional way.
  1. Versioning of ontologies at the axiom level - every axiom change is recorded and tracked and can be inspected and reverted. That is, version management support full history, commits, rollbacks, reverts etc.
  1. Team features include sharing ontologies on a common repository and managing versions following the Subversion model with a centralized location. Merging of change sets is supported, but branching is not available yet (planned for next release).
  1. A few extra improvements to Protege itself that help dealing with large-scale ontologies such as search box for long lists of individuals or properties in some dialog boxes.

# Installation and Compatibility #

Protege itself is build on [OSGI](http://www.osgi.org) and the plugin comes in the form of several bundles that you install simply by copying over in your Protege home installation directory. The downloads below are zip files that you can directly unzip into your protege home and restart protege:

  * **For Protege 4.3** - Protege HyperGraphDB Plugin version 1.8beta. This is simply an upgrade to match recent changes to the OWLAPI (version 3.4.2) and Protege 4.3. No functional changes, just some bug fixes and improvements on the codebase.

> [download http://hypergraphdb.org/files/ProtegeHGDBPlugin-1.8-beta.zip](http://hypergraphdb.org/files/ProtegeHGDBPlugin-1.8-beta.zip)

  * **For Protege 4.1** - Protege HyperGraphDB Plugin version 1.7. This is the last official stable release. Note that this version will replace some of the core Protege bundles upon installation.

> [download http://hypergraphdb.org/files/ProtegeHGDBPlugin-1.7.0.zip](http://hypergraphdb.org/files/ProtegeHGDBPlugin-1.7.0.zip)

# How to Use the Plugin #

A quick 5 minute screen cast will walk you through the menus and operations available: http://sharegov.org/protegehgdb/HGOWLScreencast3.mp4

If you have any questions, please do not hesitate to seek help on the [HyperGraphDB Google Group](https://groups.google.com/forum/?hl=en#!forum/hypergraphdb)

# Building From Source Code #

If you want to build the latest code, for example in order to take advantage of a bug fix or because you want to make some changes yourself, please follow these steps:

# Make sure you have the Maven build system installed, and of course a recent JDK.
# Get the latest HyperGraphDB code from the source control repository.
# Build the following modules using the `mvn -DskipTests=true install` command in that order: `core`, `p2p`, `storage/bdb-je`, `apps/management`, `apps/owl`, `apps/owl/gov.miamidade.owlgdb`. Those are directories under the `hypergraphdb` root folder. Just navigate to each one and issue the command.
# Under each of the above folder, you will have a jar file `target\modulename-version.jar`. Each of those jar files is an OSGI plugin on its own and you have to copy each of them to PROTEGE\_HOME/plugins directory.

Here is a table of all modules, their jar names and what each one does, assuming the version is 1.3.

| **Module** | **JAR Filename** | **Purpose** |
|:-----------|:-----------------|:------------|
| core       | hgdb-1.3.jar     | Core HyperGraphDB API |
| p2p        | hgdbp2p-1.3.jar  | Communication framework for distributed versioning|
| storage/bdb-je | hgbdbje-1.3.jar  | Storage layer for HyperGraphDB |
| apps/management | hgdbmanagement-1.3.jar | The module manager of HyperGraphDB |
| apps/owl   | hgdbowl-1.3.jar  | The OWLAPI 2.0 implementation, independent of Protege |
| apps/owl/gov.miamidade.owlgdb | hgdbprotege-1.3.jar | The Protege Plugin, Java Swing based |
