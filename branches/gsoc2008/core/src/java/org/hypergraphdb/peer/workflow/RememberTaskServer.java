package org.hypergraphdb.peer.workflow;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.peer.HyperGraphPeer;
import org.hypergraphdb.peer.PeerInterface;
import org.hypergraphdb.peer.PeerRelatedActivity;
import org.hypergraphdb.peer.Subgraph;
import org.hypergraphdb.peer.protocol.Message;

/**
 * @author Cipri Costa
 *
 * A task that performs the "server" side of the REMEMBER action. The task only manages a single 
 * conversation (with the client). The task is usually created when a call for proposal is received 
 * and decides in the startup phase whether to send a proposal or not. 
 */
public class RememberTaskServer extends TaskActivity<RememberTaskServer.State>
{
	private enum State {Started, HandleAccepted, HandleRejected, Done};
	
	private HyperGraphPeer peer;
	ProposalConversation conversation;
	
	public RememberTaskServer(PeerInterface peerInterface, HyperGraphPeer peer)
	{
		super(peerInterface, State.Started, State.Done);
		this.peer = peer;
		
		setState(State.Started);
	}
	
	public RememberTaskServer(PeerInterface peerInterface, HyperGraphPeer peer, Message msg)
	{
		super(peerInterface, msg.getTaskId(), State.Started, State.Done);
		this.peer = peer;

		//start the conversation
		PeerRelatedActivity activity = (PeerRelatedActivity)getPeerInterface().newSendActivityFactory().createActivity();
		conversation = new ProposalConversation(activity, getPeerInterface(), msg);	
	}

	protected void startTask()
	{
		//initialize transitions
		registerConversationHandler(State.Started, ProposalConversation.State.Accepted, "handleAccept", State.HandleAccepted);
		registerConversationHandler(State.Started, ProposalConversation.State.Rejected, "handleReject", State.HandleRejected);

		//do startup task (propose)
		Message reply = getReply(conversation.getMessage());
		registerConversation(conversation, reply.getConversationId());
		conversation.propose(reply);
		
	}
	
	/**
	 * called when a conversation enters the <code>Accepted</code> state while the task is in the <code>Started</code> state.
	 * 
	 * @param conversation
	 * @return
	 */
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
		//TODO why?
		
		return State.Done;
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
			return new RememberTaskServer(peerInterface, peer, msg);
		}
		
	}
}
