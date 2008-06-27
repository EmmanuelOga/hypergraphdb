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
	
	public InterestsPeerFilterEvaluator(PeerInterface peerInterface, HyperGraph hg, HGHandle handle)
	{
		this.peerInterface = peerInterface;
		this.hg = hg;
		this.handle = handle;
	}
	
	public boolean shouldSend(Object target)
	{
		System.out.println("InterestsPeerFilterEvaluator: evaluating " + handle + " for " + target);
		
		HGAtomPredicate pred = peerInterface.getPeerNetwork().getAtomInterests(target);
		return pred.satisfies(hg, handle);
	}
	
}
