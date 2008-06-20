package org.hypergraphdb.peer.workflow;

import java.util.HashMap;
import java.util.UUID;

import net.jxta.impl.id.UUID.UUIDFactory;

import org.hypergraphdb.peer.PeerInterface;
import org.hypergraphdb.peer.protocol.Message;
import org.hypergraphdb.peer.protocol.Performative;
import org.hypergraphdb.util.Pair;

public class Conversation<StateType> extends AbstractActivity<StateType>
{
	private static final UUID NULL_UUID = new UUID(0L, 0L);
	
	private PeerRelatedActivity sendActivity;
	private PeerInterface peerInterface;
	private Message msg;

	private HashMap<Pair<StateType, Performative>, StateType> performativeTransitions = new HashMap<Pair<StateType,Performative>, StateType>();
	
	public Conversation(PeerRelatedActivity sendActivity, PeerInterface peerInterface, Message msg)
	{
		if (msg.getConversationId().equals(NULL_UUID))
		{
			msg.setConversationId(UUID.randomUUID());
		}
		this.sendActivity = sendActivity;
		this.msg = msg;
		this.peerInterface = peerInterface;
		
		this.sendActivity.setTarget(msg.getReplyTo());
	}


	protected void doRun()
	{
		
	}
	
	protected void registerPerformativeTransition(StateType fromState, Performative performative, StateType toState)
	{
		performativeTransitions.put(new Pair<StateType, Performative>(fromState, performative), toState);
	}

	public void handleIncomingMessage(Message msg)
	{
		StateType state = getState();
		
		Pair<StateType, Performative> key = new Pair<StateType, Performative>(state, msg.getPerformative());
		StateType newState = performativeTransitions.get(key);

		if ((newState != null) && compareAndSetState(state, newState))
		{
			//new state set
			this.msg = msg;
			stateChanged();
		}else{
			
			//say don't understand
		}
	}

	protected void sendMessage()
	{
		sendActivity.setMessage(msg);
		peerInterface.execute(sendActivity);
	}
	
	public Message getMessage()
	{
		return msg;
	}

	public void setMessage(Message msg)
	{
		this.msg = msg;
	}
}
