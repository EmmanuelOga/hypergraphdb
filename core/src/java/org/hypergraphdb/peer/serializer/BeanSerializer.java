package org.hypergraphdb.peer.serializer;

import java.beans.PropertyDescriptor;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.Map.Entry;

import org.hypergraphdb.peer.serializer.SystemName.SystemNameSerializer;

import org.hypergraphdb.type.BonesOfBeans;

public class BeanSerializer extends PooledObjectSerializer
{
	HGSerializer systemNameSerializer;
	
	public BeanSerializer(){
		systemNameSerializer = new SystemNameSerializer();
	}

	@Override
	public void putData(OutputStream out, Object data, ObjectPool objectPool)
	{
		//write serializer id
		IntSerializer.serializeInt(out, SerializerManager.BEAN_SERIALIZER_ID);
		
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
				
				HGSerializer itemSerializer = SerializerManager.getSerializer(propertyValue); 
				itemSerializer.writeData(out, propertyValue, objectPool);
			}
		}
	}
	
	@Override
	protected Object createObject(InputStream in, ObjectPool objectPool)
	{
		String typeName = ((SystemName)SerializerManager.getSerializer(in).readData(in, objectPool)).getName();
		return BonesOfBeans.makeBean(typeName);
	}
	
	protected Object loadObjectData(InputStream in, Object result, ObjectPool objectPool)
	{
		//read number of properties
		int n = IntSerializer.deserializeInt(in);
		//read properties
		for(int i=0;i<n;i++){
			//read property
			
			String propertyName = ((SystemName)SerializerManager.getSerializer(in).readData(in, objectPool)).getName();

			Integer serializerId = IntSerializer.deserializeInt(in);
			HGSerializer itemSerializer = SerializerManager.getSerializerById(serializerId); 

			Object propertyValue = itemSerializer.readData(in, objectPool);
			
			BonesOfBeans.setProperty(result, propertyName, propertyValue);
		}

		return null;
	}
}
