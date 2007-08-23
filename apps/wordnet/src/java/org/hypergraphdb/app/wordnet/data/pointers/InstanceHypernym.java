package org.hypergraphdb.app.wordnet.data.pointers;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.app.wordnet.data.Pointer;


public class InstanceHypernym extends Pointer{

	public InstanceHypernym() {
		super();
	}

	public InstanceHypernym(HGHandle[] outgoingSet) {
		super(outgoingSet);
	}

}
