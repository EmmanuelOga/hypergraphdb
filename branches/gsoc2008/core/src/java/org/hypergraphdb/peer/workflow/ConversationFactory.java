package org.hypergraphdb.peer.workflow;

import org.hypergraphdb.peer.PeerInterface;

public interface ConversationFactory
{
	ConversationActivity<?> newConversation(PeerInterface peerInterface);
}
