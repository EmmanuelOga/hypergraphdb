package hgtest.jxta;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.peer.HGTypeSystemPeer;
import org.hypergraphdb.peer.HyperGraphPeer;
import org.hypergraphdb.peer.PeerConfiguration;
import org.hypergraphdb.peer.jxta.JXTAPeerConfiguration;


public class HyperGraphDBClient{
	
	public static void main(String[] args){
		System.out.println("Starting a HGDB client ...");

		JXTAPeerConfiguration jxtaConf = new JXTAPeerConfiguration("");
		jxtaConf.addPeer("urn:jxta:uuid-59616261646162614E50472050325033C0C1DE89719B456691A596B983BA0E1004");
		
		PeerConfiguration conf = new PeerConfiguration(true, "", 
				false, null, null, 
				true, "org.hypergraphdb.peer.jxta.JXTAPeerForwarder", jxtaConf);
		
		HyperGraphPeer peer = new HyperGraphPeer(conf);
		
		peer.start();

		HGHandle handle = null;
		
		handle = peer.add("First atom to be sent");

		System.out.println("Client added handle: " + ((handle == null) ? "null" : handle.toString()));
		
		Object retrievedData = peer.get(handle);
	
		System.out.println("Client read: " + ((retrievedData == null) ? "null" : retrievedData.toString()));
		
		SimpleBean b = new SimpleBean();
		
		handle = ((HGTypeSystemPeer)peer.getTypeSystem()).getTypeHandle(SimpleBean.class);
		
		System.out.println("Handle for type simple: " + ((handle == null) ? "null" : handle.toString()));
		
	}

    
}
