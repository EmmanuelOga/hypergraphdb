package org.hypergraphdb.peer.workflow;

import org.apache.servicemix.beanflow.AbstractActivity;
import org.hypergraphdb.peer.protocol.Message;

public abstract class PeerRelatedActivity extends AbstractActivity
{
	protected Object target;
	protected Message msg;
	
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
