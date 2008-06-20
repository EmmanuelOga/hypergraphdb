package org.hypergraphdb.peer.workflow;

import java.util.Timer;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.peer.HyperGraphPeer;
import org.hypergraphdb.peer.PeerInterface;
import org.hypergraphdb.peer.Subgraph;
import org.hypergraphdb.peer.protocol.Message;

public class RememberActivityServer extends TaskActivity<RememberActivityServer.State>
{
	private enum State {Waiting, Deciding, WaitingProposalState, Working, Done, Started, HandleAccept, HandleAccepted, HandleRejected};
	
	private HyperGraphPeer peer;
	
	public RememberActivityServer(PeerInterface peerInterface, HyperGraphPeer peer)
	{
		super(peerInterface);
		this.peer = peer;
		
		setState(State.Started);
	}
	
	public RememberActivityServer(PeerInterface peerInterface, HyperGraphPeer peer, Message msg)
	{
		super(peerInterface, msg.getTaskId());
		this.peer = peer;

		setState(State.Started);
	
		init();
		PeerRelatedActivity activity = (PeerRelatedActivity)getPeerInterface().newSendActivityFactory().createActivity();
		ProposalConversation conversation = new ProposalConversation(activity, getPeerInterface(), msg);
		
		Message reply = getReply(msg);
		
		registerConversation(conversation, reply.getConversationId());
		conversation.propose(reply);
	}

	
	
	public void init()
	{
		registerConversationHandler(State.Started, ProposalConversation.State.Accepted, "handleAccept", State.HandleAccepted);
		registerConversationHandler(State.Started, ProposalConversation.State.Rejected, "handleReject", State.HandleRejected);

		
/*		registerConversationTrigger(State.Accepted, ProposalConversation.State.Confirmed, "handleConfirm");
		registerConversationTrigger(State.Accepted, ProposalConversation.State.Disconfirmed, "handleDisconfirm");
*/		/**/
		
/* 		registerReceiveHook(Performative.CallForProposal, "decide", State.Waiting, State.Deciding);
 		registerReceiveHook(Performative.Accept, "handleAccept", State.WaitingProposalState, State.Working);
		registerReceiveHook(Performative.RejectProposal, "handleReject", State.WaitingProposalState, State.Working);		
*/	}
	
	public void run()
	{
//		startConversation();
		//get call for proposal
		
		
		//decide to propose/reject/forward ...
		
		//if propose wait for accept/reject
	}

	public State decide(Message msg)
	{
		System.out.println("RememberActivityServer: deciding");
		/*		
		//TODO for now just accept anything
		if (true)
		{
			//TODO add target
			Message reply = getReply(Performative.Proposal, msg);
			
			PeerRelatedActivity activity = (PeerRelatedActivity)getPeerInterface().newSendActivityFactory().createActivity();
			
			activity.setMessage(reply);
			activity.setTarget(msg.getReplyTo());
			new Thread(activity).start();
			//ActivityHelper.start(activity);
		}
*/
		
		return State.WaitingProposalState;
	}
	
	public State handleAccept(AbstractActivity<?> conversation)
	{		
		System.out.println("RememberActivityServer: acccepting");

		ProposalConversation conv = (ProposalConversation)conversation;
		Message msg = ((Conversation<?>)conversation).getMessage();
		Subgraph subgraph = (Subgraph) msg.getContent();
		HGHandle handle = peer.addSubgraph(subgraph);
		
		System.out.println("RememberActivityServer: added " + handle);

		Message reply = getReply(msg);		
		reply.setContent(handle);
		
		conv.confirm(reply);

		return State.Done;
	}
	
	public State handleReject(AbstractActivity<?> conversation)
	{
		//why?
		
		return State.Done;
	}
	
	public void startWithTimeout(Timer arg0, long arg1)
	{

	}

	public static class RememberTaskServerFactory implements TaskFactory
	{
		private HyperGraphPeer peer;
		public RememberTaskServerFactory(HyperGraphPeer peer)
		{
			this.peer = peer;
		}
		public TaskActivity<?> newTask(PeerInterface peerInterface, Message msg)
		{
			return new RememberActivityServer(peerInterface, peer, msg);
		}
		
	}

	protected void startTask()
	{
		// TODO Auto-generated method stub
		
	}

}
