package org.hypergraphdb.app.wordnet.data.pointers;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.app.wordnet.data.Pointer;


public class Pertainym extends Pointer{

	public Pertainym() {
		super();
	}

	public Pertainym(HGHandle[] outgoingSet) {
		super(outgoingSet);
	}

}
