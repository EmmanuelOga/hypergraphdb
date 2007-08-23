package org.hypergraphdb.app.wordnet.data.pointers;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.app.wordnet.data.Pointer;


public class Region extends Pointer{

	public Region() {
		super();
	}

	public Region(HGHandle[] outgoingSet) {
		super(outgoingSet);
	}

}
