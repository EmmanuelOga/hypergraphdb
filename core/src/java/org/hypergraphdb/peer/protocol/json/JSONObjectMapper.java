package org.hypergraphdb.peer.protocol.json;

import java.net.URI;
import java.net.URISyntaxException;

import net.jxta.document.AdvertisementFactory;
import net.jxta.id.IDFactory;
import net.jxta.protocol.PipeAdvertisement;

import org.hypergraphdb.query.AnyAtomCondition;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author ciprian.costa
 * One way to embed objects into the Message. This is used by classes that would rather use 
 * proxies instead of the <code>JSONSerializable</code> interface.
 */
public class JSONObjectMapper
{
	private final static String PIPE_CLASS = "pipe";
	
	public static boolean accept(Object value)
	{
		return (value instanceof PipeAdvertisement) || (value instanceof AnyAtomCondition);
	}

	public static JSONObject getJSONObject(Object value)
	{
		if (value instanceof PipeAdvertisement)
			return getFromPipeAdvertisement((PipeAdvertisement)value);
		else if (value instanceof AnyAtomCondition)
			return getFromAnyAtomCondition((AnyAtomCondition)value);
		return null;
	}

	public static JSONObject getFromAnyAtomCondition(AnyAtomCondition value)
	{
		JSONObject json = new JSONObject();
		
		try
		{
			json.put("class", "query:any_atom");
		} catch (JSONException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return json;
	}

	public static Object getObject(JSONObject value)
	{
		String className = value.optString("class");
		
		if (className != null)
		{
			if (className.equals(PIPE_CLASS))
				return getPipeAdvertisement(value);
			else if (className.equals("query:any_atom"))
				return getAnyAtomCondition(value);
		}
		
		return null;
	}
	
	public static AnyAtomCondition getAnyAtomCondition(JSONObject value)
	{
		// TODO Auto-generated method stub
		return new AnyAtomCondition();
	}

	public static Object getPipeAdvertisement(JSONObject value)
	{
		PipeAdvertisement adv = (PipeAdvertisement)AdvertisementFactory.newAdvertisement(PipeAdvertisement.getAdvertisementType());

		adv.setType(value.optString("type"));
		adv.setName(value.optString("name"));
		try
		{
			adv.setPipeID(IDFactory.fromURI(URI.create(value.optString("id"))));
		} catch (URISyntaxException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return adv;
	}

	public static JSONObject getFromPipeAdvertisement(PipeAdvertisement value)
	{
		JSONObject json = new JSONObject();
		
		try
		{
			json.put("class", PIPE_CLASS);
			json.put("type", value.getType());
			json.put("id", value.getPipeID().toString());
			json.put("name", value.getName());
		} catch (JSONException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return json;
	}

	
}
