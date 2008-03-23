package org.hypergraphdb.app.wordnet.data;

import org.hypergraphdb.HGHandle;

public class Entails extends SemanticLink
{
	public Entails() 
	{
		super();
	}

	public Entails(HGHandle[] outgoingSet) 
	{
		super(outgoingSet);
	}
}
