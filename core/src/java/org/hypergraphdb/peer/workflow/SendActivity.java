package org.hypergraphdb.peer.workflow;

import org.apache.servicemix.beanflow.AbstractActivity;
import org.hypergraphdb.peer.protocol.Message;

public abstract class SendActivity extends AbstractActivity
{
	protected Object target;
	protected Message message;
	
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
		return message;
	}

	public void setMessage(Message message)
	{
		this.message = message;
	}
	
}
