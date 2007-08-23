package org.hypergraphdb.app.wordnet.data.pointers;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.app.wordnet.data.Pointer;


public class Attribute extends Pointer{

	public Attribute() {
		super();
	}

	public Attribute(HGHandle[] outgoingSet) {
		super(outgoingSet);
	}

}
