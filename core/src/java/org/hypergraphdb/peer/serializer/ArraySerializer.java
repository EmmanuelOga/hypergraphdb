package org.hypergraphdb.peer.serializer;

import java.io.InputStream;
import java.io.OutputStream;


public class ArraySerializer extends PooledObjectSerializer
{

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
			array[i] = SerializerManager.getSerializer(in).readData(in, objectPool);
		}
		
		return null;
	}

	@Override
	protected void putData(OutputStream out, Object data, ObjectPool objectPool)
	{
		Object[] array = (Object[])data;
		
		//write serializer id
		IntSerializer.serializeInt(out, SerializerManager.ARRAY_SERIALIZER_ID);
		
		//write length 
		IntSerializer.serializeInt(out, array.length);
		
		for(int i=0;i<array.length;i++)
		{
			HGSerializer itemSerializer = SerializerManager.getSerializer(array[i]); 
			itemSerializer.writeData(out, array[i], objectPool);
			
		}
	}
}
