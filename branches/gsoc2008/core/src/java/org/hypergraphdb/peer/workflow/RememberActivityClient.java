package org.hypergraphdb.peer.workflow;

import java.util.Timer;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.servicemix.beanflow.AbstractActivity;
import org.apache.servicemix.beanflow.Activity;
import org.apache.servicemix.beanflow.ActivityHelper;
import org.hypergraphdb.HGHandle;
import org.hypergraphdb.peer.PeerInterface;
import org.hypergraphdb.peer.Subgraph;
import org.hypergraphdb.peer.protocol.Message;
import org.hypergraphdb.peer.protocol.OldMessage;
import org.hypergraphdb.peer.protocol.Performative;

public class RememberActivityClient extends ConversationActivity<RememberActivityClient.State>
{
	private enum State {WaitingProposal, AcceptingProposal, HandlingInform, WaitingConfirm, WaitingProposalState, HandlingProposalState, Done};
	
	private Object targetDescription;
	private Subgraph subgraph;
	private HGHandle result;
	
	public RememberActivityClient(PeerInterface peerInterface, Object targetDescription, Subgraph subgraph)
	{
		super(peerInterface);
		this.targetDescription = targetDescription;
		this.subgraph = subgraph;
		
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
		startConversation();
		init();
		
		PeerFilterActivity peerFilter = getPeerInterface().newFilterActivity();
		peerFilter.setMessage(new Message(Performative.CallForProposal, Message.REMEMBER_ACTION, getConversationId()));
		peerFilter.setTargetDescription(targetDescription);
		peerFilter.setActivityFactory(getPeerInterface().newSendActivityFactory());
		
		
//		msg.setConversationId(conversationId);
		
		
/*		peerInterface.registerReceiveHook(conversationId, Performative.Proposal, "handleProposal");
		peerInterface.registerReceiveHook(conversationId, Performative.Inform, "handleInform");
*/		
		//ActivityHelper.start(receiveActivity);

		System.out.println("!!!!!!!!!!!!Before filter");
		new Thread(peerFilter).start();
		//ActivityHelper.start(peerFilter);
		System.out.println("!!!!!!!!!!!!After filter");		
	}

	public State handleProposal(Message msg)
	{
		//TODO decide if accept or reject
		//for the time being ... always accept
		System.out.println("RememberActivityClient: deciding to accept or not");
		if (true)
		{
			Message reply = getReply(Performative.Accept, msg);
			
			reply.setContent(subgraph);
			PeerRelatedActivity activity = (PeerRelatedActivity)getPeerInterface().newSendActivityFactory().createActivity();
			
			activity.setMessage(reply);
			activity.setTarget(msg.getReplyTo());
			new Thread(activity).start();
			//ActivityHelper.start(activity);

			return State.WaitingConfirm;
		}else{
			//just wait for another proposal
			return State.WaitingProposal;
		}
	}
	
	public State handleInform(Message msg)
	{
		//TODO information about how the message is forwarded ... 
		
		return State.AcceptingProposal;
	}
	
	public State handleConfirm(Message msg)
	{
		result = (HGHandle)msg.getContent();
		
		stop();
		return State.Done;
	}
	
	public State handleDisconfirm(Message msg)
	{
		return State.AcceptingProposal;
	}
	

	public void startWithTimeout(Timer arg0, long arg1)
	{
		// TODO Auto-generated method stub
	}

	public HGHandle getResult()
	{
		return result;
	}
	
}
