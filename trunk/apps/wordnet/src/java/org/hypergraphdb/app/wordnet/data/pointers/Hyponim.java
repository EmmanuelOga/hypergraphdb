package org.hypergraphdb.app.wordnet.data.pointers;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.app.wordnet.data.Pointer;


public class Hyponim extends Pointer{

	public Hyponim() {
		super();
	}

	public Hyponim(HGHandle[] outgoingSet) {
		super(outgoingSet);
	}

}
