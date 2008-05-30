package org.hypergraphdb.peer.serializer;

import java.io.InputStream;
import java.io.OutputStream;


public class StreamObjectReference
{
	private Integer objectID;
	
	public StreamObjectReference(Integer objectID){
		this.objectID = objectID;
	}

	public Integer getObjectID() {
		return objectID;
	}

	public void setObjectID(Integer objectID) {
		this.objectID = objectID;
	}

	public static class StreamObjectReferenceSerializer implements HGSerializer{
		@Override
		public Object readData(InputStream in, ObjectPool objectPool)
		{
			Integer objectId = IntSerializer.deserializeInt(in);
			return objectPool.getObject(objectId);
		}

		@Override
		public void writeData(OutputStream out, Object data, ObjectPool objectPool)
		{
			IntSerializer.serializeInt(out, SerializerManager.STREAM_OBJECT_REFERENCE_SERIALIZER_ID);
			IntSerializer.serializeInt(out, ((StreamObjectReference)data).getObjectID());
		}
		
	}

}
