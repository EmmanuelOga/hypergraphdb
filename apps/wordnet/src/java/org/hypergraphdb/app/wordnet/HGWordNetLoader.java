package org.hypergraphdb.app.wordnet;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Comparator;
import java.util.Iterator;

import net.didion.jwnl.JWNL;
import net.didion.jwnl.JWNLException;
import net.didion.jwnl.data.Exc;
import net.didion.jwnl.data.IndexWord;
import net.didion.jwnl.data.POS;
import net.didion.jwnl.data.Pointer;
import net.didion.jwnl.data.PointerTarget;
import net.didion.jwnl.data.Synset;
import net.didion.jwnl.data.VerbFrame;
import net.didion.jwnl.data.Word;
import net.didion.jwnl.dictionary.Dictionary;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGIndex;
import org.hypergraphdb.HGPersistentHandle;
import org.hypergraphdb.HGTypeSystem;
import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.app.wordnet.data.AdjExcLink;
import org.hypergraphdb.app.wordnet.data.AdjSynsetLink;
import org.hypergraphdb.app.wordnet.data.AdverbExcLink;
import org.hypergraphdb.app.wordnet.data.AdverbSynsetLink;
import org.hypergraphdb.app.wordnet.data.ExcLink;
import org.hypergraphdb.app.wordnet.data.NounExcLink;
import org.hypergraphdb.app.wordnet.data.NounSynsetLink;
import org.hypergraphdb.app.wordnet.data.PtType;
import org.hypergraphdb.app.wordnet.data.SynsetLink;
import org.hypergraphdb.app.wordnet.data.VerbExcLink;
import org.hypergraphdb.app.wordnet.data.VerbFrameLink;
import org.hypergraphdb.app.wordnet.data.VerbSynsetLink;

/**
 * 
 * <p>
 * Use this class to load the WordNet database into HyperGraph. To use this class:
 * </p>
 * <ol>
 * <li>Download WordNet from http://http://wordnet.princeton.edu.
 * <li>Create a new instance.</li>
 * <li>Set the location of the WordNet dictionary with the <code>setDictionaryLocation</code>
 * method. This should be the directory from the WordNet distribution that contains all
 * WordNet data files (e.g. c:\wordnet\dict).
 * </li>
 * <li>Invoke the <code>loadWordNet(HyperGraph) method.</li> 
 * </ol>
 * @author Borislav Iordanov
 *
 */
public class HGWordNetLoader
{
	final static String PROPS_RESOURCE = "org/hypergraphdb/app/wordnet/configuration/file_properties.xml";
	final POS[] pos = new POS[] { POS.NOUN, POS.VERB, POS.ADVERB, POS.ADJECTIVE };
	final Comparator posComparator = new Comparator()
	{
		public int compare(Object left, Object right)
		{
			POS pl = (POS)left;
			POS pr = (POS)right;
			return pl.getKey().compareTo(pr.getKey());
		}
	};	
	final Class[] synset_link_classes = new Class[] 
	{
		NounSynsetLink.class, 
		VerbSynsetLink.class, 
		AdverbSynsetLink.class,
		AdjSynsetLink.class 
	};
	final String[] synset_alias = new String[] 
    { 
		"NounSynset",
		"VerbSynset", 
		"AdverbSynset", 
		"AdjSynset" 
	};
	
	int n_exc = 0;
	long n_syn = 0;
	long n_point = 0;
	long n_word = 0;
	long n_vlinks = 0;
	
	private String typeAliasPrefix;
	private String dictionaryLocation;
	
	public HGWordNetLoader()
	{
		Arrays.sort(pos, posComparator);
	}
	
	
	public String getDictionaryLocation()
	{
		return dictionaryLocation;
	}


	public void setDictionaryLocation(String dictionaryLocation)
	{
		this.dictionaryLocation = dictionaryLocation;
	}


	public String getTypeAliasPrefix()
	{
		return typeAliasPrefix;
	}


	public void setTypeAliasPrefix(String typeAliasPrefix)
	{
		this.typeAliasPrefix = typeAliasPrefix;
	}

