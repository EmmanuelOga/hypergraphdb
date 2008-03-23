package org.hypergraphdb.app.wordnet.data;

import org.hypergraphdb.HGHandle;

public class InRegion extends DomainLink
{
	public InRegion() 
	{
		super();
	}

	public InRegion(HGHandle[] outgoingSet) 
	{
		super(outgoingSet);
	}
}