package org.hypergraphdb.app.wordnet.data;

import org.hypergraphdb.HGHandle;

public class InCategory extends DomainLink
{
	public InCategory() 
	{
		super();
	}

	public InCategory(HGHandle[] outgoingSet) 
	{
		super(outgoingSet);
	}
}
