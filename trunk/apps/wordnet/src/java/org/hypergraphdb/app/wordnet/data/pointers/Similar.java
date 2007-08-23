package org.hypergraphdb.app.wordnet.data.pointers;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.app.wordnet.data.Pointer;


public class Similar extends Pointer{

	public Similar() {
		super();
	}

	public Similar(HGHandle[] outgoingSet) {
		super(outgoingSet);
	}

}
