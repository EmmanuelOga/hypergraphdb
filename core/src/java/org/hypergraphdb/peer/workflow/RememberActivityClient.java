package org.hypergraphdb.peer.workflow;

import java.util.Iterator;
import java.util.Timer;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.peer.PeerInterface;
import org.hypergraphdb.peer.Subgraph;
import org.hypergraphdb.peer.protocol.Message;
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
	public void startActivity()
	{
		ActivityFactory activityFactory = getPeerInterface().newSendActivityFactory();
		PeerFilter peerFilter = getPeerInterface().newFilterActivity();
		peerFilter.setTargetDescription(targetDescription);

		Iterator<Object> it = peerFilter.iterator();
		while (it.hasNext())
		{
			Object target = it.next();
		
			Message msg = new Message(Performative.CallForProposal, Message.REMEMBER_ACTION);
			
			PeerRelatedActivity activity = (PeerRelatedActivity)activityFactory.createActivity();
			activity.setTarget(target);
			new Thread(activity).start();
		}
		
	}
	public void run()
	{
		startConversation();
		init();
		startActivity();
		
		//PeerFilter peerFilter = getPeerInterface().newFilterActivity();
///		peerFilter.setMessage(new Message(Performative.CallForProposal, Message.REMEMBER_ACTION, getConversationId()));
		//peerFilter.setTargetDescription(targetDescription);
		//peerFilter.setActivityFactory(getPeerInterface().newSendActivityFactory());
		
		
//		msg.setConversationId(conversationId);
		
		
/*		peerInterface.registerReceiveHook(conversationId, Performative.Proposal, "handleProposal");
		peerInterface.registerReceiveHook(conversationId, Performative.Inform, "handleInform");
*/		
		//ActivityHelper.start(receiveActivity);

		System.out.println("!!!!!!!!!!!!Before filter");
		//new Thread(peerFilter).start();
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
		
		//stop();
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
