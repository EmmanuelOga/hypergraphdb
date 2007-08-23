package org.hypergraphdb.app.wordnet.data.pointers;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.app.wordnet.data.Pointer;


public class VerbGroup extends Pointer{

	public VerbGroup() {
		super();
	}

	public VerbGroup(HGHandle[] outgoingSet) {
		super(outgoingSet);
	}

}
