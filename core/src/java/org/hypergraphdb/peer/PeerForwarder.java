package org.hypergraphdb.peer;

import org.hypergraphdb.peer.protocol.Message;


public interface PeerForwarder {
	boolean configure(Object configuration);
	
	Object forward(Message msg);
}
