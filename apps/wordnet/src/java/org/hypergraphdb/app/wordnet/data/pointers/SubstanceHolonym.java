package org.hypergraphdb.app.wordnet.data.pointers;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.app.wordnet.data.Pointer;


public class SubstanceHolonym extends Pointer{

	public SubstanceHolonym() {
		super();
	}

	public SubstanceHolonym(HGHandle[] outgoingSet) {
		super(outgoingSet);
	}

}
