package org.hypergraphdb.app.wordnet;

import net.didion.jwnl.JWNL;
import net.didion.jwnl.JWNLException;
import net.didion.jwnl.data.*;
import net.didion.jwnl.data.list.PointerTargetNodeList;
import net.didion.jwnl.data.list.PointerTargetTree;
import net.didion.jwnl.data.relationship.AsymmetricRelationship;
import net.didion.jwnl.data.relationship.Relationship;
import net.didion.jwnl.data.relationship.RelationshipFinder;
import net.didion.jwnl.data.relationship.RelationshipList;
import net.didion.jwnl.dictionary.Dictionary;

import java.io.FileInputStream;
import java.util.*;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.handle.*;
import org.hypergraphdb.HGIndex;
import org.hypergraphdb.HGPersistentHandle;
import org.hypergraphdb.HGQuery;
import org.hypergraphdb.HGSearchResult;
import org.hypergraphdb.HGTypeSystem;
import org.hypergraphdb.HGValueLink;
import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.query.AtomTypeCondition;
import org.hypergraphdb.query.ComparisonOperator;
import org.hypergraphdb.query.HGQueryCondition;
import org.hypergraphdb.query.impl.PredicateBasedFilter;
import org.hypergraphdb.query.impl.SearchableBasedQuery;
import org.hypergraphdb.storage.DefaultIndexImpl;
import org.hypergraphdb.type.HGAtomType;
import org.hypergraphdb.type.RecordType;
import org.hypergraphdb.viewer.hg.HGUtils;

/** A class to demonstrate the functionality of the JWNL package. */
public class Main {
	// private static final String USAGE = "java Examples <properties file>";

	public static void main(String[] args) {
		/*
		 * if (args.length != 1) { System.out.println(USAGE); System.exit(-1); }
		 */

		String propsFile = "/usr/java/classlib/jwnl/file_properties.xml";// args[0];
		try {
			// initialize JWNL (this must be done before JWNL can be used)
			JWNL.initialize(new FileInputStream(propsFile));
			new Main().go();
		} catch (Exception ex) {
			ex.printStackTrace();
			System.exit(-1);
		}
	}

	private IndexWord ACCOMPLISH;

	private IndexWord DOG;

	private IndexWord CAT;

	private IndexWord FUNNY;

	private IndexWord DROLL;

	private String MORPH_PHRASE = "running-away";

	public Main() throws JWNLException {
		ACCOMPLISH = Dictionary.getInstance().getIndexWord(POS.VERB,
				"accomplish");
		DOG = Dictionary.getInstance().getIndexWord(POS.NOUN, "dog");
		CAT = Dictionary.getInstance().lookupIndexWord(POS.NOUN, "cat");
		FUNNY = Dictionary.getInstance()
				.lookupIndexWord(POS.ADJECTIVE, "funny");
		DROLL = Dictionary.getInstance()
				.lookupIndexWord(POS.ADJECTIVE, "droll");
	}

	public static HyperGraph getHG() {
		return hg;
	}

	private static HyperGraph hg;

	public void go() throws JWNLException {
		 demonstrateMorphologicalAnalysis(MORPH_PHRASE);
		 demonstrateListOperation(ACCOMPLISH);
		 demonstrateTreeOperation(DOG);
		// printFrames(DOG);
		// printFrames(ACCOMPLISH);
		
		//printPointers(Dictionary.getInstance().getIndexWord(POS.NOUN,"empress"));
        //printFrames(Dictionary.getInstance().getIndexWord(POS.VERB,"mire"));
		if(false)
			 return;
		hg = populateHGDB(ACCOMPLISH); //DOG);
		// hg = new HyperGraph("X:/kosta/hg/dog");
		// getAllTypes();
		Set nodes = new HashSet();
		Set links = new HashSet();

		HGHandle sHandle = hg.getHandle(
				hg.getTypeSystem().getAtomType(Word.class));
		if (sHandle == null)
			return;

		HGAtomType type = hg.getTypeSystem().getAtomType(Word.class);
		loadHG(hg, type, nodes, links);
		System.out
				.println("Nodes: " + nodes.size() + " Links: " + links.size());
		for (Iterator i = nodes.iterator(); i.hasNext();) {
			Object obj = hg.get((HGHandle) i.next());
			System.out.println("Nodes: " + obj.getClass().getName());
			if (obj instanceof Word)
				System.out.println("Word: " + ((Word) obj).getLemma());
		}
		// demonstrateAsymmetricRelationshipOperation(DOG, CAT);
		// demonstrateSymmetricRelationshipOperation(FUNNY, DROLL);
	}

