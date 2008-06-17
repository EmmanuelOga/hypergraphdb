package org.hypergraphdb.peer;

import java.util.UUID;

import org.hypergraphdb.peer.protocol.OldMessage;
import org.hypergraphdb.peer.protocol.Performative;
import org.hypergraphdb.peer.workflow.ActivityFactory;
import org.hypergraphdb.peer.workflow.ConversationFactory;
import org.hypergraphdb.peer.workflow.PeerFilterActivity;


public interface PeerInterface extends Runnable{
	boolean configure(Object configuration);
	
	PeerFilterActivity newFilterActivity();
	ActivityFactory newSendActivityFactory();

	//TODO remove
	Object forward(Object destination, OldMessage msg);

	void registerActivity(Performative performative, String action, ConversationFactory convFactory);
	void registerReceiveHook(UUID conversationId, Performative performative, String handleFunc);
}
