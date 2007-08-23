package org.hypergraphdb.app.wordnet.data.pointers;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.app.wordnet.data.Pointer;


public class InstanceHyponym extends Pointer{

	public InstanceHyponym() {
		super();
	}

	public InstanceHyponym(HGHandle[] outgoingSet) {
		super(outgoingSet);
	}

}