	private static Set or_synsets = new HashSet();
	private static Set or_words = new HashSet();
	private static Set links = new HashSet();
	private HyperGraph populateHGDB(IndexWord word) throws JWNLException {
		HyperGraph hg = new HyperGraph("/home/bolerio/hgapps/db/" + word.getLemma());
		HGTypeSystem ts = hg.getTypeSystem();

		HGHandle handle = hg.getHandle(ts.getAtomType(IndexWord.class));
		ts.addAlias(hg.getPersistentHandle(handle), "IndexWord");
		handle = hg.getHandle(ts.getAtomType(Synset.class));
		ts.addAlias(hg.getPersistentHandle(handle), "Synset");
		handle = hg.getHandle(ts.getAtomType(Word.class));
		ts.addAlias(hg.getPersistentHandle(handle), "Word");
		// HGHandle h = hg.add(word, ts.getAtomType(IndexWord.class));
		Synset[] syns = word.getSenses();
		Map synsets = new HashMap();
		Map words = new HashMap();
		List targets = new LinkedList();
		for (int i = 0; i < syns.length; i++) {
			HGHandle sys_handle = makeSynsetHandle(hg, syns[i], synsets, words, true);
			targets.add(sys_handle);
		}
		HGValueLink link = new HGValueLink(word.getLemma()/* word */,
				(HGHandle[]) targets.toArray(new HGHandle[targets.size()]));
		words.put(word.getLemma(), hg.add(link));
		//or_words.add(word);
		hg.add(link);
		System.out.println("words: " + words.size() + " synsets: " + synsets.size());
		processPointers(hg, synsets, words, 1);
		hg.close();
		System.out.println("Finish - words: " + words.size() + " synsets: " + synsets.size());
		return hg;
	}
	
	private void processPointers(HyperGraph hg, Map synsets, Map words, int depth) throws JWNLException
	{
		int i = 0;
		System.out.println("processPointers: " + i);
		Set or_synsets1 = new HashSet(or_synsets);
		Set or_words1 = new HashSet(or_words);
		for(Iterator it = or_words1.iterator(); it.hasNext();)
			makePointers(hg, it.next(), synsets, words, false);  //;true);
		for(Iterator it1 = or_synsets1.iterator(); it1.hasNext();)
			makePointers(hg, it1.next(), synsets, words, false); //false);
		i++;
		if(or_synsets.size() != or_synsets1.size() ||
			or_words.size() != or_words1.size() && i<depth)
		{
			or_synsets1 = null;
			or_words1 = null;
			processPointers(hg, synsets, words, depth);
		}
	}

	private void makePointers(HyperGraph hg, Object obj, Map synMap, Map wordMap, boolean propagate)
			throws JWNLException {
		//
		Pointer[] ps = (obj instanceof Synset) ? ((Synset) obj).getPointers()
				: ((Word) obj).getPointers();
		HGHandle[] ptrTargets = new HGHandle[2];
		for (int i = 0; i < ps.length; i++) {
			if(ps[i] == null || ps[i].getType() == null ||
					ps[i].getType().getLabel() == null)
			{
				System.out.println("??????????????Pointer : " + ps[i] + ":");
				System.out.println("??????????????Pointer : " + ps[i].getType() + ":");
				continue;
			}
			if(ps[i].getType().equals(PointerType.NOMINALIZATION))
				continue;
			System.out.println("Pointer: " + ps[i].getType().getLabel() + ":"
					+ ps[i].getSource().getClass().getName() + " source: "
					+ ps[i].getSource());
			System.out.println("Pointer: "
					+ ps[i].getTarget().getClass().getName() + " target: "
					+ ps[i].getTarget());
			ptrTargets[0] = makeSynsetHandle(hg, ps[i].getSource(), synMap,
					wordMap, propagate);
			ptrTargets[1] = makeSynsetHandle(hg, ps[i].getTarget(), synMap,
					wordMap, propagate);
			System.out.println("handles: " + ptrTargets[0] + " : " + ptrTargets[1]);
			if(ptrTargets[1] == null || ptrTargets[0]== null)
				continue;
			HGValueLink link = new HGValueLink(ps[i].getType().getLabel(),
					ptrTargets);
			if(links.contains(link))
					return;
			links.add(link);
			hg.add(link);
		}
		// /
	}

