package org.hypergraphdb.peer;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.query.HGAtomPredicate;

/**
 * @author ciprian.costa
 * Evaluates if a peer passes the filtering process based on the interests it 
 * announced previously.
 */
public class InterestsPeerFilterEvaluator implements PeerFilterEvaluator
{
	private PeerInterface peerInterface;
	private HyperGraph hg;
	private HGHandle handle;
	
	public InterestsPeerFilterEvaluator(PeerInterface peerInterface, HyperGraph hg)
	{
		this.peerInterface = peerInterface;
		this.hg = hg;
	}
	
	public boolean shouldSend(Object target)
	{
		System.out.println("InterestsPeerFilterEvaluator: evaluating " + handle + " for " + target);
		
		HGAtomPredicate pred = peerInterface.getPeerNetwork().getAtomInterests(target);
		return pred.satisfies(hg, handle);
	}

	public PeerInterface getPeerInterface()
	{
		return peerInterface;
	}

	public void setPeerInterface(PeerInterface peerInterface)
	{
		this.peerInterface = peerInterface;
	}

	public HyperGraph getHg()
	{
		return hg;
	}

	public void setHg(HyperGraph hg)
	{
		this.hg = hg;
	}

	public HGHandle getHandle()
	{
		return handle;
	}

	public void setHandle(HGHandle handle)
	{
		this.handle = handle;
	}
	
}
