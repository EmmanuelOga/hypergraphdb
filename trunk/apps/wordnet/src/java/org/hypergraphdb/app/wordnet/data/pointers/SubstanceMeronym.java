package org.hypergraphdb.app.wordnet.data.pointers;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.app.wordnet.data.Pointer;


public class SubstanceMeronym extends Pointer{

	public SubstanceMeronym() {
		super();
	}

	public SubstanceMeronym(HGHandle[] outgoingSet) {
		super(outgoingSet);
	}

}