	private HGHandle makeWordHandle(HyperGraph hg, Word word, Map synMap,
			Map wordMap) throws JWNLException {
		if (!wordMap.containsKey(word.getLemma())) {
			HGHandle hh = hg.add(word.getLemma());
			wordMap.put(word.getLemma(), hh);
			or_words.add(word);
           //propagate the creation of wordss
			//check avoid the infinite loop
			/*
			Pointer[] ps = word.getPointers();
			for (int i = 0; i < ps.length; i++)
			{
				if(!ps[i].getSource().equals(word))
				  makeSynsetHandle(hg, ps[i].getSource(), synMap, wordMap);
				if(!ps[i].getTarget().equals(word))
				  makeSynsetHandle(hg, ps[i].getTarget(), synMap, wordMap);
			}
			*/
			return hh;
		}
		return (HGHandle) wordMap.get(word.getLemma());
	}

	private HGHandle makeSynsetHandle(HyperGraph hg, Object syn1, Map synMap,
			Map wordMap, boolean propagate) throws JWNLException {
		System.out.println("makeSynsetHandle: " + syn1);
		if (!(syn1 instanceof Synset)) {
			return makeWordHandle(hg, (Word) syn1, synMap, wordMap);
		}

		Synset syn = (Synset) syn1;
		String label = makeSynLabel(syn);
		if (synMap.containsKey(label))
			return (HGHandle) synMap.get(label);
		if(!propagate)
			return null;
		System.out.println("makeSynsetHandle - new: " + syn1);
		HGAtomType syns_type = hg.getTypeSystem().getAtomType(Synset.class);
		// HGHandle handle = hg.add(syn, syns_type);
		Word[] words = syn.getWords();
		List targets = new LinkedList();
		for (int i = 0; i < words.length; i++) {
			System.out.println("Adding word: " + words[i]);
			// if(wordMap.containsKey(words[i]))
			HGHandle sys_handle = makeWordHandle(hg, words[i], synMap, wordMap);
			targets.add(sys_handle);
		}
		//propagate the creation of Words //synsets
		//check avoid the infinite loop
		Pointer[] ps = syn.getPointers();
		for (int i = 0; i < ps.length; i++)
		{
			//if(!ps[i].getSource().equals(syn))
			if(ps[i].getSource() instanceof Word) //&&!label.equals(makeSynLabel((Synset)ps[i].getSource())))
				makeSynsetHandle(hg, ps[i].getSource(), synMap, wordMap, propagate);
			//if(!ps[i].getTarget().equals(syn))
			if(ps[i].getTarget() instanceof Word) // &&	!label.equals(makeSynLabel((Synset)ps[i].getTarget())))
			{
				//System.out.println("makeSynsetHandle : " + label + " trg_label: " + makeSynLabel((Synset)ps[i].getTarget()));
			   makeSynsetHandle(hg, ps[i].getTarget(), synMap, wordMap, propagate);
			}
		}

		HGValueLink link = new HGValueLink(makeSynLabel(syn)/* syn */,
				(HGHandle[]) targets.toArray(new HGHandle[targets.size()]));
		synMap.put(makeSynLabel(syn), hg.add(link));
		or_synsets.add(syn);
		return (HGHandle) synMap.get(makeSynLabel(syn));
	}

	private String makeSynLabel(Synset syn) throws JWNLException {
		String label = "";
		Word[] words = syn.getWords();
		for (int i = 0; i < words.length; i++)
			label += words[i].getLemma() + "|";
		return label;
	}

