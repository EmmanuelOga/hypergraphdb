package org.hypergraphdb.peer;

import java.util.UUID;

import org.hypergraphdb.peer.protocol.OldMessage;
import org.hypergraphdb.peer.protocol.Performative;
import org.hypergraphdb.peer.workflow.ActivityFactory;
import org.hypergraphdb.peer.workflow.PeerRelatedActivity;
import org.hypergraphdb.peer.workflow.TaskActivity;
import org.hypergraphdb.peer.workflow.TaskFactory;
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
	void registerTaskFactory(Performative performative, String action, TaskFactory convFactory);

	/**
	 * registers existing conversation handlers. The peer will route any incoming message to that conversation
	 * 
	 * @param conversationId
	 * @param convHandler
	 */
	void registerTask(UUID taskId, TaskActivity<?> task);
	
	void execute(PeerRelatedActivity activity);
}
