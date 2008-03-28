package org.hypergraphdb.app.wordnet.data;

import org.hypergraphdb.HGHandle;

public class NounSynsetLink extends SynsetLink{

	public NounSynsetLink() {
		super();
	}

	public NounSynsetLink(HGHandle[] targets) {
		super(targets);
	}

	public String toString()
	{
		return "Noun(" + getGloss() + ")";
	}	
}
