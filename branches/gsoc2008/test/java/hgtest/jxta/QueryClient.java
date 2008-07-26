package hgtest.jxta;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import net.jxta.platform.NetworkManager;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGPersistentHandle;
import org.hypergraphdb.HGQuery.hg;
import org.hypergraphdb.handle.UUIDPersistentHandle;
import org.hypergraphdb.peer.DummyPolicy;
import org.hypergraphdb.peer.HyperGraphPeer;
import org.hypergraphdb.peer.PeerFilterEvaluator;
import org.hypergraphdb.peer.jxta.DefaultPeerFilterEvaluator;

public class QueryClient
{
	public static void main(String[] args) throws NumberFormatException, IOException{
		if (args.length != 2)
		{
			System.out.println("arguments: PeerName PeerGroup");
			System.exit(0);
		}

		String peerName = args[0];
		String groupName = args[1];

		System.out.println("Starting a HGDB client ...");

		HyperGraphPeer peer = new HyperGraphPeer(new File("./client1Config"), new DummyPolicy(false));
		
		peer.start("user", "pwd");

		try
		{
			Thread.sleep(3000);
		} catch (InterruptedException e){}
	
		HGPersistentHandle typeHandle = UUIDPersistentHandle.makeHandle("e917bda6-0932-4a66-9aeb-3fc84f04ce57");
		peer.registerType(typeHandle, User.class);
		System.out.println("Types registered...");

		peer.updateNetworkProperties();
		
		//getting users from Server1
		ArrayList<?> result;
		PeerFilterEvaluator evaluator = new DefaultPeerFilterEvaluator("Server1");
		result = peer.query(evaluator, hg.type(User.class), false);
		System.out.println("the client received: " + result);		
		
		for(Object elem:result)
		{
			HGHandle handle = (HGHandle)elem;
			System.out.println(elem + " -> " + peer.query(evaluator, hg.bfs(handle), false));
		}
		
//		result = peer.query(new DefaultPeerFilterEvaluator("Server1"), hg.type(User.class), true);
//		System.out.println("the client received: " + result);
	}
}
