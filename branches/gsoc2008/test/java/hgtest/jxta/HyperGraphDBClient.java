package hgtest.jxta;

import java.util.HashMap;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.peer.DummyPolicy;
import org.hypergraphdb.peer.HyperGraphPeer;
import org.hypergraphdb.peer.PeerConfiguration;
import org.hypergraphdb.peer.jxta.JXTAPeerConfiguration;


public class HyperGraphDBClient{
	
	public static void main(String[] args){
		if (args.length != 2)
		{
			System.out.println("arguments: PeerName PeerGroup");
			System.exit(0);
		}

		String peerName = args[0];
		String groupName = args[1];

		System.out.println("Starting a HGDB client ...");

		JXTAPeerConfiguration jxtaConf = new JXTAPeerConfiguration("");
		jxtaConf.setPeerName(peerName);
		jxtaConf.setPeerGroupName(groupName);
		jxtaConf.setMessageFactory("org.hypergraphdb.peer.protocol.json.JSONMessageFactory");

		HashMap<String, Object> messageConfig = new HashMap<String, Object>();
		messageConfig.put("ForceTextOnly", false);
		jxtaConf.setMessageFactoryParams(messageConfig);
		
		PeerConfiguration conf = new PeerConfiguration(true, "", 
				false, true, "org.hypergraphdb.peer.jxta.JXTAPeerInterface", jxtaConf,
				"./DBs/" + peerName + "CacheDB");
		
		HyperGraphPeer peer = new HyperGraphPeer(conf, new DummyPolicy(false));
		
		peer.start();

		try
		{
			Thread.sleep(3000);
		} catch (InterruptedException e){}
		
		peer.updateNetworkProperties();
		
		System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
		
		HGHandle handle = null;
		Object retrievedData = null;
		
		HGHandle handle1 = peer.add("First atom to be sent");
		System.out.println("Client added handle: " + handle1);

		HGHandle handle2 = peer.add("Second atom to be sent");
		System.out.println("Client added handle: " + handle2);

//		retrievedData = null;
//		if (handle1 != null) retrievedData = peer.get(handle1);
//		System.out.println("Client read: " + ((retrievedData == null) ? "null" : retrievedData.toString()));
		
		//retrievedData = null;
		//if (handle2 != null) retrievedData = peer.get(handle2);
		//System.out.println("Client read: " + ((retrievedData == null) ? "null" : retrievedData.toString()));

		/*
		SimpleBean b = new SimpleBean("test");
		
		handle = peer.add(b);

		System.out.println("Client added handle: " + ((handle == null) ? "null" : handle.toString()));

		retrievedData = peer.get(handle);
		
		System.out.println("Client read: " + ((retrievedData == null) ? "null" : retrievedData.toString()));
*/
		/*
		handle = ((HGTypeSystemPeer)peer.getTypeSystem()).getTypeHandle(SimpleBean.class);
		
		System.out.println("Handle for type simple: " + ((handle == null) ? "null" : handle.toString()));
		*/		
	}
}
