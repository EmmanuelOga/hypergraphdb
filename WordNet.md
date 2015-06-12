Wordnet is a popular lexical database used in NLP (Natural Language Processing) research. Meaning in Wordnet is defined through _synsets_ - synonym sets that group together words with similar meaning. In addition, the database contains semantic relationships between synsets (e.g. type-token, whole-part etc.). More information can be found on
the [Wordnet home page at Princeton](http://wordnet.princeton.edu/).

The HyperGraphDB Wordnet application (called henceforth HGWN) is essentially a representation of all the information from WordNet within HyperGraphDB. This includes support for:

  * Querying the DB for a given word.
  * Querying for specific semantical or lexical relationship of a given word
  * Morphological analysys - e.g. finding base form of  a given word or collocation

HGWN does not support the so called "polysemy count" - number of uses of a given word in a specific sense as obtained by analyzing several sample texts. This feature is obsolete in WordNet too and is left unchanged since 2003.

## Representation Details ##

The basis of the representation is the [Word class](http://www.kobrix.com/javadocs/hgapps/wordnet/org/hypergraphdb/app/wordnet/data/Word.html) which represents a single
token (or a lemma). Each word is stored as an atom in HyperGraphDB. Words are then grouped into
so called <em>synsets</em> or "synonym sets" which represent senses. Thus a sense of a word is exemplified
as a set of synonyms. In HyperGraphDB, those are represented as links of type
[SynsetLink](http://www.kobrix.com/javadocs/hgapps/wordnet/org/hypergraphdb/app/wordnet/data/SynsetLink.html)
of which there's one variety for each part-of-speech. Such links have variable arity depending
on the number of synonyms in a synset. Thus, noun senses are represented as instances of `NounSynsetLink`. Those are HyperGraphDB links with arbitrary arity >= 1. The original WordNet ID is stored as the `id` property of synsets and the sense definition (a dictionary style definition in plain english) is stored as the `gloss` property.

Synsets are further related by semantic relationships (called _pointers_ in WordNet's terminology). Those are stored as hypergraph links of arity 2 between two given synsets. The super-type
of those links is [SemanticLink](http://www.kobrix.com/javadocs/hgapps/wordnet/org/hypergraphdb/app/wordnet/data/SemanticLink.html). There are many types of semantic relationships (pointers) and they differ according to the part of speech for which they are used. Refer to the various WordNet papers (links can be found on their homepage) for a description of the semantic theory behind. Most of the time semantic links are ordered (e.g. hypernymy links), but some like [Similar](http://www.kobrix.com/javadocs/hgapps/wordnet/org/hypergraphdb/app/wordnet/data/Similar.html) link are not.

This representation is rather obvious and natural given the generality of the HyperGraphDB schema.

## Exceptions ##

For each part of speech (noun, adjective, verb, adverb), WordNet contains a list of exceptional forms of the root lexeme. For nouns, this is usually the irregular plural forms, for adjectives and adverbs irregular superlatives, for verbs irregular conjugations. This is useful in morphological analysis where the normal rules for inflexion do not apply. Exceptions are represented in HGWN as links of type [ExcLink](http://www.kobrix.com/javadocs/hgapps/wordnet/org/hypergraphdb/app/wordnet/data/ExcLink.html), specifically its four sub types, one for each part of speech. The first target of an exception link is the root word and the other targets are its exceptional forms. The links have an arity >= 2. Both root and exceptional forms are stored as lexemes in HyperGraphDB (i.e. as instances of the type `Word`).

## Semantic Tools ##

HGWN offers some extras for working with lexical semantics, and in particular for implementing WSD (word sense disambiguation) algorithms.

First, there's a utility class [WNGraph](http://www.kobrix.com/javadocs/hgapps/wordnet/org/hypergraphdb/app/wordnet/WNGraph.html) which offers an API for working with WordNet in a underlying HyperGraphDB instance. In addition, the [SemTools](http://www.kobrix.com/javadocs/hgapps/wordnet/org/hypergraphdb/app/wordnet/SemTools.html) offers ready-made implementation of several word sense similarity measured commonly found in the computational linguistics literature.

Finally, the [WNStat](http://www.kobrix.com/javadocs/hgapps/wordnet/org/hypergraphdb/app/wordnet/WNStat.html) is simple, generic representation of some global information about the WordNet graph that is computationally intensive to calculate. Some of the predefined such `WNStats'` are the depths of the noun and verb synset hierarchies in WordNet.

## Loading WordNet in a HyperGraphDB ##

The JWNL (Java WordNet Library) - http://sourceforge.net/projects/jwordnet is used to load the WordNet database files. It is an open-source Java API for accessing WordNet which provides API-level access to WordNet data. The configuration files needed by that library are maintained in [org.hypergraph.app.wordnet.configuration](http://www.kobrix.com/javadocs/hgapps/wordnet/org/hypergraphdb/app/wordnet/configuration). They are only needed when Wordnet is initially loaded into HyperGraphDB. The JWNL library is also needed only for loading the data. Once the data is in the HyperGraphDB instance, the library can be removed from Java's classpath.

Here is a sample code to load WordNet data into a HGDB instance. The code disables transactions for faster processing. So if the data already in your HGDB instance is important to you, make a backup copy beforehand.

```
import org.hypergraphdb.*;
import org.hypergraphdb.app.wordnet.*;

String wordnetDB = "d:/data/graphs/wn"; // the location of your HGDB

// Disable transactions
HGConfiguration config = new HGConfiguration();
config.setTransactional(false);

// Open/Create the HGDB instance
HyperGraph graph = HGEnvironment.get(wordnetDB, config);

long startTime = System.currentTimeMillis();
HGWordNetLoader loader = new HGWordNetLoader();

// Modify the following line to point to your WordNet installation
loader.setDictionaryLocation("C:/tools/wordnet/dict");

// This should take a while, usually under 10 minutes but maybe 
// more if you have an older machine
loader.loadWordNet(graph);
long endTime = System.currentTimeMillis();
System.out.println("Completed in " + (endTime - startTime) / 1000 + " seconds.");
```