package org.hypergraphdb.peer;

/**
 * @author ciprian.costa
 * This class will be able to decide if a peer will store data locally or not.
 */
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
