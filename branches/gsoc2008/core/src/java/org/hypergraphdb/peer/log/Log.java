package org.hypergraphdb.peer.log;

import java.util.HashMap;
import java.util.Iterator;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGHandleFactory;
import org.hypergraphdb.HGPersistentHandle;
import org.hypergraphdb.HGPlainLink;
import org.hypergraphdb.HGSearchResult;
import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.handle.UUIDPersistentHandle;
import org.hypergraphdb.peer.PeerFilter;
import org.hypergraphdb.query.And;
import org.hypergraphdb.query.AtomPartCondition;
import org.hypergraphdb.query.AtomTypeCondition;

/**
 * @author Cipri Costa
 *
 * Manages all log operations. Ensures serialization of events
 */
public class Log
{
	public static final HGPersistentHandle LATEST_VERSION_HANDLE =
		HGHandleFactory.makeHandle("136b5d67-7b0c-41f4-a0e0-105f2c42622e");

	private HyperGraph logDb;
	private HashMap<Object, Peer> peers = new HashMap<Object, Peer>();
	private HashMap<Object, HGHandle> peerHandles = new HashMap<Object, HGHandle>();
	
	private Timestamp timestamp;
	
	public Log(HyperGraph logDb)
	{
		this.logDb = logDb;
		
		//initialize with the latest version
		byte[] data = logDb.getStore().getData(LATEST_VERSION_HANDLE);
		if (data == null)
		{
			System.out.println("LATEST_VERSION_HANDLE not found");
			timestamp = new Timestamp();
			HGPersistentHandle handle = logDb.getPersistentHandle(logDb.add(timestamp));
			logDb.getStore().store(LATEST_VERSION_HANDLE, handle.toByteArray());
		}
		else
		{
			HGHandle handle = UUIDPersistentHandle.makeHandle(data);
			timestamp = logDb.get(handle);
			
			System.out.println("LATEST_VERSION_HANDLE : " + timestamp);
		}
		timestamp.moveNext();

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
			Timestamp entryTimestamp = timestamp.moveNext();
			
			entry.setTime(timestamp);
			HGHandle timestampHandle = logDb.add(entryTimestamp);
			
			logDb.getStore().store(LATEST_VERSION_HANDLE, (logDb.getPersistentHandle(timestampHandle)).toByteArray());
			logDb.add(new HGPlainLink(timestampHandle, entry.getLogEntryHandle()));
			
			//get timestamp, save, 
			while (it.hasNext())
			{
				Object target = it.next();
	
				Object targetId = peerFilter.getTargetId(target);
				
				Peer peer = peers.get(targetId);
				if (peer == null)
				{
					//try to find the peer
					HGSearchResult<HGHandle> peerSearchResult = logDb.find(new And(new AtomTypeCondition(Peer.class), new AtomPartCondition(new String[]{"peerId"}, targetId)));
					HGHandle peerHandle;
					if (peerSearchResult.hasNext())
					{
						peerHandle = peerSearchResult.next();
						peer = logDb.get(peerHandle);
					}else{
						peer = new Peer(targetId);
						peerHandle = logDb.add(peer);
					}
					peers.put(targetId, peer);
					peerHandles.put(targetId, peerHandle);						
				}
				
				//make connection with peer
				HGPlainLink link = new HGPlainLink(peerHandles.get(targetId), entry.getLogEntryHandle());
				logDb.add(link);
				entry.setLastTimestamp(targetId, peer.getTimestamp());
				
				peer.setTimestamp(entryTimestamp);
				logDb.replace(peerHandles.get(targetId), peer);
				
				System.out.println(entry.getLastTimestamp(targetId));
			}
		}
		return entry;
	}

	public void purge()
	{
		
	}
	
	
	
}
