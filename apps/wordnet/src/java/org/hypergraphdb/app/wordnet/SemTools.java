package org.hypergraphdb.app.wordnet;

import org.hypergraphdb.*;

public class SemTools
{
	private HyperGraph graph;
	
	public SemTools(HyperGraph graph)
	{
		this.graph = graph;		 
	}
	
	/**
	 * <p>Compute and return a WordNet-intrinsic information content
	 * measure for a synset. The measure is computed according to 
	 * the following formula:<br><br>
	 * <code>IC(s) = 1 - log(hypo(s) + 1)/log(total_synset_count)</code>
	 * <br><br>
	 * where <code>hypo(s)</code> is the number of hyponyms of the synset
	 * and <code>total_synset_count</code> is the total number of synsets
	 * stored for the part-of-speech of <code>synset</code>.
	 * </p>
	 * 
	 * <p>
	 * This formula was proposed in the master's thesis of Nuno Seco titled
	 * "Computational Models of Similarity in Lexical Ontologies", University
	 * College Dublin. While in general information content is calculated
	 * as -log(p(synset)) where the probability p(synset) is extracted from
	 * a corpus, the idea here is to apply the <em>principle of cognitive
	 * saliency</em> (Zavaracky - "humans created new concepts where there is 
	 * need to differentiate from what already exists"). So, the probability
	 * of the top-level concept in the taxonomy is 1, and the probability
	 * of a leaf node is 1/total_synset_count and then the above formula follows.
	 * </p>
	 * 
	 * <p>
	 * To derive hypo(synset), for nouns all variants of the Isa relation are used
	 * while for verbs the Entails relation is used. Adjectives and adverbs are
	 * not organized in a taxonomical hierarchy and therefore for them this
	 * measure is meaningless and the method will return 0.
	 * </p>
	 * @param synset
	 * @return The information content of the synset concept for nouns and verbs, and
	 * 0 for all other parts of speech.
	 */
	public double getInformationContent(HGHandle synset)
	{
		return 0;
	}
	
	public HGHandle getLeastCommonSubsumer(HGHandle synset1, HGHandle synset2)
	{
		return null;
	}
	
	public double getPathSimilarity(HGHandle s1, HGHandle s2)
	{
		return 0.0;
	}
	
	public double getWuPalmerSimilarity(HGHandle s1, HGHandle s2)
	{
		return 0.0;
	}
	
	public double getLeacockChodorowSimilarity(HGHandle s1, HGHandle s2)
	{
		return 0.0;
	}
}