	public void loadWordNet(HyperGraph graph)
	{
		try
		{
			// First, we get the file_properties.xml that JWNL needs as a String,
			// then put the local directory path to the WordNet dictionary, and then
			// initialize the JWNL library.
			StringBuffer propsBuffer = new StringBuffer();
			InputStream inStream = this.getClass().getResourceAsStream(PROPS_RESOURCE);
			if (inStream == null)
				inStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(PROPS_RESOURCE);
			if (inStream == null)
				throw new RuntimeException("Could not load configuration resource " + PROPS_RESOURCE);
			InputStreamReader in = new InputStreamReader(inStream);
			char [] buf = new char[256];
			for (int n = in.read(buf); n > 0; n = in.read(buf) )
				propsBuffer.append(buf, 0, n);				
			JWNL.initialize(new ByteArrayInputStream(
					propsBuffer.toString().replace("LOCAL_DICTIONARY_PATH", 
												   dictionaryLocation).getBytes()));
			createIndices(graph);
			Dictionary d = Dictionary.getInstance();			
			for(int i = 0; i < pos.length; i++)
				addWords(graph, d.getIndexWordIterator(pos[i]));
			for (int i = 0; i < pos.length; i++) 
				addSynsets(graph, d.getSynsetIterator(pos[i]), i);
			for (int i = 0; i < pos.length; i++)
				addPointers(graph, d.getSynsetIterator(pos[i]));
			addVerbFrames(graph);
			for (Iterator it = d.getSynsetIterator(POS.VERB); it.hasNext();)
				connectSynsetVerbFrames(graph, (Synset) it.next());
			for (int i = 0; i < pos.length; i++)
				addExceptions(graph, d.getExceptionIterator(pos[i]), i);
		}
		catch (RuntimeException ex) { throw ex; }
		catch (Exception ex) { throw new RuntimeException(ex); }
	}
	
	private HGPersistentHandle lookup(HyperGraph graph, 
									 String typeAlias,
									 String keyProperty, 
									 String keyValue)
	{
		HGHandle typeHandle = graph.getTypeSystem().getTypeHandle(typeAlias);
		HGIndex index = graph.getIndex(typeHandle, new String[] { keyProperty });
		if (index == null)
		{
			index = graph.getIndex(typeHandle, new String[] { keyProperty });
		}
		return (HGPersistentHandle) index.findFirst(keyValue);
	}
	
	private void createIndices(HyperGraph graph)
	{
		HGTypeSystem ts = graph.getTypeSystem();
		HGPersistentHandle typeH = graph.getPersistentHandle(
				ts.getTypeHandle(org.hypergraphdb.app.wordnet.data.Word.class));
		HGHandle check = ts.getTypeHandle(typeAliasPrefix + "word");
		if (check == null)
			ts.addAlias(typeH, typeAliasPrefix + "word");
		else if (!check.equals(typeH))
			throw new RuntimeException("Alias already in use: " + typeAliasPrefix + "word" + ", please change WordNet prefix.");
		if (graph.getIndex(typeH, new String[] { "lemma" }) == null)
			graph.createIndex(typeH, new String[] { "lemma" });
		for (int i = 0; i < pos.length; i++)
		{
			typeH = graph.getPersistentHandle(ts.getTypeHandle(synset_link_classes[i]));
			check = ts.getTypeHandle(typeAliasPrefix + synset_alias[i]);
			if (check == null)
				ts.addAlias(typeH, typeAliasPrefix + synset_alias[i]);
			else if (!check.equals(typeH))
				throw new RuntimeException("Alias already in use: " + typeAliasPrefix + synset_alias[i] + ", please change WordNet prefix.");					
			if (graph.getIndex(typeH, new String[] { "gloss" }) == null)
				graph.createIndex(typeH, new String[] { "gloss" });
		}
		typeH = graph.getPersistentHandle(ts.getTypeHandle(org.hypergraphdb.app.wordnet.data.VerbFrame.class));
		check = ts.getTypeHandle(typeAliasPrefix + "frame");
		if (check == null)
			ts.addAlias(typeH, typeAliasPrefix + "frame");
		else if (!check.equals(typeH))
			throw new RuntimeException("Alias already in use: " + typeAliasPrefix + "frame" + ", please change WordNet prefix.");
		if (graph.getIndex(typeH, new String[] { "text" }) == null)
			graph.createIndex(typeH, new String[] { "text" });
	}
	
