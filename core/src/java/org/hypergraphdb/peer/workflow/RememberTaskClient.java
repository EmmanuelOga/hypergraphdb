package org.hypergraphdb.peer.workflow;

import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGPersistentHandle;
import org.hypergraphdb.HyperGraph;
import static org.hypergraphdb.peer.HGDBOntology.*;
import org.hypergraphdb.peer.InterestEvaluator;
import org.hypergraphdb.peer.PeerFilter;
import org.hypergraphdb.peer.PeerInterface;
import org.hypergraphdb.peer.PeerRelatedActivity;
import org.hypergraphdb.peer.PeerRelatedActivityFactory;
import org.hypergraphdb.peer.Subgraph;
import org.hypergraphdb.peer.log.Log;
import org.hypergraphdb.peer.log.LogEntry;
import org.hypergraphdb.peer.log.Timestamp;
import static org.hypergraphdb.peer.Structs.*;
import static org.hypergraphdb.peer.Messages.*;
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
	protected enum State {Started, Accepted, HandleProposal, HandleProposalResponse, Done};

	private HGHandle result;
	private InterestEvaluator evaluator;
	private Object value;
	private Log log;
	private LogEntry entry;
	
	//TODO replace. for now just assumming everyone is online 
	private AtomicInteger count = new AtomicInteger(1);
	PeerFilter peerFilter;
	private Object targetPeer;
	
	public RememberTaskClient(PeerInterface peerInterface, Object value, Log log, HyperGraph hg)
	{
		super(peerInterface, State.Started, State.Done);
		this.value = value;
		this.log = log;
		
		evaluator = new InterestEvaluator(peerInterface, hg);
	}
	
	public RememberTaskClient(PeerInterface peerInterface, LogEntry entry, Object targetPeer, Log log)
	{
		super(peerInterface, State.Started, State.Done);
		
		this.entry = entry;
		this.targetPeer = targetPeer;
		this.log = log;
	}

	protected void startTask()
	{		
		//initialize
		registerConversationHandler(State.Started, ProposalConversation.State.Proposed, "handleProposal", State.HandleProposal);
		
		registerConversationHandler(State.Accepted, ProposalConversation.State.Confirmed, "handleConfirm", State.HandleProposalResponse);
		registerConversationHandler(State.Accepted, ProposalConversation.State.Disconfirmed, "handleDisconfirm", State.HandleProposalResponse);		

		//do startup tasks - filter peers and send messages
		PeerRelatedActivityFactory activityFactory = getPeerInterface().newSendActivityFactory();

		if (targetPeer == null)
		{
			peerFilter = getPeerInterface().newFilterActivity(evaluator);
		}
		
		if (entry == null)
		{
			entry = log.createLogEntry(value);
			evaluator.setHandle(entry.getLogEntryHandle());
			log.addEntry(entry, peerFilter);
		}
		
		if (peerFilter != null)
		{
			Iterator<Object> it = peerFilter.iterator();
			while (it.hasNext())
			{
				Object target = it.next();
				sendCallForProposal(target, activityFactory);
			}
		}else{
			sendCallForProposal(targetPeer, activityFactory);
		}
		
		if (count.decrementAndGet() == 0) setState(State.Done);
	}
	private void sendCallForProposal(Object target, PeerRelatedActivityFactory activityFactory)
	{
		count.incrementAndGet();
		
		Object msg = createMessage(Performative.CallForProposal, REMEMBER_ACTION, getTaskId());
		combine(msg, struct(
				CONTENT, struct(
						SLOT_LAST_VERSION, entry.getLastTimestamp(getPeerInterface().getPeerNetwork().getPeerId(target)),
						SLOT_CURRENT_VERSION, entry.getTimestamp()
					))
		);

		PeerRelatedActivity activity = (PeerRelatedActivity)activityFactory.createActivity();
		activity.setTarget(target);
		activity.setMessage(msg);
		
		getPeerInterface().execute(activity);

	}
	/* (non-Javadoc)
	 * @see org.hypergraphdb.peer.workflow.TaskActivity#createNewConversation(org.hypergraphdb.peer.protocol.Message)
	 * 
	 * This function is called when the server started a conversation and the conversation has to be started on the cleint too.
	 */
	protected Conversation<?> createNewConversation(Object msg)
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
			Object reply = getReply(conversation.getMessage());
			combine(reply, struct(CONTENT, object(entry.getData())));
//			reply.setContent(entry.getData());
			
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
		Object msg = ((ProposalConversation)fromActivity).getMessage();		
		result = (HGHandle)getPart(msg, CONTENT);
		//result = (HGHandle) msg.getContent();

		//record the message in the log
		//Object peerId = getPeerInterface().getPeerNetwork().getPeerId(msg.getReplyTo());
		Object peerId = getPeerInterface().getPeerNetwork().getPeerId(getPart(msg, REPLY_TO));
		log.confirmFromPeer(peerId, entry.getTimestamp());
		
		if (count.decrementAndGet() == 0) return State.Done;
		else return State.Started;
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
