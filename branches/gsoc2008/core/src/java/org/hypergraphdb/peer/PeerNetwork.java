package org.hypergraphdb.peer;

import org.hypergraphdb.query.HGAtomPredicate;

/**
 * @author ciprian.costa
 * Implementors will handle the available information about the peer network
 */
public interface PeerNetwork
{
	boolean init(Object config);
	void start();

	//get/set atom interests for known peers
	void setAtomInterests(Object peer, HGAtomPredicate interest);
	HGAtomPredicate getAtomInterests(Object peer);
	
	Object getPeerId(Object peer);
	
	void waitForRemotePipe();
}