	private void addWords(HyperGraph graph, Iterator it)
	{
		while (it.hasNext())
		{
			IndexWord word = (IndexWord) it.next();
			graph.add(new org.hypergraphdb.app.wordnet.data.Word(word.getLemma()));
		}
	}
	
	private void addSynsets(HyperGraph hg, Iterator it, int pos)
	{
		while (it.hasNext())
		{
			makeSynsetLinkHandle(hg, (Synset) it.next(), pos);
		}
	}

	private HGHandle makeSynsetLinkHandle(HyperGraph graph, Synset syn, int pos)
	{
		Word[] words = syn.getWords();
		HGHandle[] targets = new HGHandle[words.length];
		for (int i = 0; i < words.length; i++)
		{
			targets[i] = getWordHandle(graph, words[i].getLemma(), true);
		}
		SynsetLink link = createSynsetLink(syn);
		link.setTargets(targets);
		HGHandle sh = graph.getHandle(link);
		if (sh == null)
		{
			sh = lookup(graph, this.getTypeAliasPrefix() + synset_alias[pos], "gloss", syn.getGloss());
			if (sh != null) return sh;
			sh = graph.add(link);
			n_syn++;
		}
		return sh;
	}
	
	private SynsetLink createSynsetLink(Synset syn)
	{
		SynsetLink link = null;
		if (syn.getPOS().equals(POS.ADJECTIVE))
			link = new AdjSynsetLink();
		else if (syn.getPOS().equals(POS.ADVERB))
			link = new AdverbSynsetLink();
		else if (syn.getPOS().equals(POS.NOUN))
			link = new NounSynsetLink();
		else if (syn.getPOS().equals(POS.VERB)) link = new VerbSynsetLink();
		link.setGloss(syn.getGloss());
		return link;
	}

	private void addPointers(HyperGraph graph, Iterator it)
			throws JWNLException
	{
		PtType.initialize();
		while (it.hasNext())
		{
			Synset syn = (Synset) it.next();
			Pointer[] ps = syn.getPointers();
			for (int i = 0; i < ps.length; i++)
			{
				n_point++;
				org.hypergraphdb.app.wordnet.data.Pointer hg_p = makePointer(graph, ps[i]);
				if (hg_p != null)
				{
					graph.add(hg_p);
				} 
				else
				{
					throw new NullPointerException("!!!!!!!NULL Pointer: "
							+ ps[i]);
				}
			}
		}
	}

	private void addExceptions(HyperGraph graph, Iterator it, int pos)
	{
		while (it.hasNext())
		{
			Exc exc = (Exc) it.next();
			String[] excs = exc.getExceptionArray();
			HGHandle[] targets = new HGHandle[excs.length + 1];
			targets[0] = getWordHandle(graph, exc.getLemma(), true);
			for (int i = 0; i < excs.length; i++)
			{
				targets[i + 1] = getWordHandle(graph, excs[i], true);
			}
			graph.add(createExcLink(targets, pos));
			n_exc++;
		}
	}

	// POS.NOUN, POS.VERB, POS.ADVERB,POS.ADJECTIVE
	private ExcLink createExcLink(HGHandle[] targets, int pos)
	{
		ExcLink link = null;
		if (pos == 0)
			link = new NounExcLink();
		else if (pos == 1)
			link = new VerbExcLink();
		else if (pos == 2)
			link = new AdverbExcLink();
		else if (pos == 3) link = new AdjExcLink();
		link.setTargets(targets);
		return link;
	}

