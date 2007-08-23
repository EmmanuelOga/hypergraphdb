package org.hypergraphdb.app.wordnet.data.pointers;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.app.wordnet.data.Pointer;


public class MemberMeronym extends Pointer{

	public MemberMeronym() {
		super();
    }

	public MemberMeronym(HGHandle[] outgoingSet) {
		super(outgoingSet);
	}

}
