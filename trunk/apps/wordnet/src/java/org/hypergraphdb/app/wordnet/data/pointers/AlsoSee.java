package org.hypergraphdb.app.wordnet.data.pointers;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.app.wordnet.data.Pointer;


public class AlsoSee extends Pointer{

	public AlsoSee() {
		super();
	}

	public AlsoSee(HGHandle[] outgoingSet) {
		super(outgoingSet);
	}

}