	private void printPointers(IndexWord word) throws JWNLException {
		Synset[] syn = word.getSenses();
		for(int j = 0; j < syn.length; j++)
		{
    	  Pointer[] ps = syn[j].getPointers();
    	  System.out.println("Synset: " + syn[j] + "size: " + ps.length);
		  for (int i = 0; i < ps.length; i++) {
			  System.out.println("Type \"" + ps[i].getType() + " : " + i);
			System.out.println("Type \"" + ps[i].getType().getLabel() + " : " + i);
			PointerTarget target = ps[i].getSource();
			PointerTarget source = ps[i].getTarget();
			System.out.println("Source: " + source.getClass().getName() + ": "
					+ source);
			System.out.println("Target: " + target.getClass().getName() + ": "
					+ target);
			int src_index = 0;
			if(ps[i].isLexical())
			{
				src_index = ((Word) ps[i].getSource()).getIndex();//getWordIndexInSynset((Word) ps[i].getSource(), syn[j]);
			}
			System.out.println("Indexes: " + src_index + " target: " + ps[i].getTargetIndex());
		 }
		}
		
		List pos = PointerType.getAllPointerTypes();
		Iterator it = pos.iterator();
		while(it.hasNext())
		{
			PointerType type = (PointerType) it.next();
			System.out.println("Type: " + type.getKey() + " label: " + type.getLabel());
		}
	}
	
	
	private void printFrames(IndexWord word) throws JWNLException {
		//dumpVerbFrames(hg, word.getSense(4));
		String[] frames = word.getSense(4).getVerbFrames();
		System.out.println("Synset: " + word.getSense(4));
		System.out.println("BitSet: " + word.getSense(4).getVerbFrameFlags());
		for (int i = 0; i < frames.length; i++)
			System.out.println("Frames: " +  frames[i]);
		
		//frames = word.getSense(3).getVerbFrames();
		//System.out.println("Synset: " + word.getSense(3));
		//for (int i = 0; i < frames.length; i++)
		//	System.out.println("Frames: " +  frames[i]);
	}
	
	
	private static void connectVerbFrames(HyperGraph hg, Synset syn)
	{
		//System.out.println("VerbFrame - size():" + VerbFrame.getVerbFramesSize());
		//for(int i = 1; i <= VerbFrame.getVerbFramesSize(); i++)
		//	System.out.println("VerbFrame" + i + ":" + VerbFrame.getKeyString(i));
		
		//String[] frames = syn.getVerbFrames();
		
		BitSet bs = syn.getVerbFrameFlags();
		System.out.println("Syn" + syn);
		System.out.println("BitSet" + bs + " size: " + bs.size());
		for(int ii=bs.nextSetBit(0); ii>=0; ii=bs.nextSetBit(ii+1))
		{
			System.out.println("BitSet" + ii);
		}
			
		
		//if(true) return;
		//System.out.println("Frames.size()" + syn.getVerbFrames().length);
		int[] fr_inds = VerbFrame.getVerbFrameIndicies(bs);//VerbFrame.getVerbFrameIndicies(bs);
		for (int i = 0; i < fr_inds.length; i++)
		{
			System.out.println("VerbFrame" + fr_inds[i] + ":" + VerbFrame.getFrame(fr_inds[i]));
		}
		//HGValueLink link = new HGValueLink(
		//		new com.kobrix.wordnet.data.VerbFrame(frames[i]));
		
	}

	private void demonstrateMorphologicalAnalysis(String phrase)
			throws JWNLException {
		// "running-away" is kind of a hard case because it involves
		// two words that are joined by a hyphen, and one of the words
		// is not stemmed. So we have to both remove the hyphen and stem
		// "running" before we get to an entry that is in WordNet
		System.out.println("Base form for \"" + phrase + "\": "
				+ Dictionary.getInstance().lookupIndexWord(POS.VERB, phrase));
	}

	private void demonstrateListOperation(IndexWord word) throws JWNLException {
		// Get all of the hypernyms (parents) of the first sense of
		// <var>word</var>
		PointerTargetNodeList hypernyms = PointerUtils.getInstance()
				.getDirectHypernyms(word.getSense(1));
		System.out.println("Direct hypernyms of \"" + word.getLemma() + "\":");
		hypernyms.print();
	}

	private void demonstrateTreeOperation(IndexWord word) throws JWNLException {
		// Get all the hyponyms (children) of the first sense of <var>word</var>
		PointerTargetTree hyponyms = PointerUtils.getInstance().getHyponymTree(
				word.getSense(1));
		System.out.println("Hyponyms of \"" + word.getLemma() + "\":");
		hyponyms.print();
	}

