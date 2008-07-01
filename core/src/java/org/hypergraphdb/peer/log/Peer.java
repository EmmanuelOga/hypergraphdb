package org.hypergraphdb.peer.log;


public class Peer
{
	Object peerId;
	Timestamp timestamp = new Timestamp();
	
	public Peer()
	{
		
	}
	public Peer(Object peerId)
	{
		this.peerId = peerId;
	}

	public Object getPeerId()
	{
		return peerId;
	}

	public void setPeerId(Object peerId)
	{
		this.peerId = peerId;
	}
	public Timestamp getTimestamp()
	{
		return timestamp;
	}
	public void setTimestamp(Timestamp timestamp)
	{
		this.timestamp = timestamp;
	}
	
	public String toString()
	{
		return "Peer: " + peerId + "; " + timestamp;
	}
	
}
