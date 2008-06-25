package org.hypergraphdb.peer.protocol.json;

import java.util.HashMap;

import org.hypergraphdb.peer.protocol.Message;
import org.hypergraphdb.peer.protocol.MessageFactory;
import org.hypergraphdb.peer.serializer.DefaultSerializerManager;

/**
 * @author ciprian.costa
 * A factory class that knows how to create <code>JSONMessage</code> instances
 */
public class JSONMessageFactory implements MessageFactory
{
	private static final String FORCE_TEXT_ONLY = "ForceTextOnly";	

	private boolean useTextOnly = false;
	
	public JSONMessageFactory(HashMap<String, Object> params)
	{
		if (params != null)
		{
			if (params.containsKey(FORCE_TEXT_ONLY))
			{
				useTextOnly = (Boolean)params.get(FORCE_TEXT_ONLY);
			}
		}
		
		//register
		DefaultSerializerManager.addSerializerMapper(new JSONMessageSerializer(), DefaultSerializerManager.MESSAGE_SERIALIZER_ID, null);
	}
	
	public Message createMessage()
	{
		return new JSONMessage(useTextOnly);
	}
}
