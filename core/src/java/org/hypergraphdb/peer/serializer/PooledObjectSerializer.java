package org.hypergraphdb.peer.serializer;

import java.io.InputStream;
import java.io.OutputStream;

import org.hypergraphdb.peer.protocol.SerializerManager;


public abstract class PooledObjectSerializer extends ChainedSerializer
{
	private HGSerializer referenceSerializer = null;
	
	protected abstract void putData(OutputStream out, Object data, ObjectPool objectPool);
	protected abstract Object createObject(InputStream in, ObjectPool objectPool);
	protected abstract Object loadObjectData(InputStream in, Object result, ObjectPool objectPool);

	public PooledObjectSerializer(SerializerManager serializerManager)
	{
		super(serializerManager);
		
		referenceSerializer = getSerializerManager().getSerializerByType(StreamObjectReference.class);
	}

	@Override
	public Object readData(InputStream in, ObjectPool objectPool)
	{
		Integer objectId = objectPool.getId();
		
		Object result = createObject(in, objectPool);
		
		if (result != null)
		{
			objectPool.setObject(objectId, result);
		}
		
		Object loadResult = loadObjectData(in, result, objectPool);

		if (loadResult != null)
		{
			objectPool.setObject(objectId, loadResult);
			result = loadResult;
		}
		
		return result;
	}



	@Override
	public void writeData(OutputStream out, Object data, ObjectPool objectPool)
	{
		int objectId = objectPool.getObjectId(data);
		if (objectId >= 0)
		{
			referenceSerializer.writeData(out, new StreamObjectReference(objectId), objectPool);
		}else{
			objectPool.addObject(data);

			putData(out, data, objectPool);
		}

		
	}

}
