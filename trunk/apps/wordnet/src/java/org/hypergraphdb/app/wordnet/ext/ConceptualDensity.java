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
import org.hypergraphdb.util.Pair;

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
		HGHandle result = hg.findOne(wn.getGraph(),hg.and(hg.type(SynsetInfo.class), 
                							   			  hg.incident(synsetHandle)));
		return result != null ? (SynsetInfo)wn.getGraph().get(result) : null;
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
	
	/**
	 * <p>
	 * Compute the conceptual densities between a given sense and all possible senses of
	 * a given word with its part of speech.
	 * </p>
	 * 
	 * <p>
	 * The resulting <code>Map</code> object contains for each possible sense of the word 
	 * <code>word</code>, the conceptual density between it and the <code>sense</code> parameter.
	 * If <code>normalize</code> is true the values calculated are normalized to sum to 1.
	 * </p>
	 * 
	 * @param sense The sense against which conceptual densities will be calculated.
	 * @param word The word whose senses are going to be examined.
	 * @param normalize Whether to normalize the results (see above for an explanation).
	 * @return The density map whose structure is described above.
	 */	
	public Map<HGHandle, Double> getDensityMapPerSense(HGHandle sense, 
													   HGHandle word, 
													   Pos wordPos, 
													   Pos densityPos,
													   boolean normalize)
	{
		Map<HGHandle, Double> result = new HashMap<HGHandle, Double>();
		double total = 0.0;
		for (HGHandle wordSense : wn.getSenses(word, wn.getSenseType(wordPos)))
		{
			double cd = calculate(sense, wordSense, densityPos);
			if (normalize) 
				total += cd;
			result.put(wordSense, cd);
		}
		if (normalize)
			for (Map.Entry<HGHandle, Double> e : result.entrySet())
				e.setValue(e.getValue() / total);
		return result;
	}
	
	/**
	 * <p>
	 * Compute the conceptual densities between all possible senses of two
	 * words, given their parts of speech and the part of speech on which to
	 * compute the density.
	 * </p>
	 * 
	 * <p>
	 * The resulting <code>Map</code> object has the following structure: the 
	 * keys are pairs of synset <code>HGHandle</code>s whose first element is a
	 * synset of the <code>x</code> parameter and whose second element a synset 
	 * of the <code>y</code> parameter. The values are pairs of doubles. When the
	 * <code>normalize</code> argument is false, the pairs will contain the same
	 * double in the first and second elements. Otherwise, the first element will
	 * contain the conceptual density normalized to the [0,1] for the sense of x and
	 * the second the conceptual density normalized for the sense of y. Normalization
	 * works by summing over all values for the senses of the "other" word. That is,
	 * a normalized value for the sense of x will be the 
	 * conceptual density between that sense of x and the corresponding sense of y, divided
	 * by the sum of all conceptual densities between that same sense of x and <strong>all</strong>
	 * senses of y. The idea is to get conditional probability semantics: given a sense of x,
	 * what is the probability that the sense of y will be such and such. We want for a fixed
	 * x (and analogously a fixed y) all those probabilities to sum to 1.
	 * </p>
	 * 
	 * @param x One of the words.
	 * @param xpos Its part of speech.
	 * @param y The other word.
	 * @param ypos And its part of speech of the word <code>y</code>.
	 * @param densityPos The part of speech on which to compute the density. That is words 
	 * in glosses of related synsets with that part of speech will be included in the
	 * density measure.
	 * @param normalize Whether to normalize the results (see above for an explanation).
	 * @return The density map whose structure is described above.
	 */
	public Map<Pair<HGHandle, HGHandle>, Pair<Double, Double>> getDensityMap(HGHandle x, 
																		     Pos xpos, 
																		     HGHandle y, 
																		     Pos ypos, 
																		     Pos densityPos,
																		     boolean normalize)
	{
		Map<Pair<HGHandle, HGHandle>, Pair<Double, Double>> result = 
			new HashMap<Pair<HGHandle, HGHandle>, Pair<Double, Double>>();
		List<HGHandle> xList = wn.getSenses(x, wn.getSenseType(xpos));
		List<HGHandle> yList = wn.getSenses(x, wn.getSenseType(ypos));
		Map<HGHandle, Double> xtotals = normalize ? new HashMap<HGHandle, Double>(yList.size()) : null;
		Map<HGHandle, Double> ytotals = normalize ? new HashMap<HGHandle, Double>(xList.size()) : null;
		for (HGHandle xsense : xList)
		{
			double ytotal = 0.0;
			for (HGHandle ysense : yList)
			{
				Pair<HGHandle, HGHandle> key = new Pair<HGHandle, HGHandle>(xsense, ysense);
				double cd = calculate(xsense, ysense, densityPos);
				if (normalize)
				{
					ytotal += cd;
					Double xtotal = xtotals.get(ysense);
					if (xtotal == null)
						xtotal = cd;
					else
						xtotal += cd;
					xtotals.put(ysense, xtotal);
				}
				result.put(key, new Pair<Double, Double>(cd, cd));
			}
			if (normalize)
				ytotals.put(xsense, ytotal);
		}
		if (normalize)
			for (Map.Entry<Pair<HGHandle, HGHandle>, Pair<Double, Double>> e : result.entrySet())
			{
				Pair<Double, Double> p = e.getValue();
				e.setValue(new Pair<Double, Double>(p.getFirst()/ytotals.get(e.getKey().getFirst()), 
												    p.getSecond()/xtotals.get(e.getKey().getSecond())));
			}
		return result;
	}	 
}