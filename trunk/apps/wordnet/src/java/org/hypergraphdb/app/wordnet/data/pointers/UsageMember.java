package org.hypergraphdb.app.wordnet.data.pointers;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.app.wordnet.data.Pointer;


public class UsageMember extends Pointer{

	public UsageMember() {
		super();
	}

	public UsageMember(HGHandle[] outgoingSet) {
		super(outgoingSet);
	}

}
