package org.hypergraphdb.peer.workflow;

import org.hypergraphdb.peer.protocol.Message;

public class CFPConversation extends Conversation
{
	private enum State {Proposal, Accept, Confirm, Reject, Done};

	public CFPConversation(PeerRelatedActivity sendActivity, Object target, Message msg)
	{
		super(sendActivity, target, msg);
		
		//start - cfp, proposal
		//cfp - Proposal - 
	}
	
	public void start()
	{
		//if call for proposal - enter cfp / else proposal
	}
	
	public void accept()
	{
		
	}
	
	public void reject()
	{
		
	}
	
	public void confirm()
	{
		
	}
}
