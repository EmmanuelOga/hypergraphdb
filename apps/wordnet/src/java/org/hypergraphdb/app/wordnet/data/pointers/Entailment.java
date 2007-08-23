package org.hypergraphdb.app.wordnet.data.pointers;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.app.wordnet.data.Pointer;


public class Entailment extends Pointer{

	public Entailment() {
		super();
	}

	public Entailment(HGHandle[] outgoingSet) {
		super(outgoingSet);
	}

}
