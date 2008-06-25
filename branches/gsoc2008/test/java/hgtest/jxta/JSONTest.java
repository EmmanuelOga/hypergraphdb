package hgtest.jxta;

import java.util.HashMap;
import java.util.UUID;

import org.hypergraphdb.peer.protocol.Message;
import org.hypergraphdb.peer.protocol.json.JSONMessageFactory;
import org.json.JSONException;
import org.json.JSONObject;

public class JSONTest
{
	public static void main(String[] args)
	{
		JSONObject json1 = new JSONObject();
		JSONObject json2 = new JSONObject();
		
		try
		{
			json1.put("prop1", "test");
			json2.put("prop1", "test1");
			json1.put("otherJSON", json2);
			System.out.println(json1);
			
			json2.put("prop2", "test2");
			System.out.println(json1);
		} catch (JSONException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("ForceTextOnly", true);
		JSONMessageFactory factory = new JSONMessageFactory(params);
		Message msg = factory.createMessage();
		
		msg.setAction("test");
		msg.setConversationId(UUID.randomUUID());
		
		System.out.println(msg);
	}
}
