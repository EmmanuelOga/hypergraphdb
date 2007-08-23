package org.hypergraphdb.app.wordnet.data.pointers;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.app.wordnet.data.Pointer;


public class EntailedBy extends Pointer{

	public EntailedBy() {
		super();
	}

	public EntailedBy(HGHandle[] outgoingSet) {
		super(outgoingSet);
	}

}
