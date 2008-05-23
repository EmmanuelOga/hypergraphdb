package org.hypergraphdb.app.wordnet.data;

import org.hypergraphdb.HGHandle;

public class DomainLink extends SemanticLink
{
	public DomainLink() 
	{
		super();
	}

	public DomainLink(HGHandle[] outgoingSet) 
	{
		super(outgoingSet);
	}
}