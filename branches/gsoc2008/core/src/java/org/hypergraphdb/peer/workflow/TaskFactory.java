package org.hypergraphdb.peer.workflow;

import java.util.UUID;

import org.hypergraphdb.peer.PeerInterface;
import org.hypergraphdb.peer.protocol.Message;

public interface TaskFactory
{
	TaskActivity<?> newTask(PeerInterface peerInterface, Message msg);
}
