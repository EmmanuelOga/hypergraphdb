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

/*		JXTAPeerConfiguration jxtaConf = new JXTAPeerConfiguration();
		jxtaConf.setPeerName(peerName);
		jxtaConf.setPeerGroupName(groupName);
		jxtaConf.setAdvTimeToLive(1*10*1000);
		//jxtaConf.setNeedsRdvConn(true);
		jxtaConf.setNeedsRdvConn(false);
		//jxtaConf.setNeedsRelayConn(true);
		jxtaConf.setNeedsRelayConn(false);
		jxtaConf.setMode(NetworkManager.ConfigMode.ADHOC);
*/
/*		PeerConfiguration conf = new PeerConfiguration(true, "./DBs/" + peerName + "DB", 
				true, false, "org.hypergraphdb.peer.jxta.JXTAPeerInterface", jxtaConf, 
				"./DBs/" + peerName + "CacheDb");
*/		
		HyperGraphPeer server = new HyperGraphPeer(new File("./server1Config"), new DummyPolicy(true));
		
		server.start();
		
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
	}
}
