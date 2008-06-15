package org.hypergraphdb.peer;

import org.hypergraphdb.peer.protocol.Message;
import org.hypergraphdb.peer.workflow.ActivityFactory;
import org.hypergraphdb.peer.workflow.PeerFilterActivity;


public interface PeerForwarder {
	boolean configure(Object configuration);
	
	PeerFilterActivity newFilterActivity();
	ActivityFactory newSendActivityFactory();
	
	Object forward(Object destination, Message msg);
}
