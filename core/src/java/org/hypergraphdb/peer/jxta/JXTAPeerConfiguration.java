package org.hypergraphdb.peer.jxta;

import java.util.ArrayList;

public class JXTAPeerConfiguration {
	private String peerId;
	private ArrayList<String> peers = new ArrayList<String>();
	
	public JXTAPeerConfiguration(String peerId){
		this.peerId = peerId;
	}

	public String getPeerId() {
		return peerId;
	}

	public void setPeerId(String peerId) {
		this.peerId = peerId;
	}

	public ArrayList<String> getPeers() {
		return peers;
	}

	public void setPeers(ArrayList<String> peers) {
		this.peers = peers;
	}
	
	public void addPeer(String peerId){
		peers.add(peerId);
	}
	
}
