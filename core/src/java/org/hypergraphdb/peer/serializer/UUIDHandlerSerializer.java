package org.hypergraphdb.peer.serializer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.hypergraphdb.handle.UUIDPersistentHandle;
import org.hypergraphdb.peer.protocol.SerializerManager;

public class UUIDHandlerSerializer extends PooledObjectSerializer
{
	public UUIDHandlerSerializer(SerializerManager serializerManager)
	{
		super(serializerManager);
	}

	@Override
	protected Object createObject(InputStream in, ObjectPool objectPool)
	{
		return null;
	}

	@Override
	protected Object loadObjectData(InputStream in, Object result, ObjectPool objectPool)
	{
		byte[] data = new byte[UUIDPersistentHandle.SIZE];
		
		try
		{
			in.read(data);
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return UUIDPersistentHandle.makeHandle(data);
	}

	@Override
	protected void putData(OutputStream out, Object data, ObjectPool objectPool)
	{
		IntSerializer.serializeInt(out, DefaultSerializerManager.UUID_HANDLE_SERIALIZER_ID);
		try
		{
			out.write(((UUIDPersistentHandle)data).toByteArray());
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
	}

}
