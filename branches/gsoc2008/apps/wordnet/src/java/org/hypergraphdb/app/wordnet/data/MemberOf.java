package org.hypergraphdb.app.wordnet.data;

import org.hypergraphdb.HGHandle;

public class MemberOf extends Hasa
{
	public MemberOf() 
	{
		super();
	}

	public MemberOf(HGHandle[] outgoingSet) 
	{
		super(outgoingSet);
	}
}
