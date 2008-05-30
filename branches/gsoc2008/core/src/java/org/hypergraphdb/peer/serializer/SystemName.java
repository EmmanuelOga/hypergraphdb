package org.hypergraphdb.peer.serializer;

import java.io.InputStream;
import java.io.OutputStream;


public class SystemName
{
	private String name;
	
	public SystemName()
	{
	}

	public SystemName(String name)
	{
		this.name = name;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final SystemName other = (SystemName) obj;
		if (name == null)
		{
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
	
	public static class SystemNameSerializer extends PooledObjectSerializer
	{
		public SystemNameSerializer(){}
		
		@Override
		protected Object createObject(InputStream in, ObjectPool objectPool)
		{
			return null;
		}

		@Override
		protected Object loadObjectData(InputStream in, Object result, ObjectPool objectPool)
		{
			return new SystemName(StringSerializer.deserializeString(in));
		}

		@Override
		protected void putData(OutputStream out, Object data, ObjectPool objectPool)
		{
			IntSerializer.serializeInt(out, SerializerManager.SYSTEM_NAME_SERIALIZER_ID);
			StringSerializer.serializeString(out, ((SystemName)data).getName());
		}
		
		
	}
}