	private org.hypergraphdb.app.wordnet.data.Pointer makePointer(HyperGraph graph, Pointer p) throws JWNLException
	{
		// System.out.println("makePointer: " + p + " : " +
		// p.getType().getKey());
		if (PtType.getPointerTypeForKey(p.getType().getKey()) == null)
		{
			// temp_check(p);
			return null;
		}
		Class clazz = PtType.getPointerTypeForKey(p.getType().getKey())
				.getClazz();
		org.hypergraphdb.app.wordnet.data.Pointer hg_p = null;
		try
		{
			hg_p = (org.hypergraphdb.app.wordnet.data.Pointer) clazz
					.newInstance();
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			throw new RuntimeException(
					"Could not instantiate pointer with class: " + clazz);
		}
		int src_index = 0;
		if (p.isLexical()) src_index = ((Word) p.getSource()).getIndex();
		// System.out.println("Indexes: " + src_index + " target: " +
		// p.getTargetIndex());
		hg_p.setSourceIndex(src_index);
		hg_p.setTargetIndex(p.getTargetIndex());
		HGHandle[] ptrTargets = new HGHandle[2];
		/*
		 * 
		 * System.out.println("Pointer: " + p.getType().getLabel() + ":" +
		 * p.getSource().getClass().getName() + " source: " + p.getSource());
		 * 
		 * System.out.println("Pointer: " + p.getTarget().getClass().getName() + "
		 * target: " + p.getTarget()); //
		 */
		Synset s = getSynsetForPointerTarget(p.getSource());
		ptrTargets[0] = makeSynsetLinkHandle(graph, s, Arrays.binarySearch(pos, s.getPOS(), posComparator));
		Synset s1 = getSynsetForPointerTarget(p.getTarget());
		ptrTargets[1] = makeSynsetLinkHandle(graph, s1, Arrays.binarySearch(pos, s1.getPOS(), posComparator));
		if (ptrTargets[1] == null || ptrTargets[0] == null)
			throw new NullPointerException("NULL target: " + ptrTargets);
		hg_p.setTargets(ptrTargets);
		return hg_p;
	}

	private Synset getSynsetForPointerTarget(PointerTarget p)
	{
		if (p instanceof Synset) return (Synset) p;
		return ((Word) p).getSynset();
	}

	private HGHandle getWordHandle(HyperGraph graph, String lemma,
			boolean search_db)
	{
		org.hypergraphdb.app.wordnet.data.Word hg_word = new org.hypergraphdb.app.wordnet.data.Word(
				lemma);
		HGHandle h = graph.getHandle(hg_word);
		if (h == null && search_db)
		{
			h = lookup(graph, 
					   this.getTypeAliasPrefix() + "word", 
					   "lemma", 
					   lemma);
			if (h != null)
			{
				return h;
			}
		}
		if (h == null)
		{
			// TODO: these are non-index words, maybe we should mark them as
			// such
			h = graph.add(hg_word);
			System.out.println("" + n_word + " Adding word: " + hg_word);
			++n_word;
		}
		return h;
	}

	private void addVerbFrames(HyperGraph graph)
	{
		for (int i = 1; i <= VerbFrame.getVerbFramesSize(); i++)
		{
			graph.add(new org.hypergraphdb.app.wordnet.data.VerbFrame(VerbFrame
					.getFrame(i), i));
		}
	}

	private HGHandle makeVerbFrameHandle(HyperGraph graph, int i)
	{
		org.hypergraphdb.app.wordnet.data.VerbFrame frame = 
			new org.hypergraphdb.app.wordnet.data.VerbFrame(VerbFrame.getFrame(i), i);
		HGHandle h = graph.getHandle(frame);
		if (h != null) return h;
		h = lookup(graph, this.getTypeAliasPrefix() + "frame", "text", frame.getText());
		if (h != null) return h;
		h = graph.add(frame);
		return h;
	}

	private void connectSynsetVerbFrames(HyperGraph graph, Synset syn)
	{
		BitSet bs = syn.getVerbFrameFlags();
		int[] fr_inds = VerbFrame.getVerbFrameIndicies(bs);
		HGHandle[] targets = new HGHandle[2];
		for (int i = 0; i < fr_inds.length; i++)
		{
			targets[0] = makeVerbFrameHandle(graph, fr_inds[i]);
			targets[1] = makeSynsetLinkHandle(graph, syn, 1);
			VerbFrameLink link = new VerbFrameLink(targets);
			graph.add(link);
			n_vlinks++;
		}
	}	
}