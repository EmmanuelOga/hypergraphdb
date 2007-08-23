package org.hypergraphdb.app.wordnet.data.pointers;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.app.wordnet.data.Pointer;


public class PartHolonym extends Pointer{

	public PartHolonym() {
		super();
	}

	public PartHolonym(HGHandle[] outgoingSet) {
		super(outgoingSet);
	}

}
