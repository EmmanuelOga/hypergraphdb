package hgtest.jxta;

import org.hypergraphdb.peer.DummyPolicy;
import org.hypergraphdb.peer.HyperGraphPeer;
import org.hypergraphdb.peer.PeerConfiguration;
import org.hypergraphdb.peer.jxta.JXTAPeerConfiguration;

public class HyperGraphDBServer {
	public static void main(String[] args){
		if (args.length != 2)
		{
			System.out.println("arguments: PeerName PeerGroup");
			System.exit(0);
		}
		
		String peerName = args[0];
		String groupName = args[1];
		
		System.out.println("Starting HGDB peer " + peerName + " ...");

		JXTAPeerConfiguration jxtaConf = new JXTAPeerConfiguration();
		jxtaConf.setPeerName(peerName);
		jxtaConf.setPeerGroupName(groupName);
		
		PeerConfiguration conf = new PeerConfiguration(true, "./DBs/" + peerName + "DB", 
				true, "org.hypergraphdb.peer.jxta.JXTAServerInterface", jxtaConf, 
				false, null, null,
				"./DBs/" + peerName + "CacheDb");
		
		HyperGraphPeer server = new HyperGraphPeer(conf, new DummyPolicy(true));
		
		server.start();
	}
}
