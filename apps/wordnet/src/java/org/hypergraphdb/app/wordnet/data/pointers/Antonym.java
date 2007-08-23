package org.hypergraphdb.app.wordnet.data.pointers;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.app.wordnet.data.Pointer;


public class Antonym extends Pointer
{

	public Antonym() {
		super();
	}

	public Antonym(HGHandle[] outgoingSet) {
		super(outgoingSet);
	}

}
