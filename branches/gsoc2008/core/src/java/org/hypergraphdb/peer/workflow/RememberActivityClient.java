package org.hypergraphdb.peer.workflow;

import java.util.Iterator;
import java.util.Timer;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.peer.PeerInterface;
import org.hypergraphdb.peer.Subgraph;
import org.hypergraphdb.peer.protocol.Message;
import org.hypergraphdb.peer.protocol.Performative;

public class RememberActivityClient extends TaskActivity<RememberActivityClient.State>
{
	private enum State {Started, Accepted, HandleProposal, HandleProposalResponse, Done};
	
	private Object targetDescription;
	private Subgraph subgraph;
	private HGHandle result;
	
	public RememberActivityClient(PeerInterface peerInterface, Object targetDescription, Subgraph subgraph)
	{
		super(peerInterface);
		this.targetDescription = targetDescription;
		this.subgraph = subgraph;
		
		setState(State.Started);
	}

	public void init()
	{
		registerConversationHandler(State.Started, ProposalConversation.State.Proposed, "handleProposal", State.HandleProposal);
		
		registerConversationHandler(State.Accepted, ProposalConversation.State.Confirmed, "handleConfirm", State.HandleProposalResponse);
		registerConversationHandler(State.Accepted, ProposalConversation.State.Disconfirmed, "handleDisconfirm", State.HandleProposalResponse);		
	}
	
	protected void startTask()
	{		
		init();
		
		ActivityFactory activityFactory = getPeerInterface().newSendActivityFactory();
		PeerFilter peerFilter = getPeerInterface().newFilterActivity();
		peerFilter.setTargetDescription(targetDescription);

		peerFilter.filterTargets();
		Iterator<Object> it = peerFilter.iterator();
		while (it.hasNext())
		{
			Object target = it.next();
		
			Message msg = new Message(Performative.CallForProposal, Message.REMEMBER_ACTION);
			msg.setTaskId(getTaskId());
			
			PeerRelatedActivity activity = (PeerRelatedActivity)activityFactory.createActivity();
			activity.setTarget(target);
			activity.setMessage(msg);
			
			getPeerInterface().execute(activity);
		}
		
		
	}
	
	/**
	 * called when the task receives a proposal
	 */
	protected Conversation<?> createNewConversation(Message msg)
	{
		//TODO refactor
		ActivityFactory activityFactory = getPeerInterface().newSendActivityFactory();
		PeerRelatedActivity activity = (PeerRelatedActivity)activityFactory.createActivity();

		return new ProposalConversation(activity, getPeerInterface(), msg);

	}
	
	public State handleProposal(AbstractActivity<?> fromActivity)
	{
		System.out.println("RememeberTaskClient: handleProposal");
		//there is a proposal, handle that
		ProposalConversation conversation = (ProposalConversation)fromActivity;
		
		//decide to accept or not ... for now just accept
		if (true)
		{
			Message reply = getReply(conversation.getMessage());
			reply.setContent(subgraph);
			
			conversation.accept(reply);
		}
		
		//return appropriate state
		return State.Accepted;
	}
	
	protected State handleConfirm(AbstractActivity<?> fromActivity)
	{
		result = (HGHandle) ((ProposalConversation)fromActivity).getMessage().getContent();

		return State.Done;
	}
	
	protected State handleDisconfirm(AbstractActivity<?> fromActivity)
	{
		//there is a disconfirm
		
		//back to get proposal ...
		return State.Started;
	}
	
}
