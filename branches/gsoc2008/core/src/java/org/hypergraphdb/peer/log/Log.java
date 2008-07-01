package org.hypergraphdb.peer.log;

import java.util.HashMap;
import java.util.Iterator;

import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.peer.PeerFilter;
import org.hypergraphdb.peer.PeerFilterEvaluator;

/**
 * @author Cipri Costa
 *
 * Manages all log operations. Ensures serialization of events
 */
public class Log
{
	private HyperGraph logDb;
	private HashMap<Object, Peer> peers = new HashMap<Object, Peer>();
	private Timestamp timestamp = new Timestamp();
	
	public Log(HyperGraph logDb)
	{
		this.logDb = logDb;
	}

	public LogEntry createLogEntry(Object value)
	{
		LogEntry entry = new LogEntry(value, logDb);
		
		return entry;
	}
	/**
	 * 
	 * Adds an event to the log.
	 * @param value
	 * @param peerFilter
	 * @return
	 */
	public LogEntry addEntry(LogEntry entry, PeerFilter peerFilter)
	{
		peerFilter.filterTargets();
		Iterator<Object> it = peerFilter.iterator();
		
		//ensure only one at a time is logged
		synchronized(timestamp)
		{
			//get timestamp, save, 
			while (it.hasNext())
			{
				Object target = it.next();
	
				Object targetId = peerFilter.getTargetId(target);
				
				Peer peer = peers.get(targetId);
				if (peer == null)
				{
					peer = new Peer(targetId);
					peers.put(targetId, peer);
				}
				
				//make connection with peer
				
			}
		}
		return entry;
		
	}

	public void purge()
	{
		
	}
	
	
	
}
