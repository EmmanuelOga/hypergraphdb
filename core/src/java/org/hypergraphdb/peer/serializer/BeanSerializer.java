package org.hypergraphdb.peer.serializer;

import java.beans.PropertyDescriptor;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.Map.Entry;

import org.hypergraphdb.peer.protocol.SerializerManager;
import org.hypergraphdb.peer.protocol.SerializerMapper;
import org.hypergraphdb.peer.serializer.SystemName.SystemNameSerializer;

import org.hypergraphdb.type.BonesOfBeans;
import org.hypergraphdb.type.JavaTypeFactory;

public class BeanSerializer extends PooledObjectSerializer
{
	HGSerializer systemNameSerializer;
	
	public BeanSerializer(SerializerManager manager){
		super(manager);
		
		systemNameSerializer = new SystemNameSerializer(manager);
	}

	@Override
	public void putData(OutputStream out, Object data, ObjectPool objectPool)
	{
		//write serializer id
		IntSerializer.serializeInt(out, DefaultSerializerManager.BEAN_SERIALIZER_ID);
		
		Map<String, PropertyDescriptor> properties = BonesOfBeans.getAllPropertyDescriptors(data.getClass());
		
		//write type if already there it will be transformed in a stream reference 
		systemNameSerializer.writeData(out, new SystemName(data.getClass().getName()), objectPool);
		
		if (properties != null){
			//write number of properties
			IntSerializer.serializeInt(out, properties.size());
			
			for(Entry<String, PropertyDescriptor> entry : properties.entrySet()){
				//write property name - if already there will be transformed in a stream reference
				systemNameSerializer.writeData(out, new SystemName(entry.getKey()), objectPool);

				//write data
				Object propertyValue = BonesOfBeans.getProperty(data, entry.getValue());
				
				HGSerializer itemSerializer = getSerializerManager().getSerializer(propertyValue); 
				itemSerializer.writeData(out, propertyValue, objectPool);
			}
		}
	}
	
	@Override
	protected Object createObject(InputStream in, ObjectPool objectPool)
	{
		String typeName = ((SystemName)getSerializerManager().getSerializer(in).readData(in, objectPool)).getName();
		return BonesOfBeans.makeBean(typeName);
	}
	
	protected Object loadObjectData(InputStream in, Object result, ObjectPool objectPool)
	{
		//read number of properties
		int n = IntSerializer.deserializeInt(in);
		//read properties
		for(int i=0;i<n;i++){
			//read property
			
			String propertyName = ((SystemName)getSerializerManager().getSerializer(in).readData(in, objectPool)).getName();

			Integer serializerId = IntSerializer.deserializeInt(in);
			HGSerializer itemSerializer = DefaultSerializerManager.getSerializerById(serializerId); 

			Object propertyValue = itemSerializer.readData(in, objectPool);
			
			BonesOfBeans.setProperty(result, propertyName, propertyValue);
		}

		return null;
	}
	
	public static class BeanSerializerMapper implements SerializerMapper
	{
		public HGSerializer serializer;
		
		public BeanSerializerMapper(SerializerManager manager)
		{
			serializer = new BeanSerializer(manager);
		}
		
		public HGSerializer accept(Class<?> clazz)
		{
			Map<String, PropertyDescriptor> descriptors = BonesOfBeans.getAllPropertyDescriptors(clazz);
			
			// copied from DefaultJavaTypeMapper ... not sure that we should reuse this code or they might eveolve independently
			boolean isRecord = JavaTypeFactory.isDefaultConstructible(clazz);

			if (isRecord)
			{
				//
				// Determine whether the Java class has a "record" aspect to it: that is,
				// whether there is at least one property that is both readable and  writeable.
				//
				isRecord = false;
				for (PropertyDescriptor d : descriptors.values()) {
					if (d.getReadMethod() != null && d.getWriteMethod() != null) {
						isRecord = true;
						break;
					}
				}
			}
			
			if (isRecord) return serializer;
			else return null;
		}

		@Override
		public HGSerializer getSerializer()
		{
			return serializer;
		}
	}
}
