package org.hypergraphdb.peer.serializer;

import java.io.InputStream;
import java.io.OutputStream;


public class NullSerializer implements HGSerializer {
	
	public Object readData(InputStream in, ObjectPool objectPool)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public void writeData(OutputStream out, Object data, ObjectPool objectPool)
	{
		IntSerializer.serializeInt(out, DefaultSerializerManager.NULL_SERIALIZER_ID);
	}

}
