package org.hypergraphdb.app.wordnet.data.pointers;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.app.wordnet.data.Pointer;


public class Nominalization extends Pointer{

	public Nominalization() {
		super();
	}

	public Nominalization(HGHandle[] outgoingSet) {
		super(outgoingSet);
	}

}
