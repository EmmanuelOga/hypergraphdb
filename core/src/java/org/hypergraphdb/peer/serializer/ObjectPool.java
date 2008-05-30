package org.hypergraphdb.peer.serializer;

import java.util.HashMap;

public class ObjectPool
{
	// TODO objects should be grouped by type to avoid unnecesary compares
	private int objectId;
	private HashMap<Object, Integer> objects;
	private HashMap<Integer, Object> invertedObjects;
	
	public ObjectPool()
	{
		objectId = 0;
		objects = new HashMap<Object, Integer>();
		invertedObjects = new HashMap<Integer, Object>();
	}
	
	public Object getObject(Integer objectId)
	{
		return invertedObjects.get(objectId);
	}

	public int getObjectId(Object data)
	{
		if (data == null) return -1;
		else{
			Integer id = objects.get(data);
			
			if (id == null) return -1;
			else return id.intValue();
		}
	}

	public void addObject(Object data)
	{
		setObject(objectId, data);
		objectId++;
		
	}

	public Integer getId()
	{
		return objectId++;
	}

	public void setObject(Integer objectId, Object data)
	{
		invertedObjects.put(objectId, data);
		objects.put(data, objectId);
		
	}

}
