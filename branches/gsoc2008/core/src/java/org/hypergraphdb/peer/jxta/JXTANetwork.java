package org.hypergraphdb.peer.jxta;

import java.util.Set;

import net.jxta.document.Advertisement;
import net.jxta.peergroup.PeerGroup;

public interface JXTANetwork
{
	boolean init(JXTAPeerConfiguration config);
	void start();
	
	PeerGroup getPeerGroup();
	
	void publishAdv(Advertisement adv);
	Set<Advertisement> getAdvertisements();

}
