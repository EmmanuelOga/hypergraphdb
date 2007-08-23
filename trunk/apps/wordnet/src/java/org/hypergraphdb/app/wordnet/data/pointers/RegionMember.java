package org.hypergraphdb.app.wordnet.data.pointers;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.app.wordnet.data.Pointer;


public class RegionMember extends Pointer{

	public RegionMember() {
		super();
	}

	public RegionMember(HGHandle[] outgoingSet) {
		super(outgoingSet);
	}

}
