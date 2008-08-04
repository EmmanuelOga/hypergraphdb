package org.hypergraphdb.peer.workflow;


import java.util.UUID;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.peer.HyperGraphPeer;
import org.hypergraphdb.peer.PeerInterface;
import org.hypergraphdb.peer.PeerRelatedActivity;
import org.hypergraphdb.peer.Subgraph;
import org.hypergraphdb.peer.log.Timestamp;
import static org.hypergraphdb.peer.HGDBOntology.*;
import static org.hypergraphdb.peer.Structs.*;
import static org.hypergraphdb.peer.Messages.*;

/**
 * @author Cipri Costa
 *
 * A task that performs the "server" side of the REMEMBER action. The task only manages a single 
 * conversation (with the client). The task is usually created when a call for proposal is received 
 * and decides in the startup phase whether to send a proposal or not. 
 */
public class RememberTaskServer extends TaskActivity<RememberTaskServer.State>
{
	protected enum State {Started, HandleAccepted, HandleRejected, Done};
	
	private HyperGraphPeer peer;
	private ProposalConversation conversation;
	private Timestamp last_version;
	private Timestamp current_version;

	public RememberTaskServer(PeerInterface peerInterface, HyperGraphPeer peer)
	{
		super(peerInterface, State.Started, State.Done);
		this.peer = peer;
		
		setState(State.Started);
	}
	
	public RememberTaskServer(PeerInterface peerInterface, HyperGraphPeer peer, Object msg)
	{
		//super(peerInterface, msg.getTaskId(), State.Started, State.Done);
		super(peerInterface, (UUID)getPart(msg, SEND_TASK_ID), State.Started, State.Done);
		this.peer = peer;

		//start the conversation
		PeerRelatedActivity activity = (PeerRelatedActivity)getPeerInterface().newSendActivityFactory().createActivity();
		conversation = new ProposalConversation(activity, getPeerInterface(), msg);	
		
		last_version = (Timestamp) getPart(msg, CONTENT, SLOT_LAST_VERSION);//(Timestamp) ((Document)msg).get("last_version");
		current_version = (Timestamp) getPart(msg, CONTENT, SLOT_CURRENT_VERSION);//((Document)msg).get("curent_version");
	}

	protected void startTask()
	{
		//initialize transitions
		registerConversationHandler(State.Started, ProposalConversation.State.Accepted, "handleAccept", State.HandleAccepted);
		registerConversationHandler(State.Started, ProposalConversation.State.Rejected, "handleReject", State.HandleRejected);

		//do startup task (propose)
		Object reply = getReply(conversation.getMessage());
		//registerConversation(conversation, reply.getConversationId());
		registerConversation(conversation, (UUID)getPart(reply, CONVERSATION_ID));
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
		Object msg = ((Conversation<?>)conversation).getMessage();
		Subgraph subgraph = (Subgraph) getPart(msg, CONTENT);//.getContent();
		
		
		HGHandle handle = null;
		Object peerId = getPeerInterface().getPeerNetwork().getPeerId(getPart(msg, REPLY_TO));//.getReplyTo());
		if (peer.getLog().registerRequest(peerId, last_version, current_version))
		{
			handle = peer.getStorage().addSubgraph(subgraph);
			
			peer.getLog().finishRequest(peerId, last_version, current_version);
			System.out.println("RememberActivityServer: added " + handle);
			
			Object reply = getReply(msg);
			combine(reply, struct(CONTENT, handle));

			conv.confirm(reply);
		}else{
			Object reply = getReply(msg);		
			
			conv.disconfirm(reply);
		}

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
		public TaskActivity<?> newTask(PeerInterface peerInterface, Object msg)
		{
			return new RememberTaskServer(peerInterface, peer, msg);
		}
		
	}
}
