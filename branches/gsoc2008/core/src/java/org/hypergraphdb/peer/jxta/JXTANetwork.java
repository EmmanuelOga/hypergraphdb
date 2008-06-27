package org.hypergraphdb.peer.jxta;

import java.util.Set;

import org.hypergraphdb.peer.PeerNetwork;
import org.hypergraphdb.util.Pair;

import net.jxta.document.Advertisement;
import net.jxta.peergroup.PeerGroup;
import net.jxta.pipe.PipeID;

public interface JXTANetwork extends PeerNetwork
{
	PeerGroup getPeerGroup();
	
	void publishAdv(Advertisement adv);
	void addOwnPipe(PipeID pipeId);
	Set<Advertisement> getAdvertisements();
	Advertisement getPipeAdv();

}
