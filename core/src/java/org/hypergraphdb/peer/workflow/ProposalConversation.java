package org.hypergraphdb.peer.workflow;

import org.hypergraphdb.peer.PeerInterface;
import org.hypergraphdb.peer.protocol.Message;
import org.hypergraphdb.peer.protocol.Performative;

public class ProposalConversation extends Conversation<ProposalConversation.State>
{
	public enum State {Started, Proposed, Accepted, Rejected, Confirmed, Disconfirmed};

	public ProposalConversation(PeerRelatedActivity sendActivity, PeerInterface peerInterface, Message msg)
	{
		super(sendActivity, peerInterface, msg);
		
		//serverside flow
		registerPerformativeTransition(State.Proposed, Performative.Accept, State.Accepted);

		//client side flow
		registerPerformativeTransition(State.Started, Performative.Proposal, State.Proposed);
		
		registerPerformativeTransition(State.Accepted, Performative.Confirm, State.Confirmed);
		registerPerformativeTransition(State.Accepted, Performative.Disconfirm, State.Disconfirmed);
		
		setState(State.Started);
	}

	/**
	 * called by server task when a proposal is to be sent
	 * @param msg
	 * @return
	 */
	public boolean propose(Message msg)
	{		
		if (compareAndSetState(State.Started, State.Proposed))
		{
			msg.setPerformative(Performative.Proposal);
			setMessage(msg);
			sendMessage();
			return true;
		}
		return false;
	}
	
	/**
	 * called by client task when accepting
	 * @param msg
	 */
	public boolean accept(Message msg)
	{
		if (compareAndSetState(State.Proposed, State.Accepted))
		{
			msg.setPerformative(Performative.Accept);
			setMessage(msg);
			sendMessage();
			
			return true;
		}
		
		return false;
	}
	
	/**
	 * called by client when rejecting
	 * @param msg
	 */
	public void reject(Message msg)
	{
		if (compareAndSetState(State.Proposed, State.Rejected))
		{
			//send acceptance
		}

	}
	
	/**
	 * called by server when confirming
	 * @param msg
	 */
	public boolean confirm(Message msg)
	{
		System.out.println("ProposalConversation: confirm");
		if (compareAndSetState(State.Accepted, State.Confirmed))
		{
			msg.setPerformative(Performative.Confirm);
			setMessage(msg);
			sendMessage();

			return true;
		}
		return false;
	}
	
	/**
	 * called by server when disconfirming
	 * @param msg
	 */
	public void disconfirm(Message msg)
	{
		if (compareAndSetState(State.Accepted, State.Disconfirmed))
		{
			//send acceptance
		}
	}
	
	
}
