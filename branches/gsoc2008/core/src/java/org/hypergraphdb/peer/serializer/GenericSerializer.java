package org.hypergraphdb.peer.serializer;

import java.io.InputStream;
import java.io.OutputStream;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGPersistentHandle;
import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.peer.Subgraph;
import org.hypergraphdb.peer.SubgraphManager;

/**
 * @author ciprian.costa
 * Class that serializes objects based on their HGDB representation
 */
public class GenericSerializer implements HGSerializer
{
	private static HyperGraph tempDB;
	private SubgraphSerializer serializer = new SubgraphSerializer();
	
	
	public static HyperGraph getTempDB()
	{
		return tempDB;
	}

	public static void setTempDB(HyperGraph tempDB)
	{
		GenericSerializer.tempDB = tempDB;
	}

	public Object readData(InputStream in)
	{
		Subgraph result = null;
		
		int nSerializerID = SerializationUtils.deserializeInt(in);
		if (nSerializerID == DefaultSerializerManager.SUBGRAPH_SERIALIZER_ID)
		{
			result = (Subgraph) serializer.readData(in);
		}
		
		HGHandle handle = SubgraphManager.store(result, tempDB.getStore());

		return tempDB.get(handle);
	}

	public void writeData(OutputStream out, Object data)
	{
		SerializationUtils.serializeInt(out, DefaultSerializerManager.GENERIC_SERIALIZER_ID);

		HGPersistentHandle tempHandle = tempDB.getPersistentHandle(tempDB.add(data));
		
		Subgraph subGraph = new Subgraph(tempDB, tempHandle);
		serializer.writeData(out, subGraph);
		
		tempDB.remove(tempHandle);
	}
	

	
}
