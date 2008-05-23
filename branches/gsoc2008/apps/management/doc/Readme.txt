As soon as several things must coexist and cooperate in function, some form of management is in order. The concept of a HyperGraphDB application refers to a set of HyperGraph atoms (type, data, event handlers etc.) that form a coherent whole, for example by implementing some standard (e.g. RDF) or integrating a large data corpus (WordNet). This set of atoms and APIs is developed independently of HyperGraphDB and it can be used as a single component on a need-by-need basis. Moreover, the decoupling and mixing and matching idea leads, as usually, to the need to deal with dependencies where a (particular version of an) application needs (a particular version of) another application in order to function properly. Hence, management of HyperGraph application must at a minimum provide:

1) Lifecycle handling - download, installation, remove, update.
2) Versioning and dependency handling between application.

Those are the two main roles fulfilled by this special "management" module. HyperGraphDB applications may leverage those facilities by implementing appopriate interfaces from the management module and exposing them through the standard Java service discovery mechanism:

- The management module will look for a file called org.hypergraphdb.app.management.HGApplication under the META-INF/services directory of the archive file holding the application. If it finds one, it assumes that
the file lists extensions of the HGApplication class, one per application residing in that particular archive.

