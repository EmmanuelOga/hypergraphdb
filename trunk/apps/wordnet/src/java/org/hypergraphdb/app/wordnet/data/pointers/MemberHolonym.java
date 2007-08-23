package org.hypergraphdb.app.wordnet.data.pointers;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.app.wordnet.data.Pointer;


public class MemberHolonym extends Pointer{

	public MemberHolonym() {
		super();
	}

	public MemberHolonym(HGHandle[] outgoingSet) {
		super(outgoingSet);
	}

}
