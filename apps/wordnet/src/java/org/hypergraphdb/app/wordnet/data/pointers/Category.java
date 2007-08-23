package org.hypergraphdb.app.wordnet.data.pointers;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.app.wordnet.data.Pointer;


public class Category extends Pointer{

	public Category() {
		super();
	}

	public Category(HGHandle[] outgoingSet) {
		super(outgoingSet);
	}

}
