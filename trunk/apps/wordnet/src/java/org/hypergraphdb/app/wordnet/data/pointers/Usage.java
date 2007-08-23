package org.hypergraphdb.app.wordnet.data.pointers;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.app.wordnet.data.Pointer;


public class Usage extends Pointer{

	public Usage() {
		super();
	}

	public Usage(HGHandle[] outgoingSet) {
		super(outgoingSet);
	}

}
