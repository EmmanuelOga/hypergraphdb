package org.hypergraphdb.peer.workflow;

import java.util.UUID;

import org.hypergraphdb.peer.PeerInterface;

public interface ConversationFactory
{
	ConversationActivity<?> newConversation(PeerInterface peerInterface, UUID conversationId);
}
