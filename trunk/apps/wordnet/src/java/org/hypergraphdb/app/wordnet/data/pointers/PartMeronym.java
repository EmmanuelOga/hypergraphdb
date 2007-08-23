package org.hypergraphdb.app.wordnet.data.pointers;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.app.wordnet.data.Pointer;


public class PartMeronym extends Pointer{

	public PartMeronym() {
		super();
	}

	public PartMeronym(HGHandle[] outgoingSet) {
		super(outgoingSet);
	}

}
