package org.hypergraphdb.peer;

import org.hypergraphdb.peer.protocol.Message;

/**
 * @author Cipri Costa
 * A runnable object that executes a task against a given peer 
 */
public abstract class PeerRelatedActivity implements Runnable
{
	protected Object target;
	protected Message msg;
	
	public PeerRelatedActivity()
	{
	}
	
	public Object getTarget()
	{
		return target;
	}

	public void setTarget(Object target)
	{
		this.target = target;
	}

	public Message getMessage()
	{
		return msg;
	}

	public void setMessage(Message msg)
	{
		this.msg = msg;
	}
	
}
