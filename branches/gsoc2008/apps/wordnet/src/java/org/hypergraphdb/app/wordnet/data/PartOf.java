package org.hypergraphdb.app.wordnet.data;

import org.hypergraphdb.HGHandle;

public class PartOf extends Hasa
{
	public PartOf() 
	{
		super();
	}

	public PartOf(HGHandle[] outgoingSet) 
	{
		super(outgoingSet);
	}
}
