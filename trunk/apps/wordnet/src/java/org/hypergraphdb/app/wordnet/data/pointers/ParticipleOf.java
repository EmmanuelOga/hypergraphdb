package org.hypergraphdb.app.wordnet.data.pointers;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.app.wordnet.data.Pointer;


public class ParticipleOf extends Pointer{

	public ParticipleOf() {
		super();
	}

	public ParticipleOf(HGHandle[] outgoingSet) {
		super(outgoingSet);
	}

}
