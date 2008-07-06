package hgtest.jxta;

import java.util.HashMap;

import org.hypergraphdb.peer.DummyPolicy;
import org.hypergraphdb.peer.HyperGraphPeer;
import org.hypergraphdb.peer.PeerConfiguration;
import org.hypergraphdb.peer.jxta.JXTAPeerConfiguration;
import org.hypergraphdb.query.AnyAtomCondition;

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
		jxtaConf.setAdvTimeToLive(1*5*1000);

		PeerConfiguration conf = new PeerConfiguration(true, "./DBs/" + peerName + "DB", 
				true, false, "org.hypergraphdb.peer.jxta.JXTAPeerInterface", jxtaConf, 
				"./DBs/" + peerName + "CacheDb");
		
		HyperGraphPeer server = new HyperGraphPeer(conf, new DummyPolicy(true));
		
		server.start();
		
		try
		{
			Thread.sleep(3000);
		} catch (InterruptedException e){}

		server.setAtomInterests(new AnyAtomCondition());
		server.catchUp();
	}
}
