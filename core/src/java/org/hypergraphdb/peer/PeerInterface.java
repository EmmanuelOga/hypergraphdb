package org.hypergraphdb.peer;

import java.util.UUID;

import org.hypergraphdb.peer.protocol.OldMessage;
import org.hypergraphdb.peer.protocol.Performative;
import org.hypergraphdb.peer.workflow.ActivityFactory;
import org.hypergraphdb.peer.workflow.ConversationActivity;
import org.hypergraphdb.peer.workflow.ConversationFactory;
import org.hypergraphdb.peer.workflow.PeerFilter;


public interface PeerInterface extends Runnable{
	boolean configure(Object configuration);
	
	//factory methods to obtain activities that are specific to the peer implementation
	PeerFilter newFilterActivity();
	ActivityFactory newSendActivityFactory();

	//TODO remove
	Object forward(Object destination, OldMessage msg);


	/**
	 * Register a ConversationFactory that will be used to create conversations for messages 
	 * that are not in a conversation that exist on the peer. 
	 * 
	 * @param performative
	 * @param action
	 * @param convFactory
	 */
	void registerActivity(Performative performative, String action, ConversationFactory convFactory);

	/**
	 * registers existing conversation handlers. The peer will route any incoming message to that conversation
	 * 
	 * @param conversationId
	 * @param convHandler
	 */
	void registerReceiveHook(UUID conversationId, ConversationActivity<?> convHandler);
}
