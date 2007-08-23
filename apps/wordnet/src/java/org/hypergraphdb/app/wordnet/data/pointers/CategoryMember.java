package org.hypergraphdb.app.wordnet.data.pointers;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.app.wordnet.data.Pointer;


public class CategoryMember extends Pointer{

	public CategoryMember() {
		super();
	}

	public CategoryMember(HGHandle[] outgoingSet) {
		super(outgoingSet);
	}

}
