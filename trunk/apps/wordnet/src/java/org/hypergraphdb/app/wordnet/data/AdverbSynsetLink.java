package org.hypergraphdb.app.wordnet.data;

import org.hypergraphdb.HGHandle;

public class AdverbSynsetLink extends SynsetLink{

	public AdverbSynsetLink() {
		super();
	}

	public AdverbSynsetLink(HGHandle[] targets) {
		super(targets);
	}

	public String toString()
	{
		return "Adverb(" + getGloss() + ")";
	}	
}
