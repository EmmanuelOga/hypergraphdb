package org.hypergraphdb.app.wordnet.data.pointers;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.app.wordnet.data.Pointer;


public class Cause extends Pointer{

	public Cause() {
		super();
	}

	public Cause(HGHandle[] outgoingSet) {
		super(outgoingSet);
	}

}
