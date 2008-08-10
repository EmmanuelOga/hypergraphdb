package org.hypergraphdb.peer.jxta;

import java.util.ArrayList;

import net.jxta.document.Advertisement;
import net.jxta.protocol.PeerAdvertisement;
import net.jxta.protocol.PipeAdvertisement;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGPersistentHandle;
import org.hypergraphdb.peer.HyperGraphPeer;
import org.hypergraphdb.peer.PeerFilterEvaluator;
import org.hypergraphdb.peer.RemotePeer;
import org.hypergraphdb.peer.StorageService;
import org.hypergraphdb.peer.StorageService.Operation;
import org.hypergraphdb.peer.workflow.QueryTaskClient;
import org.hypergraphdb.peer.workflow.RememberTaskClient;
import org.hypergraphdb.query.HGQueryCondition;

/**
 * @author Cipri Costa
 * Remote peer implementation based on JXTA.
 */
public class JXTARemotePeer extends RemotePeer
{
	/**
	 * The advertisement of the remote peer.
	 */
	private Advertisement adv;
	
	public JXTARemotePeer(Advertisement adv)
	{		
		this.adv = adv;
		setName(((PipeAdvertisement)adv).getName());
	}
	
	/* (non-Javadoc)
	 * @see org.hypergraphdb.peer.RemotePeer#query(org.hypergraphdb.query.HGQueryCondition, boolean)
	 */
	@Override
	public ArrayList<?> query(HGQueryCondition condition, boolean getObjects)
	{
		ArrayList<Object> targets = new ArrayList<Object>();
		targets.add(adv);
		
		QueryTaskClient queryTask = new QueryTaskClient(getLocalPeer().getPeerInterface(), getLocalPeer().getTempDb(), targets.iterator(), condition, getObjects);
		queryTask.run();
		
		return queryTask.getResult();
	}
	
	/* (non-Javadoc)
	 * @see org.hypergraphdb.peer.RemotePeer#get(org.hypergraphdb.HGHandle)
	 */
	@Override
	public Object get(HGHandle handle)
	{
		ArrayList<Object> targets = new ArrayList<Object>();
		targets.add(adv);

		QueryTaskClient queryTask = new QueryTaskClient(getLocalPeer().getPeerInterface(), getLocalPeer().getTempDb(), targets.iterator(), handle);
		queryTask.run();
		
		ArrayList<?> result = queryTask.getResult();

		if (result.size() > 0) return result.get(0);
		else return null;
	}

	/* (non-Javadoc)
	 * @see org.hypergraphdb.peer.RemotePeer#add(java.lang.Object)
	 */
	@Override
	public HGHandle add(Object atom)
	{
		RememberTaskClient activity = new RememberTaskClient(getLocalPeer().getPeerInterface(), atom, getLocalPeer().getLog(), getLocalPeer().getTempDb(), null, adv, StorageService.Operation.Create);
		activity.run();
		return activity.getResult();
	}
	
	/* (non-Javadoc)
	 * @see org.hypergraphdb.peer.RemotePeer#remove(org.hypergraphdb.HGPersistentHandle)
	 */
	@Override
	public HGHandle remove(HGPersistentHandle handle)
	{
		RememberTaskClient activity = new RememberTaskClient(getLocalPeer().getPeerInterface(), null, getLocalPeer().getLog(), getLocalPeer().getTempDb(), handle, adv, Operation.Remove);

		activity.run();
		return activity.getResult();
	}
	
	/* (non-Javadoc)
	 * @see org.hypergraphdb.peer.RemotePeer#replace(org.hypergraphdb.HGPersistentHandle, java.lang.Object)
	 */
	@Override 
	public void replace(HGPersistentHandle handle, Object atom)
	{
	
		RememberTaskClient activity = new RememberTaskClient(getLocalPeer().getPeerInterface(), atom, getLocalPeer().getLog(), getLocalPeer().getTempDb(), handle, adv, Operation.Update);
		activity.run();
	}
}
