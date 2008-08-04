package hgtest.jxta;

import java.io.File;

import net.jxta.platform.NetworkManager;

import org.hypergraphdb.HGPersistentHandle;
import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.handle.UUIDPersistentHandle;
import org.hypergraphdb.peer.DummyPolicy;
import org.hypergraphdb.peer.HyperGraphPeer;
import org.hypergraphdb.query.AnyAtomCondition;
import org.hypergraphdb.query.AtomPartCondition;
import org.hypergraphdb.query.ComparisonOperator;

public class HyperGraphDBServer {
	public static void main(String[] args){
		if (args.length != 3)
		{
			System.out.println("arguments: configFile startId userCount");
			System.exit(0);
		}
		
		String configFile = args[0];
		int startId = Integer.parseInt(args[1]);
		int count = Integer.parseInt(args[2]);
		
		
		System.out.println("Starting HGDB peer " + configFile + " ...");

		//HyperGraphPeer server = new HyperGraphPeer(new File("./server1Config"), new DummyPolicy(true));
		//HyperGraphPeer server = new HyperGraphPeer(new File("./config/minimalConfig"), new DummyPolicy(true));
		//HyperGraphPeer server = new HyperGraphPeer(new File("./config/namedPeerConfig"), new DummyPolicy(true));
		//HyperGraphPeer server = new HyperGraphPeer(new File("./config/dirPeerConfig"), new DummyPolicy(true));
		//HyperGraphPeer server = new HyperGraphPeer(new File("./config/transportPeerConfig"), new DummyPolicy(true));
		//HyperGraphPeer server = new HyperGraphPeer(new File("./config/relayPeerConfig"), new DummyPolicy(true));
		HyperGraphPeer server = new HyperGraphPeer(new File(configFile), new DummyPolicy(true));
		
		
		if (server.start("user", "pwd"))
		{		
			try
			{
				Thread.sleep(3000);
			} catch (InterruptedException e){}
	
			
			HGPersistentHandle typeHandle = UUIDPersistentHandle.makeHandle("e917bda6-0932-4a66-9aeb-3fc84f04ce57");
			server.registerType(typeHandle, User.class);
			System.out.println("Types registered...");
			
			//server.setAtomInterests(new AtomPartCondition(new String[] {"part"}, "5", ComparisonOperator.LT));
			server.updateNetworkProperties();
			server.setAtomInterests(new AnyAtomCondition());
			server.catchUp();
			
			HyperGraph graph =server.getHGDB();
			for(int i=0;i<count;i++)
			{
				User user = new User(startId + i, "user " + Integer.toString(startId + i));
				graph.add(user);
				System.out.println("object added");
			}
			
		}else{
			System.out.println("Can not start peer");
		}
	}
}
