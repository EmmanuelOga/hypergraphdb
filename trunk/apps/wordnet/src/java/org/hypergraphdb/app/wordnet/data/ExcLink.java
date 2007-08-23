package org.hypergraphdb.app.wordnet.data;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGPlainLink;

public class ExcLink extends HGPlainLink{

	public ExcLink() {
		super();
	}

	public ExcLink(HGHandle[] outgoingSet) {
		super(outgoingSet);
	}

	public void setTargets(HGHandle [] _outgoingSet)
	{
		outgoingSet = _outgoingSet;
	}
}
