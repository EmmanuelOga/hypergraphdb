package org.hypergraphdb.app.wordnet.data;

import org.hypergraphdb.HGHandle;

public class ParticipleOf extends SemanticLink
{
	public ParticipleOf() {
		super();
	}

	public ParticipleOf(HGHandle[] outgoingSet) {
		super(outgoingSet);
	}
}