	private void demonstrateAsymmetricRelationshipOperation(IndexWord start,
			IndexWord end) throws JWNLException {
		// Try to find a relationship between the first sense of
		// <var>start</var> and the first sense of <var>end</var>
		RelationshipList list = RelationshipFinder.getInstance()
				.findRelationships(start.getSense(1), end.getSense(1),
						PointerType.HYPERNYM);
		System.out.println("Hypernym relationship between \""
				+ start.getLemma() + "\" and \"" + end.getLemma() + "\":");
		for (Iterator itr = list.iterator(); itr.hasNext();) {
			((Relationship) itr.next()).getNodeList().print();
		}
		System.out
				.println("Common Parent Index: "
						+ ((AsymmetricRelationship) list.get(0))
								.getCommonParentIndex());
		System.out.println("Depth: " + ((Relationship) list.get(0)).getDepth());
	}

	private void demonstrateSymmetricRelationshipOperation(IndexWord start,
			IndexWord end) throws JWNLException {
		// find all synonyms that <var>start</var> and <var>end</var> have in
		// common
		RelationshipList list = RelationshipFinder.getInstance()
				.findRelationships(start.getSense(1), end.getSense(1),
						PointerType.SIMILAR_TO);
		System.out.println("Synonym relationship between \"" + start.getLemma()
				+ "\" and \"" + end.getLemma() + "\":");
		for (Iterator itr = list.iterator(); itr.hasNext();) {
			((Relationship) itr.next()).getNodeList().print();
		}
		System.out.println("Depth: " + ((Relationship) list.get(0)).getDepth());
	}

	private static void loadHG(HyperGraph h, HGAtomType type, Set nodes,
			Set links) {
		HGHandle sHandle = h.getHandle(type);
		AtomTypeCondition cond = new AtomTypeCondition(((HGLiveHandle) sHandle)
				.getPersistentHandle());
		HGQuery query = HGQuery.make(h, cond);
		HGSearchResult it = query.execute();
		while (it.hasNext()) {
			it.next();
			processHandles(h, (HGHandle) it.current(), nodes, links);
		}
	}

	private static void processHandles(HyperGraph h, HGHandle handle,
			Set nodes, Set links) {
		// System.out.println("processHandles: " + handle.getClass().getName());
		if (handle instanceof HGLiveHandle)
			handle = ((HGLiveHandle) handle).getPersistentHandle();

		// System.out.println("processHandles - after: " + handle);
		if (nodes.contains(handle))
			return;
		// TODO:Nasty hacks - everything here should be redesigned
		Object obj = h.get(handle);
		if (obj instanceof RecordType) {
			HGHandle[] recHandles = HGUtils.getAllForType(h,
					(HGPersistentHandle) handle);
			for (int i = 0; i < recHandles.length; i++)
				processHandles(h, recHandles[i], nodes, links);
			return;
		}
		nodes.add(handle);
		// System.out.println("processHandles - add: " +
		// h.get(handle).getClass().getName());
		HGHandle[] all = h.getIncidenceSet(handle);
		for (int i = 0; i < all.length; i++) {
			HGValueLink link = (HGValueLink) h.get(all[i]);
			if (links.contains(all[i]))// link))
				continue;
			links.add(all[i]); // link);
			int arity = link.getArity();
			for (int j = 0; j < arity; j++) {
				processHandles(h, link.getTargetAt(j), nodes, links);
			}
		}
	}
	
	private static Object getMapKey(Map map, Object val) {
		for (Iterator it = map.entrySet().iterator(); it.hasNext();) {
			Map.Entry obj = (Map.Entry) it.next();
			if (obj.getValue().equals(val))
				return obj.getKey();
		}
		return null;
	}

	public static class MyAtomTypeCondition extends AtomTypeCondition {
		private Set types = new HashSet();

		public MyAtomTypeCondition(HGPersistentHandle typeHandle) {
			super(typeHandle);
		}

		public boolean satisfies(HyperGraph h, HGHandle value) {
			Object obj = hg.get(value);
			System.out.println("satisfies: " + types.size() + ": "
					+ obj.getClass().getName());
			if (types.contains(obj.getClass()))
				return false;
			types.add(obj.getClass());
			return true;
		}
	}
}
