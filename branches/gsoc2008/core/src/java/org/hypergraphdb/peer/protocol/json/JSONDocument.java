package org.hypergraphdb.peer.protocol.json;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map.Entry;

import org.hypergraphdb.peer.protocol.Document;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author ciprian.costa
 *
 */
/**
 * @author ciprian.costa
 *
 * Default JSON implementation for the <code>Document</code> interface. Also, for properties that can not be directly embedded
 * into JSON, it will store them as separate entities ... let the serialization mechanism decide how to handle them. 
 */
public abstract class JSONDocument implements Document
{
	static final String KEY_CLASS = null;

	protected JSONObject data;
	protected boolean useTextOnly;
	private HashMap<String, Object> nonJSONContent = new HashMap<String, Object>();

	public String toString()
	{
		String result = "";
		try
		{
			result = data.toString(4);
		} catch (JSONException e)
		{
			e.printStackTrace();
		}
		for(Entry<String, Object> binaryData : nonJSONContent.entrySet())
		{
			result += binaryData.getKey() + ": " + binaryData.getValue();
		}
		
		return result;
	}

	public Object get(String key)
	{
		if (nonJSONContent.containsKey(key))
		{
			return nonJSONContent.get(key);
		}else{
			Object value = data.opt(key);
			
			if (value instanceof JSONObject)
			{
				Object wrapper = JSONObjectMapper.getObject((JSONObject)value);
				if (wrapper != null)
				{
					value = wrapper;
				}
			}
			//for now just return the json object ... 
/*			if (value instanceof JSONObject)
			{
				//this might be "packaged by us as another object, need to return the package
				JSONObject jsonData = (JSONObject)value;
				String className = jsonData.optString(KEY_CLASS);
				if (className != null)
				{
					try
					{
						value = Class.forName(className).getConstructor(JSONObject.class).newInstance(jsonData);
					} catch (Exception e)
					{
						//if this fails just return the JSON aobject
					}
				}
			}
*/			return value;			
		}
	}

	private boolean isJSONObject(Object value)
	{
		return (value instanceof JSONObject || value instanceof String || value instanceof Boolean
				|| value instanceof Number);
	}

	public void put(String key, Object value)
	{
		if (isJSONObject(value))
		{
			try
			{
				data.put(key, value);
			} catch (JSONException e)
			{
				e.printStackTrace();
			}
		}else if (value instanceof JSONSerializable){
			try
			{
				data.put(key, ((JSONSerializable)value).getData());
			} catch (JSONException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}else if (JSONObjectMapper.accept(value)){
			try
			{
				data.put(key, JSONObjectMapper.getJSONObject(value));
			} catch (JSONException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}else{
			nonJSONContent.put(key, value);
		}
	}

	public JSONObject getData()
	{
		return data;
	}

	public boolean getBoolean(String key)
	{
		return data.optBoolean(key);
	}

	public double getDouble(String key)
	{
		return data.optDouble(key);
	}

	public int getInt(String key)
	{
		return data.optInt(key);
	}

	public long getLong(String key)
	{
		return data.optLong(key);
	}

	public String getString(String key)
	{
		return data.optString(key);
	}

	public void put(String key, boolean value)
	{
		try
		{
			data.put(key, value);
		} catch (JSONException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void put(String key, double value)
	{
		try
		{
			data.put(key, value);
		} catch (JSONException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	public void put(String key, int value)
	{
		try
		{
			data.put(key, value);
		} catch (JSONException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	public void put(String key, long value)
	{
		try
		{
			data.put(key, value);
		} catch (JSONException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void put(String key, String value)
	{
		try
		{
			data.put(key, value);
		} catch (JSONException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private JSONArray getArray(String key)
	{
		return data.optJSONArray(key);
	}

	public Object get(String key, int index)
	{
		JSONArray array = getArray(key);
		if (array != null)
		{
			return array.opt(index);
		}
		return null;
	}

	public boolean getBoolean(String key, int index)
	{
		JSONArray array = getArray(key);
		if (array != null)
		{
			return array.optBoolean(index);
		}
		return false;
	}

	public double getDouble(String key, int index)
	{
		JSONArray array = getArray(key);
		if (array != null)
		{
			return array.optDouble(index);
		}
		return 0;
	}

	public int getInt(String key, int index)
	{
		JSONArray array = getArray(key);
		if (array != null)
		{
			return array.optInt(index);
		}
		return 0;
	}

	public long getLong(String key, int index)
	{
		JSONArray array = getArray(key);
		if (array != null)
		{
			return array.optLong(index);
		}
		return 0L;
	}

	public String getString(String key, int index)
	{
		JSONArray array = getArray(key);
		if (array != null)
		{
			return array.optString(index);
		}
		return null;
	}

	public int length(String key)
	{
		JSONArray array = getArray(key);
		if (array != null)
		{
			return array.length();
		}
		return 0;
		
	}

	public void put(String key, Collection<?> col)
	{
		try
		{
			data.put(key, new JSONArray(col));
		} catch (JSONException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void remove(String key)
	{
		data.remove(key);
		
	}

	public boolean useTextOnly()
	{
		return useTextOnly;
	}

	public HashMap<String, Object> getNonJSONContent()
	{
		return nonJSONContent;
	}

	public void setNonJSONContent(HashMap<String, Object> nonJSONContent)
	{
		this.nonJSONContent = nonJSONContent;
	}
	
}
