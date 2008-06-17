package org.hypergraphdb.peer.workflow;

import java.util.Timer;
import java.util.UUID;

import org.apache.servicemix.beanflow.ActivityHelper;
import org.hypergraphdb.HGHandle;
import org.hypergraphdb.peer.HyperGraphPeer;
import org.hypergraphdb.peer.PeerInterface;
import org.hypergraphdb.peer.Subgraph;
import org.hypergraphdb.peer.protocol.Message;
import org.hypergraphdb.peer.protocol.OldMessage;
import org.hypergraphdb.peer.protocol.Performative;

import sun.misc.Perf.GetPerfAction;

public class RememberActivityServer extends ConversationActivity<RememberActivityServer.State>
{
	private enum State {Waiting, Deciding, WaitingProposalState, Working, Done};
	
	private HyperGraphPeer peer;
	
	public RememberActivityServer(PeerInterface peerInterface, HyperGraphPeer peer)
	{
		super(peerInterface);
		this.peer = peer;
		
		setState(State.Waiting);
	}
	
	public RememberActivityServer(PeerInterface peerInterface, HyperGraphPeer peer, UUID conversationId)
	{
		super(peerInterface, conversationId);
		this.peer = peer;

		setState(State.Waiting);
	}

	public void init()
	{
 		registerReceiveHook(Performative.CallForProposal, "decide", State.Waiting, State.Deciding);
 		registerReceiveHook(Performative.Accept, "handleAccept", State.WaitingProposalState, State.Working);
		registerReceiveHook(Performative.RejectProposal, "handleReject", State.WaitingProposalState, State.Working);		
	}
	
	public void run()
	{
		//get call for proposal
		
		
		//decide to propose/reject/forward ...
		
		//if propose wait for accept/reject
	}

	public State decide(Message msg)
	{
		System.out.println("RememberActivityServer: deciding");
		
		//TODO for now just accept anything
		if (true)
		{
			//TODO add target
			Message reply = new Message(Performative.Proposal, msg.getAction(), msg.getConversationId());
			
			PeerRelatedActivity activity = (PeerRelatedActivity)getPeerInterface().newSendActivityFactory().createActivity();
			
			activity.setMessage(reply);
			activity.setTarget(msg.getReplyTo());
			ActivityHelper.start(activity);
		}

		
		return State.WaitingProposalState;
	}
	public State handleAccept(Message msg)
	{		
		//try to save
		//TODO get param
		//Subgraph subgraph = null;
		//HGHandle handle = peer.addSubgraph(subgraph);
		
		//send confirmation message
		//TODO: create reply
		
		return State.Done;
	}
	
	public State handleReject(Message msg)
	{
		//why?
		
		return State.Done;
	}
	
	public void startWithTimeout(Timer arg0, long arg1)
	{

	}

	public static class ConvFactory implements ConversationFactory
	{
		private HyperGraphPeer peer;
		public ConvFactory(HyperGraphPeer peer)
		{
			this.peer = peer;
		}
		public ConversationActivity<?> newConversation(PeerInterface peerInterface)
		{
			return new RememberActivityServer(peerInterface, peer);
		}
		
	}

}
