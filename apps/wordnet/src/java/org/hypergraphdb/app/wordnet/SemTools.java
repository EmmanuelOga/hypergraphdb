package org.hypergraphdb.app.wordnet;

import java.util.*;

import org.hypergraphdb.*;
import org.hypergraphdb.HGQuery.hg;
import org.hypergraphdb.query.impl.TraversalBasedQuery;
import org.hypergraphdb.util.Pair;
import org.hypergraphdb.algorithms.HGBreadthFirstTraversal;
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

		// We proceed in a way similar to the algorithm that finds a single LCA (least
		// common ancestor) implemented in 'getLeastCommonSubsumer'. However, instead
		// of stopping at the first one that is found, we continue until all roots of the
		// hyponymy DAG are reached.
		// 
		// We must be careful to include only LCAs in the
		// result set. So for any ancestor that we find during the traversal, we must make
		// sure that it doesn't have a descendant which is LCA of synset1 and synset2. Depending
		// on the structure of the sub-graph that we are examining we may reach an ancestor
		// 'X' that is not an LCA but before we have examined the path which contains a the actual
		// LCA. The key observation in the following algorithm is that we will eventually 
		// examine that path at which point we can invalidate X as an LCA and remove it from
		// the result set. 
		//
		// All atoms reachable from an already found ancestor are ancestors also and could
		// potentially be reached from other paths and added as candidate LCAs. So we need
		// to keep track of all of them. 
		// 
		// Consider the set 'A' of all common ancestors to synset1 and synset2. This set
		// can be partitioned into the set 'R' of all LCAs (the result set) and the set 'I' 
		// of all the other ancestors which are not LCAs (the "invalid" set). So the algorithm
		// here maintains both the sets R and I in such a way that at the end
		// R contains only LCAs. R is represented by the variable 'result' and I by the variable
		// 'invalid'. When a common ancestor X is found below, the two set are updated as follows:
		//
		// if X's sibling in the Isa link is in I or R then
		// 		add X to I
		//      if X is in R, remove from there
		// else if X itself is not in I or R then
		//	    add X to R
		//
		// Essentially, the first time an atom is found to be an ancestor, we add it to the
		// result set tentatively. We then keep adding its own ancestors to the set of invalid
		// ones. And as soon as an atom is found to be a parent of something already examined
		// it is added as an invalid ancestors and removed as a candidate of the result set.
		// Maybe to much explanation for something not so complicated, but anyway...
		
		Set<HGHandle> invalid = new HashSet<HGHandle>();
		
		HGTraversal t1 = new HGBreadthFirstTraversal(synset1, wn.isaRelatedGenerator(false, true));
		HGTraversal t2 = new HGBreadthFirstTraversal(synset2, wn.isaRelatedGenerator(false, true));
		
		while (true)
		{
			if (t1.hasNext())
			{
				Pair<HGHandle, HGHandle> x = t1.next();
				if (t2.isVisited(x.getSecond()))
				{
					HGHandle sibling = ((HGLink)graph.getHandle(x.getFirst())).getTargetAt(0);
					if (invalid.contains(sibling) || result.contains(sibling))
					{
						invalid.add(x.getSecond());
						result.remove(x.getSecond());
					}
					else if (!invalid.contains(x.getSecond()) && !result.contains(x.getSecond()))
						result.add(x.getSecond());
				}
			}
			else if (!t2.hasNext())
				break;
			if (t2.hasNext())
			{
				Pair<HGHandle, HGHandle> x = t2.next();			
				if (t1.isVisited(x.getSecond()))
				{
					HGHandle sibling = ((HGLink)graph.getHandle(x.getFirst())).getTargetAt(0);
					if (invalid.contains(sibling) || result.contains(sibling))
					{
						invalid.add(x.getSecond());
						result.remove(x.getSecond());
					}
					else if (!invalid.contains(x.getSecond()) && !result.contains(x.getSecond()))
						result.add(x.getSecond());					
				}
			}
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