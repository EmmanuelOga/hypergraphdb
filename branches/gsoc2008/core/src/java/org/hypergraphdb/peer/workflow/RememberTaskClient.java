package org.hypergraphdb.peer.workflow;

import java.util.Iterator;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.peer.HGDBOntology;
import org.hypergraphdb.peer.PeerFilter;
import org.hypergraphdb.peer.PeerInterface;
import org.hypergraphdb.peer.PeerRelatedActivity;
import org.hypergraphdb.peer.PeerRelatedActivityFactory;
import org.hypergraphdb.peer.Subgraph;
import org.hypergraphdb.peer.protocol.Message;
import org.hypergraphdb.peer.protocol.Performative;

/**
 * @author Cipri Costa
 *
 * A task that performs the "client" side of the REMEMBER action. At the start of the task, it will send
 * "call for proposal" messages to peers using a <code>PeerFilter</code>. Any peer that decides to answer 
 * the call for proposal with a proposal will establish a conversation.
 * 
 * The task will only use <code>ProposalConversation</code> conversations. 
 * 
 * 
 */
public class RememberTaskClient extends TaskActivity<RememberTaskClient.State>
{
	private enum State {Started, Accepted, HandleProposal, HandleProposalResponse, Done};
	
	private Object targetDescription;
	private Subgraph subgraph;
	private HGHandle result;
	
	public RememberTaskClient(PeerInterface peerInterface, Object targetDescription, Subgraph subgraph)
	{
		super(peerInterface, State.Started, State.Done);
		this.targetDescription = targetDescription;
		this.subgraph = subgraph;
	}
	
	protected void startTask()
	{		
		//initialize
		registerConversationHandler(State.Started, ProposalConversation.State.Proposed, "handleProposal", State.HandleProposal);
		
		registerConversationHandler(State.Accepted, ProposalConversation.State.Confirmed, "handleConfirm", State.HandleProposalResponse);
		registerConversationHandler(State.Accepted, ProposalConversation.State.Disconfirmed, "handleDisconfirm", State.HandleProposalResponse);		

		//do startup tasks - filter peers and send messages
		PeerRelatedActivityFactory activityFactory = getPeerInterface().newSendActivityFactory();
		PeerFilter peerFilter = getPeerInterface().newFilterActivity();
		peerFilter.setTargetDescription(targetDescription);

		peerFilter.filterTargets();
		Iterator<Object> it = peerFilter.iterator();
		while (it.hasNext())
		{
			Object target = it.next();
		
			Message msg = getPeerInterface().getMessageFactory().createMessage();
			msg.setPerformative(Performative.CallForProposal);
			msg.setAction(HGDBOntology.REMEMBER_ACTION);
			msg.setTaskId(getTaskId());
			
			PeerRelatedActivity activity = (PeerRelatedActivity)activityFactory.createActivity();
			activity.setTarget(target);
			activity.setMessage(msg);
			
			getPeerInterface().execute(activity);
		}
		
		
	}
	
	/* (non-Javadoc)
	 * @see org.hypergraphdb.peer.workflow.TaskActivity#createNewConversation(org.hypergraphdb.peer.protocol.Message)
	 * 
	 * This function is called when the server started a conversation and the conversation has to be started on the cleint too.
	 */
	protected Conversation<?> createNewConversation(Message msg)
	{
		//TODO refactor
		PeerRelatedActivityFactory activityFactory = getPeerInterface().newSendActivityFactory();
		PeerRelatedActivity activity = (PeerRelatedActivity)activityFactory.createActivity();

		return new ProposalConversation(activity, getPeerInterface(), msg);
	}
	
	/**
	 * Called when one of the conversations enters the <code>Proposed</code> state while the task is in the 
	 * <code>Started</code> state. 
	 * 
	 * @param fromActivity
	 * @return
	 */
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
			
			//set the conversation in the Accepted state
			conversation.accept(reply);
		}
		
		//return appropriate state
		return State.Accepted;
	}
	
	/**
	 * Called when one of the conversations enters the <code>Confirmed</code> state while the task is in the 
	 * <code>Accepted</code> state. 
	 * 
	 * @param fromActivity
	 * @return
	 */
	public State handleConfirm(AbstractActivity<?> fromActivity)
	{
		result = (HGHandle) ((ProposalConversation)fromActivity).getMessage().getContent();

		return State.Done;
	}
	
	public State handleDisconfirm(AbstractActivity<?> fromActivity)
	{
		//there is a disconfirm
		
		//back to get proposal ...
		return State.Started;
	}
	
	public HGHandle getResult()
	{
		return result;
	}
}
