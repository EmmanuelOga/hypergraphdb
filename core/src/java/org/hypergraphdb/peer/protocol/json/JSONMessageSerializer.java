package org.hypergraphdb.peer.protocol.json;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map.Entry;

import org.hypergraphdb.peer.protocol.Message;
import org.hypergraphdb.peer.serializer.DefaultSerializerManager;
import org.hypergraphdb.peer.serializer.HGSerializer;
import org.hypergraphdb.peer.serializer.SerializationUtils;
import org.hypergraphdb.peer.serializer.SerializerMapper;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author ciprian.costa
 * Class used by the serialization mechanism to serialize JSON messages. It contains the logic of how to handle 
 * binary data (for now just append it after the JSON message).
 */
public class JSONMessageSerializer implements SerializerMapper, HGSerializer
{
	public HGSerializer accept(Class<?> clazz)
	{
		if (Message.class.isAssignableFrom(clazz)) return this;
		else return null;
	}

	public HGSerializer getSerializer()
	{
		return this;
	}

	public Object readData(InputStream in)
	{
		JSONMessage result = null;

		boolean useTextOnly = false;
		try
		{
			useTextOnly = (in.read() == 1);
		} catch (IOException e1)
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} 

		String jsonString = SerializationUtils.deserializeString(in);
		try
		{
			JSONObject jsonData = new JSONObject(jsonString);
			result = new JSONMessage(jsonData, useTextOnly);

			int size = SerializationUtils.deserializeInt(in);
			for(int i=0;i<size;i++)
			{
				String key = SerializationUtils.deserializeString(in);
				Object value = SerializationUtils.deserializeObject(in);
				
				result.put(key, value);
			}
		} catch (JSONException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		return result;
	}

	public void writeData(OutputStream out, Object data)
	{
		SerializationUtils.serializeInt(out, DefaultSerializerManager.MESSAGE_SERIALIZER_ID);

		JSONMessage msg = (JSONMessage)data;
		JSONObject jsonData = ((JSONSerializable)data).getData();
		
		try
		{
			out.write(msg.useTextOnly() ? 1 : 0);
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		SerializationUtils.serializeString(out, jsonData.toString());
		
		HashMap<String, Object> nonJSONContent = msg.getNonJSONContent();
		SerializationUtils.serializeInt(out, nonJSONContent.size());
		for(Entry<String, Object> entry : nonJSONContent.entrySet())
		{
			SerializationUtils.serializeString(out, entry.getKey());
			SerializationUtils.serializeObject(out, entry.getValue());
		}
	}

}
