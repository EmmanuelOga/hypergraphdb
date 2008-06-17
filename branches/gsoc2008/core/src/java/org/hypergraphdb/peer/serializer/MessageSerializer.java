package org.hypergraphdb.peer.serializer;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import org.hypergraphdb.peer.protocol.Message;
import org.hypergraphdb.peer.protocol.Performative;

public class MessageSerializer implements SerializerMapper, HGSerializer
{

	public HGSerializer accept(Class<?> clazz)
	{
		if (Message.class.isAssignableFrom(clazz)) return this;
		else return null;
	}

	public HGSerializer getSerializer()
	{
		return this;
	}

	public Object readData(InputStream in)
	{
		Performative performative = Performative.values()[SerializationUtils.deserializeInt(in)];
		String action = SerializationUtils.deserializeString(in);
		UUID conversationID = SerializationUtils.deserializeUUID(in);
		String replyTo = SerializationUtils.deserializeString(in);
		
		Message msg = new Message(performative, action, conversationID);
		msg.setReplyTo(replyTo);
		return msg;
	}

	public void writeData(OutputStream out, Object data)
	{
		SerializationUtils.serializeInt(out, DefaultSerializerManager.MESSAGE_SERIALIZER_ID);

		Message msg = (Message)data;
		SerializationUtils.serializeInt(out, msg.getPerformative().ordinal());
		SerializationUtils.serializeString(out, msg.getAction());
		SerializationUtils.serializeUUID(out, msg.getConversationId());
		SerializationUtils.serializeString(out, msg.getReplyTo());
		
		
		
	}

}
