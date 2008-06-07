package org.hypergraphdb.peer;

public class DummyPolicy implements PeerPolicy
{
	boolean storeLocally;
	
	public DummyPolicy(boolean storeLocally)
	{
		this.storeLocally = storeLocally;
	}

	public boolean shouldStore(Object atom)
	{
		return storeLocally;
	}
	
	
}
