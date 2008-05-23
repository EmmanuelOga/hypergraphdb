package org.hypergraphdb.app.wordnet.data;

import org.hypergraphdb.HGHandle;

public class Pertainym extends SemanticLink
{

	public Pertainym() {
		super();
	}

	public Pertainym(HGHandle[] outgoingSet) {
		super(outgoingSet);
	}

}
