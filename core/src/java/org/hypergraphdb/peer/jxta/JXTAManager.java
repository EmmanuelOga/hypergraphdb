package org.hypergraphdb.peer.jxta;

import java.io.File;

import net.jxta.peergroup.PeerGroup;
import net.jxta.platform.NetworkManager;

/**
 * @author Cipri Costa
 *
 * <p>
 * Handles problems related to the JXTA network : intialize, stop, etc
 * </p>
 */
public class JXTAManager {
	private static NetworkManager peerManager = null;
	private static PeerGroup netPeerGroup = null;

	public static boolean init(String serviceName){
		// TODO might be called multiple times
		
	    try {
	    	System.out.println("Starting network manager");
	    	peerManager = new NetworkManager(NetworkManager.ConfigMode.ADHOC, serviceName,
	    			new File(new File(".cache"), serviceName).toURI());
	    	peerManager.startNetwork();
	    } catch (Exception e) {
	    	e.printStackTrace();
	    }
	    
	    if (peerManager != null){
	    	netPeerGroup = peerManager.getNetPeerGroup();
	    }
		
		return (netPeerGroup != null);
	}

	public static PeerGroup getNetPeerGroup() {
		return netPeerGroup;
	}
	
}
