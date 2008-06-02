package org.hypergraphdb.peer.serializer;

import java.io.InputStream;
import java.io.OutputStream;

import org.hypergraphdb.peer.protocol.SerializerManager;
import org.hypergraphdb.peer.protocol.SerializerMapper;


public class ArraySerializer extends PooledObjectSerializer
{
	public ArraySerializer(SerializerManager serializerManager)
	{
		super(serializerManager);
	}

	@Override
	protected Object createObject(InputStream in, ObjectPool objectPool)
	{
		Integer length = IntSerializer.deserializeInt(in);
		return (Object)(new Object[length]);
	}

	@Override
	protected Object loadObjectData(InputStream in, Object result, ObjectPool objectPool)
	{
		Object[] array = (Object[])result;
		
		for(int i=0;i<array.length;i++)
		{
			array[i] = getSerializerManager().getSerializer(in).readData(in, objectPool);
		}
		
		return null;
	}

	@Override
	protected void putData(OutputStream out, Object data, ObjectPool objectPool)
	{
		Object[] array = (Object[])data;
		
		//write serializer id
		IntSerializer.serializeInt(out, DefaultSerializerManager.ARRAY_SERIALIZER_ID);
		
		//write length 
		IntSerializer.serializeInt(out, array.length);
		
		for(int i=0;i<array.length;i++)
		{
			HGSerializer itemSerializer = getSerializerManager().getSerializer(array[i]); 
			itemSerializer.writeData(out, array[i], objectPool);
			
		}
	}
	
	public static class ArraySerializerMapper implements SerializerMapper
	{
		private HGSerializer serializer;

		public ArraySerializerMapper(SerializerManager manager)
		{
			serializer = new ArraySerializer(manager);
		}
		
		@Override
		public HGSerializer accept(Class<?> clazz)
		{
			if (clazz.isArray()) return serializer;
			else return null;
		}

		@Override
		public HGSerializer getSerializer()
		{
			return serializer;
		}
		
	}
}
