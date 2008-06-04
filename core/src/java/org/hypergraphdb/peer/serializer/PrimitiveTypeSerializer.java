package org.hypergraphdb.peer.serializer;

import java.io.InputStream;
import java.io.OutputStream;



public abstract class PrimitiveTypeSerializer implements HGSerializer 
{
	
	public void writeData(OutputStream out, Object data, ObjectPool objectPool)
	{
		serialize(out, data);
	}
	

	public Object readData(InputStream in, ObjectPool objectPool)
	{
		return deserialize(in);
	}

	protected abstract Object deserialize(InputStream in);
	protected abstract void serialize(OutputStream out, Object data);

}
