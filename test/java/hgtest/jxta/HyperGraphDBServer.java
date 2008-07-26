package hgtest.jxta;

import java.io.File;

import net.jxta.platform.NetworkManager;

import org.hypergraphdb.HGPersistentHandle;
import org.hypergraphdb.handle.UUIDPersistentHandle;
import org.hypergraphdb.peer.DummyPolicy;
import org.hypergraphdb.peer.HyperGraphPeer;
import org.hypergraphdb.query.AtomPartCondition;
import org.hypergraphdb.query.ComparisonOperator;

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

		//HyperGraphPeer server = new HyperGraphPeer(new File("./server1Config"), new DummyPolicy(true));
		//HyperGraphPeer server = new HyperGraphPeer(new File("./config/minimalConfig"), new DummyPolicy(true));
		//HyperGraphPeer server = new HyperGraphPeer(new File("./config/namedPeerConfig"), new DummyPolicy(true));
		//HyperGraphPeer server = new HyperGraphPeer(new File("./config/dirPeerConfig"), new DummyPolicy(true));
		//HyperGraphPeer server = new HyperGraphPeer(new File("./config/transportPeerConfig"), new DummyPolicy(true));
		//HyperGraphPeer server = new HyperGraphPeer(new File("./config/relayPeerConfig"), new DummyPolicy(true));
		HyperGraphPeer server = new HyperGraphPeer(new File("./config/server1Config"), new DummyPolicy(true));
		
		
		if (server.start("user", "pwd"))
		{		
			try
			{
				Thread.sleep(3000);
			} catch (InterruptedException e){}
	
	
			HGPersistentHandle typeHandle = UUIDPersistentHandle.makeHandle("e917bda6-0932-4a66-9aeb-3fc84f04ce57");
			server.registerType(typeHandle, User.class);
			System.out.println("Types registered...");
			
			server.setAtomInterests(new AtomPartCondition(new String[] {"part"}, "5", ComparisonOperator.LT));
	//		server.setAtomInterests(new AnyAtomCondition());
			server.catchUp();
		}else{
			System.out.println("Can not start peer");
		}
	}
}
