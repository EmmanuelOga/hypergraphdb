package org.hypergraphdb.peer;

import java.util.ArrayList;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGPersistentHandle;
import org.hypergraphdb.query.HGQueryCondition;

/**
 * @author Cipri Costa
 *
 * Offers an interface to a remote peer. The peers are identified by name. 
 * 
 * Subclasses will implement the actual communication with the remote peer (get/add/remove/replace + query).
 * 
 */
public abstract class RemotePeer
{
	private String name;
	private HyperGraphPeer localPeer;
	
	public RemotePeer()
	{
	}
	
	public RemotePeer(String name)
	{
		this.name = name;
	}
	
	/**
	 * Executes a query on the remote peer
	 * 
	 * @param condition a HGQueryCondition to be executed on the remote peer.
	 * @param getObjects if true the actual objects are returned, otherwise the client will just get a set of handles.
	 * @return The result of the remote query (depending on the getObjects parameter the list will contain the objects of just the handles)
	 */
	public abstract ArrayList<?> query(HGQueryCondition condition, boolean getObjects);
	/**
	 * 
	 * @param handle The handle of the atom to be retrieved
	 * @return The atom with the given handle from the remote peer. If no such handle exists on the remote peer, the function 
	 * will return null
	 */
	public abstract Object get(HGHandle handle);
	/**
	 * Adds the atom on the remote peer. 
	 * The operation is implemented using the replication mechanism, so, even if it fails, it is registered in the logs and will be 
	 * sent to the target when the target will start a catch-up phase.
	 * @param atom The atom to be added
	 * @return
	 */
	public abstract HGHandle add(Object atom);
	/**
	 * Removes the handle from the remote peer. 
	 * The operation is implemented using the replication mechanism, so, even if it fails, it is registered in the logs and will be 
	 * sent to the target when the target will start a catch-up phase.
	 * @param handle the handle to remove from the remote peer.
	 * @return
	 */
	public abstract HGHandle remove(HGPersistentHandle handle);
	/**
	 * Replaces the atom with the given handle on the remote peer. 
	 * The operation is implemented using the replication mechanism, so, even if it fails, it is registered in the logs and will be 
	 * sent to the target when the target will start a catch-up phase.
	 * 
	 * @param handle the handle of the atom to be replaced
	 * @param atom the new atom
	 */
	public abstract void replace(HGPersistentHandle handle, Object atom);

	public String getName()
	{
		return name;
	}
	public void setName(String name)
	{
		this.name = name;
	}
	public HyperGraphPeer getLocalPeer()
	{
		return localPeer;
	}
	public void setLocalPeer(HyperGraphPeer localPeer)
	{
		this.localPeer = localPeer;
	}
	
	
	public String toString()
	{
		String result = "RemotePeer(name=" + getName() + ")";
		return result;
	}
}
