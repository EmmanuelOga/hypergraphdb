package org.hypergraphdb.peer.serializer;

import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.ListIterator;

import org.hypergraphdb.handle.UUIDPersistentHandle;
import org.hypergraphdb.peer.protocol.SerializerManager;
import org.hypergraphdb.peer.protocol.SerializerMapper;
import org.hypergraphdb.peer.serializer.ArraySerializer.ArraySerializerMapper;
import org.hypergraphdb.peer.serializer.BeanSerializer.BeanSerializerMapper;
import org.hypergraphdb.peer.serializer.JavaSerializer.JavaSerializerMapper;
import org.hypergraphdb.peer.serializer.StreamObjectReference.StreamObjectReferenceSerializer;
import org.hypergraphdb.peer.serializer.SystemName.SystemNameSerializer;
import org.hypergraphdb.util.Pair;

public class DefaultSerializerManager implements SerializerManager
{
	public static final Integer INT_SERIALIZER_ID = 0;
	public static final Integer STRING_SERIALIZER_ID = 1;
	
	public static final Integer STREAM_OBJECT_REFERENCE_SERIALIZER_ID = 100;
	public static final Integer SYSTEM_NAME_SERIALIZER_ID = 101;
	
	public static final Integer NULL_SERIALIZER_ID = 200;
	
	public static final Integer UUID_HANDLE_SERIALIZER_ID = 300;

	public static final Integer BEAN_SERIALIZER_ID = 400;
	public static final Integer ARRAY_SERIALIZER_ID = 401;
	public static final Integer JAVA_SERIALIZER_ID = 402;

	private static HashMap<String, HGSerializer> wellKnownSerializers = new HashMap<String, HGSerializer>();
	private static HashMap<String, Integer> wellKnownSerializerIds = new HashMap<String, Integer>();
	private static HashMap<Integer, HGSerializer> invertedWellKnownSerializerIds = new HashMap<Integer, HGSerializer>();
		
	private static LinkedList<Pair<SerializerMapper, Integer>> serializerMappers = new LinkedList<Pair<SerializerMapper,Integer>>();
	private static HashMap<Integer, SerializerMapper> invertedSerializerMappers = new HashMap<Integer, SerializerMapper>(); 
	
	
	public DefaultSerializerManager() 
	{
		//bootstrap for now ... 
		addWellknownSerializer(String.class.getName(), new StringSerializer(), STRING_SERIALIZER_ID);
		addWellknownSerializer(Integer.class.getName(), new IntSerializer(), INT_SERIALIZER_ID);
		
		addWellknownSerializer(StreamObjectReference.class.getName(), new StreamObjectReferenceSerializer(), STREAM_OBJECT_REFERENCE_SERIALIZER_ID);
		addWellknownSerializer(SystemName.class.getName(), new SystemNameSerializer(this), SYSTEM_NAME_SERIALIZER_ID);
		
		addWellknownSerializer("nullSerializer", new NullSerializer(), NULL_SERIALIZER_ID);
		
		addSerializerMapper(new JavaSerializerMapper(this), JAVA_SERIALIZER_ID, null);
		addSerializerMapper(new BeanSerializerMapper(this), BEAN_SERIALIZER_ID, null);
		addSerializerMapper(new ArraySerializerMapper(this), ARRAY_SERIALIZER_ID, null);
		
		addWellknownSerializer(UUIDPersistentHandle.class.getName(), new UUIDHandlerSerializer(this), UUID_HANDLE_SERIALIZER_ID);
	}
	
	public static void addWellknownSerializer(String name, HGSerializer serializer, Integer id)
	{
		wellKnownSerializers.put(name, serializer);
		wellKnownSerializerIds.put(name, id);
		invertedWellKnownSerializerIds.put(id, serializer);
	}

	public static void addSerializerMapper(SerializerMapper mapper, Integer id, SerializerMapper addAfter)
	{
		if (addAfter == null)
		{
			serializerMappers.addFirst(new Pair<SerializerMapper, Integer>(mapper, id));
		}else{
			int index = serializerMappers.indexOf(addAfter);
			if (index >= 0)
			{
				serializerMappers.add(index + 1, new Pair<SerializerMapper, Integer>(mapper, id));
			}else{
				serializerMappers.add(new Pair<SerializerMapper, Integer>(mapper, id));
			}
		}
		
		invertedSerializerMappers.put(id, mapper);
	}
	
	//interface functions
	public HGSerializer getSerializer(InputStream in)
	{
		Integer serializerId = IntSerializer.deserializeInt(in);
		return getSerializerById(serializerId); 

	}
	public HGSerializer getSerializer(Object data){
		if (data == null) return wellKnownSerializers.get("nullSerializer");
		else return getSerializerByType(data.getClass());
	}
	
	public HGSerializer getSerializerByType(Class<?> clazz){
		HGSerializer serializer = null;
		
		//first try with well known serializers
		serializer = getSerializerByTypeName(clazz.getName());
		if (serializer == null)
		{
			//try existing mappers
			ListIterator<Pair<SerializerMapper, Integer>> iterator = serializerMappers.listIterator();
			while ((serializer == null) && (iterator.hasNext()))
			{
				serializer = iterator.next().getFirst().accept(clazz);
			}
		}

		//should be worry about this being null?
		return serializer;
	}
	

	private HGSerializer getSerializerByTypeName(String typeName){
		HGSerializer serializer = null;

		serializer = wellKnownSerializers.get(typeName);
		
		if (serializer == null){
			if (serializer == null){
				// TODO check if it is actually a bean
				serializer = wellKnownSerializers.get("beanSerializer");
			}
		}
		
		return serializer;
	}

	public static HGSerializer getSerializerById(Integer serializerId)
	{
		//first try well known
		HGSerializer serializer = invertedWellKnownSerializerIds.get(serializerId);
		
		if (serializer == null)
		{
			SerializerMapper mapper = invertedSerializerMappers.get(serializerId);
			
			if (mapper != null)
			{
				serializer = mapper.getSerializer();
			}
		}
		
		//should be worry about this being null?
		return serializer;
	}
}
