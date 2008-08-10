package hgtest.jxta;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import net.jxta.platform.NetworkManager;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGPersistentHandle;
import org.hypergraphdb.HGQuery.hg;
import org.hypergraphdb.handle.UUIDPersistentHandle;
import org.hypergraphdb.peer.HyperGraphPeer;
import org.hypergraphdb.peer.PeerFilterEvaluator;
import org.hypergraphdb.peer.RemotePeer;
import org.hypergraphdb.peer.jxta.DefaultPeerFilterEvaluator;

public class QueryClient
{
	public static void main(String[] args) throws NumberFormatException, IOException{
		System.out.println("Starting a HGDB client ...");

		HyperGraphPeer peer = new HyperGraphPeer(new File("./config/client1Config"));
		
		peer.start("user", "pwd");

		try
		{
			Thread.sleep(3000);
		} catch (InterruptedException e){}
	
		HGPersistentHandle typeHandle = UUIDPersistentHandle.makeHandle("e917bda6-0932-4a66-9aeb-3fc84f04ce57");
		peer.registerType(typeHandle, User.class);
		System.out.println("Types registered...");

		peer.updateNetworkProperties();
		
		
		RemotePeer remotePeer = peer.getRemotePeer("Server1");
		if (remotePeer != null)
		{
			ArrayList<?> result = remotePeer.query(hg.type(User.class), false);
			
			//getting users from Server1
			for(Object elem:result)
			{
				HGHandle handle = (HGHandle)elem;
				System.out.println("received: " + elem + " -> " + remotePeer.get(handle));
			}
		}
		
		HGHandle addedHandle = remotePeer.add(new User(11, "user 11"));
		System.out.println("added: " + addedHandle);
		System.out.println("the value = " + remotePeer.get(addedHandle));
		
		remotePeer.replace((HGPersistentHandle)addedHandle, new User(11, "new user 11"));
		System.out.println("updated: " + addedHandle);
		System.out.println("the value = " + remotePeer.get(addedHandle));
		
		
		remotePeer.remove((HGPersistentHandle)addedHandle);
		System.out.println("removed: " + addedHandle);
		System.out.println("the value = " + remotePeer.get(addedHandle));
		
		
//		result = peer.query(new DefaultPeerFilterEvaluator("Server1"), hg.type(User.class), true);
//		System.out.println("the client received: " + result);
	}
}
