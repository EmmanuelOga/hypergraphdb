package org.hypergraphdb.peer.protocol;

import java.util.UUID;

/**
 * @author Cipri Costa
 *
 * This interface gives access to basic properties of any message. 
 */
public interface Message
{
	String getAction();
	void setAction(String action);
	
	Performative getPerformative();
	void setPerformative(Performative performative);

	UUID getConversationId();
	void setConversationId(UUID conversationId);
	
	Object getReplyTo();
	void setReplyTo(Object replyTo);
	
	public Object getContent();
	public void setContent(Object content);

	public UUID getTaskId();
	public void setTaskId(UUID taskId);
}
