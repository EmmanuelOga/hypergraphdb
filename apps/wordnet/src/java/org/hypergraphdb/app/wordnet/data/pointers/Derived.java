package org.hypergraphdb.app.wordnet.data.pointers;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.app.wordnet.data.Pointer;


public class Derived extends Pointer{

	public Derived() {
		super();
	}

	public Derived(HGHandle[] outgoingSet) {
		super(outgoingSet);
	}

}
