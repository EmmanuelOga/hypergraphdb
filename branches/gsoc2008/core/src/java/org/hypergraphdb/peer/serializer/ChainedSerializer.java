package org.hypergraphdb.peer.serializer;

import org.hypergraphdb.peer.protocol.SerializerManager;

public abstract class ChainedSerializer implements HGSerializer
{
	private SerializerManager serializerManager;
	
	public ChainedSerializer(SerializerManager serializerManager)
	{
		this.serializerManager = serializerManager;
	}

	public SerializerManager getSerializerManager()
	{
		return serializerManager;
	}

	public void setSerializerManager(SerializerManager serializerManager)
	{
		this.serializerManager = serializerManager;
	}
	
	
}
