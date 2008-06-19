package org.hypergraphdb.peer.workflow;

import org.hypergraphdb.peer.protocol.Message;

public class Conversation
{
	private PeerRelatedActivity sendActivity;
	
	public Conversation(PeerRelatedActivity sendActivity, Object target, Message msg)
	{
		this.sendActivity = sendActivity;
	}
	
	protected void registerTransition()
	{
		//from state - to state
	}
	
	
}
