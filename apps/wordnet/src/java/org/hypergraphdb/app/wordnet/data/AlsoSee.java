package org.hypergraphdb.app.wordnet.data;

import org.hypergraphdb.HGHandle;


public class AlsoSee extends SemanticLink
{

	public AlsoSee() {
		super();
	}

	public AlsoSee(HGHandle[] outgoingSet) {
		super(outgoingSet);
	}

}
