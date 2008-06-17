package org.hypergraphdb.peer.workflow;

import java.util.UUID;

public class ReceiveActivity
{
	protected UUID msgId;

	public ReceiveActivity()
	{
		
	}
	
	public UUID getMsgId()
	{
		return msgId;
	}

	public void setMsgId(UUID msgId)
	{
		this.msgId = msgId;
	}
	
	
	
}
