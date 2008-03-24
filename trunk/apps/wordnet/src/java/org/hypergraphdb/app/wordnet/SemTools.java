package org.hypergraphdb.app.wordnet;

import java.util.*;

import org.hypergraphdb.*;
import org.hypergraphdb.HGQuery.hg;
import org.hypergraphdb.query.impl.TraversalBasedQuery;
import org.hypergraphdb.util.Pair;
import org.hypergraphdb.algorithms.HGBreadthFirstTraversal;
import org.hypergraphdb.algorithms.HGDepthFirstTraversal;
import org.hypergraphdb.algorithms.HGTraversal;
import org.hypergraphdb.app.wordnet.data.*;

public class SemTools
{
	private HyperGraph graph;
	private WNGraph wn;
	
	private long countNounDescendents(HGHandle s)
	{
		return hg.count(new TraversalBasedQuery(wn.relatedBreadthFirst(s, InstanceOf.class, false))) +
			   hg.count(new TraversalBasedQuery(wn.relatedBreadthFirst(s, KindOf.class, false)));
	}

	private long countVerbDescendents(HGHandle s)
	{
		return hg.count(new TraversalBasedQuery(wn.relatedBreadthFirst(s, Entails.class, false)));
	}
	
	public SemTools(HyperGraph graph)
	{
		this.graph = graph;
		this.wn = new WNGraph(graph);
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
		SynsetLink s = graph.get(synset);
		if (s instanceof NounSynsetLink)
		{
			double hypo = countNounDescendents(synset);
			double total = hg.count(graph, hg.type(NounSynsetLink.class));
			return 1.0 - Math.log(hypo + 1.0)/Math.log(total);
		}
		else if (s instanceof VerbSynsetLink)
		{
			double hypo = countVerbDescendents(synset);
			double total = hg.count(graph, hg.type(VerbSynsetLink.class));
			return 1.0 - Math.log(hypo + 1.0)/Math.log(total);
		}
		else			
			return 0;
	}
	
	public Set<HGHandle> getAllLeastCommonSubsumers(HGHandle synset1, HGHandle synset2)
	{
		Set<HGHandle> result = new HashSet<HGHandle>();
		
		if (synset1.equals(synset2))
		{
			result.add(synset1);
			return result;
		}
		
		HGTraversal t1 = new HGBreadthFirstTraversal(synset1, wn.isaRelatedGenerator(false, true));
		HGTraversal t2 = new HGBreadthFirstTraversal(synset2, wn.isaRelatedGenerator(false, true));
		
		while (t1.hasNext() && t2.hasNext())
		{
			Pair<HGHandle, HGHandle> x = t1.next();
			Pair<HGHandle, HGHandle> y = t2.next();
			
			if (t1.isVisited(y.getSecond()))
				result.add(y.getSecond());
			else if (t2.isVisited(x.getSecond()))
				result.add(x.getSecond());
		}
		
		return result;
	}
	
	public HGHandle getLeastCommonSubsumer(HGHandle synset1, HGHandle synset2)
	{
		if (synset1.equals(synset2))
			return synset1;
		
		// The process here goes up the chains
		// of parents in the taxonomy DAG for both synsets and stops as soon as
		// a parent from one traversal is found in the other. Going breadth-first
		// ensures that the closest common parent is found indeed.
		
		HGTraversal t1 = new HGBreadthFirstTraversal(synset1, wn.isaRelatedGenerator(false, true));
		HGTraversal t2 = new HGBreadthFirstTraversal(synset2, wn.isaRelatedGenerator(false, true));
		
		while (true)
		{
			if (t1.hasNext())
			{
				Pair<HGHandle, HGHandle> x = t1.next();
				if (t2.isVisited(x.getSecond()))
					return x.getSecond();
			}
			else if (!t2.hasNext())
				break;
			if (t2.hasNext())
			{
				Pair<HGHandle, HGHandle> y = t2.next();			
				if (t1.isVisited(y.getSecond()))
					return y.getSecond();
			}
		}
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