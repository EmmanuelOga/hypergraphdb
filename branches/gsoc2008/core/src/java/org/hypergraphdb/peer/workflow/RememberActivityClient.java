package org.hypergraphdb.peer.workflow;

import java.util.Timer;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.servicemix.beanflow.AbstractActivity;
import org.apache.servicemix.beanflow.Activity;
import org.apache.servicemix.beanflow.ActivityHelper;
import org.hypergraphdb.peer.PeerInterface;
import org.hypergraphdb.peer.protocol.Message;
import org.hypergraphdb.peer.protocol.OldMessage;
import org.hypergraphdb.peer.protocol.Performative;

public class RememberActivityClient extends ConversationActivity<RememberActivityClient.State>
{
	private enum State {WaitingProposal, AcceptingProposal, HandlingInform, WaitingConfirm, WaitingProposalState, HandlingProposalState, Done};
	
	private Object targetDescription;
	
	private OldMessage msg;
	
	public RememberActivityClient(PeerInterface peerInterface, Object targetDescription)
	{
		super(peerInterface);
		this.targetDescription = targetDescription;
		
		setState(State.WaitingProposal);
	}

	public void init()
	{
		// TODO Auto-generated method stub
		registerReceiveHook(Performative.Proposal, "handleProposal", State.WaitingProposal, State.AcceptingProposal);
		registerReceiveHook(Performative.Inform, "handleInform", State.WaitingProposal, State.HandlingInform);
		registerReceiveHook(Performative.Confirm, "handleConfirm", State.WaitingProposalState, State.HandlingProposalState);
		registerReceiveHook(Performative.Disconfirm, "handleDisconfirm", State.WaitingProposalState, State.HandlingProposalState);
		
	}

	public void run()
	{
		PeerFilterActivity peerFilter = getPeerInterface().newFilterActivity();
		peerFilter.setMessage(new Message(Performative.CallForProposal, Message.REMEMBER_ACTION, getConversationId()));
		peerFilter.setTargetDescription(targetDescription);
		peerFilter.setActivityFactory(getPeerInterface().newSendActivityFactory());
		
//		msg.setConversationId(conversationId);
		
		
/*		peerInterface.registerReceiveHook(conversationId, Performative.Proposal, "handleProposal");
		peerInterface.registerReceiveHook(conversationId, Performative.Inform, "handleInform");
*/		
		//ActivityHelper.start(receiveActivity);

		ActivityHelper.start(peerFilter);
		
	}

	public State handleProposal()
	{
		//TODO decide if accept or reject
		//for the time being ... always accept
		if (true)
		{
			
			return State.WaitingConfirm;
		}else{
			//just wait for another proposal
			return State.WaitingProposal;
		}
	}
	
	public State handleInform()
	{
		//TODO information about how the message is forwarded ... 
		
		return State.AcceptingProposal;
	}
	
	public State handleConfirm()
	{
		return State.Done;
	}
	
	public State handleDisconfirm()
	{
		return State.AcceptingProposal;
	}
	
	@Override
	public void startWithTimeout(Timer arg0, long arg1)
	{
		// TODO Auto-generated method stub
	}

	public void setMessage(OldMessage msg)
	{
		this.msg = msg;
	}


	
}
