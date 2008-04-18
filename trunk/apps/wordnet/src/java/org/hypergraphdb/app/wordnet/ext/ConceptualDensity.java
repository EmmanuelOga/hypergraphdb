package org.hypergraphdb.app.wordnet.ext;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGQuery.hg;
import org.hypergraphdb.algorithms.DefaultALGenerator;
import org.hypergraphdb.algorithms.HGALGenerator;
import org.hypergraphdb.algorithms.HGBreadthFirstTraversal;
import org.hypergraphdb.algorithms.HGTraversal;
import org.hypergraphdb.app.wordnet.WNGraph;
import org.hypergraphdb.app.wordnet.data.*;
import org.hypergraphdb.query.HGAtomPredicate;
import org.hypergraphdb.query.HGQueryCondition;

public class ConceptualDensity
{
	private WNGraph wn = null;
	private Map<HGHandle, Map<Pos, Set<String>>> cache = 
		Collections.synchronizedMap(new WeakHashMap<HGHandle, Map<Pos, Set<String>>>());
	
	private Pattern nounPattern = java.util.regex.Pattern.compile("([^\\s;\"][a-zA-Z\\-]+)#n");
	private Pattern verbPattern = java.util.regex.Pattern.compile("([^\\s;\"][a-zA-Z\\-]+)#v");
	private Pattern adverbPattern = java.util.regex.Pattern.compile("([^\\s;\"][a-zA-Z\\-]+)#d");
	private Pattern adjectivePattern = java.util.regex.Pattern.compile("([^\\s;\"][a-zA-Z\\-]+)#a");
	
	private HGTraversal getTraversal(HGHandle synsetHandle)
	{
		SynsetLink synset = wn.getGraph().get(synsetHandle);
		HGALGenerator generator = null;
		if (synset instanceof NounSynsetLink)
			generator = wn.isaRelatedGenerator(true, false);
		else if (synset instanceof VerbSynsetLink)
			generator = wn.isaRelatedGenerator(true, false);
		else if (synset instanceof AdjSynsetLink)
			generator = new DefaultALGenerator(wn.getGraph(), 
											   hg.or(hg.type(Similar.class), hg.type(Antonym.class)), 
											   null, true, true, false);
		else
		{
			// adverb don't have much to offer in terms of semantic relationships
			// the best we could is antonyms and also use similarity whenever
			// the adverb is derived from an adjective.
			DerivedFrom derivedFrom = 
				hg.findOne(wn.getGraph(), hg.apply(hg.targetAt(wn.getGraph(), 0), 
											hg.and(hg.type(DerivedFrom.class), hg.incident(synsetHandle))));
			HGAtomPredicate linkPredicate = hg.type(Antonym.class);
			if (derivedFrom != null)
			{
				linkPredicate  = hg.or((HGQueryCondition)linkPredicate, hg.type(Similar.class));
			}
			generator = new DefaultALGenerator(wn.getGraph(), linkPredicate, null, true, true, false);
		}
		
		return new HGBreadthFirstTraversal(synsetHandle, generator);
	}
	
	private SynsetInfo getSynsetInfo(HGHandle synsetHandle)
	{
		return hg.findOne(wn.getGraph(),hg.and(hg.type(SynsetInfo.class), 
                							   hg.incident(synsetHandle)));		
	}
	
	private void collectWords(Set<String> set, String taggedGloss, Pattern pattern)
	{
	    Matcher matcher = nounPattern.matcher(taggedGloss);    
	    while (matcher.find())
	        set.add(matcher.group(1));		
	}
	
	private Set<String> fetchWords(HGHandle synset, Pos pos)
	{
		Pattern pattern = null;
		switch (pos) 
		{ 
			case noun: pattern = nounPattern; break;
			case verb: pattern = verbPattern; break;
			case adverb: pattern = adverbPattern; break;
			default: pattern = adjectivePattern;
		}
		HashSet<String> result = new HashSet<String>();
		HGTraversal traversal = getTraversal(synset);
		HGHandle current = synset;
		while (current != null)
		{
			SynsetInfo info = getSynsetInfo(current);
			if (info != null)
				collectWords(result, (String)info.getAttributes().get("taggedGloss"), pattern);
			current = traversal.hasNext() ? traversal.next().getSecond() : null;
		}
		return result;
	}
	
	public ConceptualDensity(WNGraph wn)
	{
		this.wn = wn;
	}
	
	public Set<String> getWords(HGHandle synset, Pos pos)
	{
		Map<Pos, Set<String>> pos_sets = cache.get(synset);
		if (pos_sets == null)
		{
			pos_sets = Collections.synchronizedMap(new HashMap<Pos, Set<String>>());
			cache.put(synset, pos_sets);
		}
		Set<String> result = pos_sets.get(pos);
		if (result == null)
		{
			result = fetchWords(synset, pos);
			pos_sets.put(pos, result);
		}
		return result;
	}
	
	/**
	 * <p>
	 * Calculates the conceptual density between two synsets. TODO: doc more...
	 * </p>
	 * 
	 * @param s1 
	 * @param s2
	 * @return
	 */
	public double calculate(HGHandle s1, HGHandle s2, Pos pos)
	{
		Set<String> X = getWords(s1, pos);
		Set<String> Y = getWords(s2, pos);
		double count = 0;
		if (X.size() < Y.size())
			{ for (String word : X) if (Y.contains(word)) count++; }
		else
			{ for (String word : Y) if (X.contains(word)) count++; }
		return count/(Math.log(X.size())*Math.log(Y.size())); 
	}	
}
