package org.hypergraphdb.peer.serializer;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import org.hypergraphdb.peer.protocol.SerializerManager;
import org.hypergraphdb.peer.protocol.SerializerMapper;

public class JavaSerializer extends PooledObjectSerializer
{
	public JavaSerializer(SerializerManager serializerManager)
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
		Object javaObj = null;
		
		try
		{
			ObjectInputStream oin = new ObjectInputStream(in);
			javaObj = oin.readObject();
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return javaObj;
	}

	@Override
	protected void putData(OutputStream out, Object data, ObjectPool objectPool)
	{
		//write serializer id
		IntSerializer.serializeInt(out, DefaultSerializerManager.JAVA_SERIALIZER_ID);

		ObjectOutputStream oout;
		try
		{
			oout = new ObjectOutputStream(out);
			oout.writeObject(data);
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static class JavaSerializerMapper implements SerializerMapper
	{
		private HGSerializer serializer;
		
		public JavaSerializerMapper(SerializerManager manager)
		{
			serializer = new JavaSerializer(manager);
		}
		
		@Override
		public HGSerializer accept(Class<?> clazz)
		{
			//accept anything
			return serializer;
		}

		@Override
		public HGSerializer getSerializer()
		{
			return serializer;
		}
		
	}
	
}
