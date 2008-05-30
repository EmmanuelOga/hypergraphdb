package org.hypergraphdb.peer.serializer;

import java.io.InputStream;
import java.util.HashMap;

import org.hypergraphdb.handle.UUIDPersistentHandle;
import org.hypergraphdb.peer.serializer.StreamObjectReference.StreamObjectReferenceSerializer;
import org.hypergraphdb.peer.serializer.SystemName.SystemNameSerializer;

public class SerializerManager
{
	public static final Integer NULL_SERIALIZER_ID = 200;
	public static final Integer BEAN_SERIALIZER_ID = 201;
	public static final Integer ARRAY_SERIALIZER_ID = 202;
	
	public static final Integer INT_SERIALIZER_ID = 0;
	public static final Integer STRING_SERIALIZER_ID = 1;
	
	public static final Integer STREAM_OBJECT_REFERENCE_SERIALIZER_ID = 100;
	public static final Integer SYSTEM_NAME_SERIALIZER_ID = 101;

	public static final Integer UUID_HANDLE_SERIALIZER_ID = 300;
	
	private static HashMap<String, HGSerializer> wellKnownSerializers = new HashMap<String, HGSerializer>();
	private static HashMap<String, Integer> wellKnownSerializerIds = new HashMap<String, Integer>();
	private static HashMap<Integer, HGSerializer> invertedWellKnownSerializerIds = new HashMap<Integer, HGSerializer>();
		
	static {
		// TODO wellKnown types are spread in two places -> refactor
		addWellknownSerializer(String.class.getName(), new StringSerializer(), STRING_SERIALIZER_ID);
		addWellknownSerializer(Integer.class.getName(), new IntSerializer(), INT_SERIALIZER_ID);
		
		addWellknownSerializer(StreamObjectReference.class.getName(), new StreamObjectReferenceSerializer(), STREAM_OBJECT_REFERENCE_SERIALIZER_ID);
		addWellknownSerializer(SystemName.class.getName(), new SystemNameSerializer(), SYSTEM_NAME_SERIALIZER_ID);
		
		addWellknownSerializer("nullSerializer", new NullSerializer(), NULL_SERIALIZER_ID);
		addWellknownSerializer("beanSerializer", new BeanSerializer(), BEAN_SERIALIZER_ID);
		addWellknownSerializer("arraySerializer", new ArraySerializer(), ARRAY_SERIALIZER_ID);
		
		addWellknownSerializer(UUIDPersistentHandle.class.getName(), new UUIDHandlerSerializer(), UUID_HANDLE_SERIALIZER_ID);
		
	}
	
	public static void addWellknownSerializer(String name, HGSerializer serializer, Integer id)
	{
		wellKnownSerializers.put(name, serializer);
		wellKnownSerializerIds.put(name, id);
		invertedWellKnownSerializerIds.put(id, serializer);
	}

	public static HGSerializer getSerializer(InputStream in)
	{
		Integer serializerId = IntSerializer.deserializeInt(in);
		return getSerializerById(serializerId); 

	}
	public static HGSerializer getSerializer(Object data){
		// TODO use special serializer for null?
		if (data == null) return wellKnownSerializers.get("nullSerializer");
		else return getSerializerByType(data.getClass());
	}
	
	public static HGSerializer getSerializerByType(Class<?> clazz){
		if (clazz.isArray()) return wellKnownSerializers.get("arraySerializer");
		else return getSerializerByTypeName(clazz.getName());
	}

	public static HGSerializer getSerializerByTypeName(String typeName){
		HGSerializer serializer = null;

		serializer = wellKnownSerializers.get(typeName);
		
		if (serializer == null){
			if (serializer == null){
				serializer = wellKnownSerializers.get("beanSerializer");
			}
		}
		
		return serializer;
	}

	public static HGSerializer getSerializerById(Integer serializerId)
	{
		return invertedWellKnownSerializerIds.get(serializerId);
	}
}
