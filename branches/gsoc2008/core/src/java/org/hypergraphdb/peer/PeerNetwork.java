package org.hypergraphdb.peer;

import org.hypergraphdb.peer.jxta.JXTAPeerConfiguration;
import org.hypergraphdb.query.HGAtomPredicate;

/**
 * @author ciprian.costa
 * Implementors will handle the available information about the peer network
 */
public interface PeerNetwork
{
	boolean init(JXTAPeerConfiguration config);
	void start();

	//get/set atom interests for known peers
	void setAtomInterests(Object peerId, HGAtomPredicate interest);
	HGAtomPredicate getAtomInterests(Object peerId);
}
