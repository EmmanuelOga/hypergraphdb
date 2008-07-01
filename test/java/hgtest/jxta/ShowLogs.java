package hgtest.jxta;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGSearchResult;
import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.peer.log.Log;
import org.hypergraphdb.peer.log.Peer;
import org.hypergraphdb.peer.log.Timestamp;
import org.hypergraphdb.query.AtomTypeCondition;

public class ShowLogs
{

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		HyperGraph hg = new HyperGraph("./DBs/Client1CacheDB");
		
		//Log log  = new Log(hg);
		
		HGSearchResult<HGHandle> timestamps = hg.find(new AtomTypeCondition(Timestamp.class));
		
		System.out.println("Timestamps: ");
		while(timestamps.hasNext())
		{
			HGHandle handle = timestamps.next();
			System.out.println("Timestamp: " + handle + " = " + hg.get(handle));
			
			for(HGHandle neighbourHandle : hg.getIncidenceSet(handle))
			{
				System.out.println("   reference: " + hg.get(neighbourHandle));
			}
		}

		HGSearchResult<HGHandle> peers = hg.find(new AtomTypeCondition(Peer.class));
		
		System.out.println("Peers: ");
		while(peers.hasNext())
		{
			HGHandle handle = peers.next();
			System.out.println("Peer: " + handle + " = " + hg.get(handle));
			
		/*	for(HGHandle neighbourHandle : hg.getIncidenceSet(handle))
			{
				System.out.println("   reference: " + hg.get(neighbourHandle));
			}*/
		}

		hg.close();
		System.out.println("done");
	}

}
