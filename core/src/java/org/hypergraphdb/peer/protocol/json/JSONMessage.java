package org.hypergraphdb.peer.protocol.json;

import java.util.UUID;

import org.hypergraphdb.peer.protocol.Message;
import org.hypergraphdb.peer.protocol.Performative;
import org.json.JSONObject;


/**
 * @author ciprian.costa
 * 
 * Basic implementation of the Message interface for JSON transport.
 */
public class JSONMessage extends JSONDocument implements Message, JSONSerializable
{
	private static final String KEY_CONVERSATION = "conv";
	private static final String KEY_ACTION = "act";
	private static final String KEY_PERFORMATIVE = "perf";
	private static final String KEY_REPLYTO = "replyTo";
	private static final String KEY_TASK = "task";
	private static final String KEY_CONTENT = "cont";
	
	public JSONMessage(boolean useTextOnly)
	{
		data = new JSONObject();
		this.useTextOnly = useTextOnly;
	}
	
	public JSONMessage(JSONObject jsonData, boolean useTextOnly)
	{
		data = jsonData;
		this.useTextOnly = useTextOnly;
	}

	public String getAction()
	{
		return (String) get(KEY_ACTION);
	}
	
	public void setAction(String action)
	{
		put(KEY_ACTION, action);
	}
	
	public Object getContent()
	{
		return get(KEY_CONTENT);
	}
	
	public void setContent(Object content)
	{
		put(KEY_CONTENT, content);
	}

	public UUID getConversationId()
	{
		String uuid = (String) get(KEY_CONVERSATION);
		if (uuid != null) return UUID.fromString(uuid);
		else return new UUID(0L, 0L);
	}
	public void setConversationId(UUID conversationId)
	{
		put(KEY_CONVERSATION, conversationId.toString());		
	}

	public Performative getPerformative()
	{
		String perf = (String) get(KEY_PERFORMATIVE);
		return Performative.valueOf(Performative.class, perf);
	}
	public void setPerformative(Performative performative)
	{
		put(KEY_PERFORMATIVE, performative.toString());
	}

	public Object getReplyTo()
	{
		return get(KEY_REPLYTO);
	}
	public void setReplyTo(Object replyTo)
	{
		put(KEY_REPLYTO, replyTo);
		
	}
	
	public UUID getTaskId()
	{
		String uuid = (String) get(KEY_TASK);
		if (uuid != null) return UUID.fromString(uuid);
		else return new UUID(0L, 0L);
	}
	public void setTaskId(UUID taskId)
	{
		put(KEY_TASK, taskId.toString());
	}
}
