package org.hypergraphdb.peer.log;

import java.util.HashMap;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGPersistentHandle;
import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.peer.Subgraph;

/**
 * @author ciprian.costa
 * Simple class that holds a log entry and its relations with the other peers.
 */
public class LogEntry implements Comparable<LogEntry>
{
	private Subgraph data;
	private HGPersistentHandle logEntryHandle;
	private HashMap<Object, Timestamp> lastTimestamps = new HashMap<Object, Timestamp>();
	Timestamp timestamp;
	
	public LogEntry(Object value, HyperGraph logDb)
	{
		this(value, logDb, null);
	}

	public LogEntry(Object value, HyperGraph logDb, HGPersistentHandle handle)
	{
		logEntryHandle = logDb.getPersistentHandle(logDb.add(handle, value));
		
		data = new Subgraph(logDb, logEntryHandle);
	}
	
	public LogEntry(HGHandle handle, HyperGraph logDb, Timestamp timestamp)
	{
		logEntryHandle = logDb.getPersistentHandle(handle);
		this.timestamp = timestamp;
		
		data = new Subgraph(logDb, logEntryHandle);
	}
	
	public Subgraph getData()
	{
		return data;
	}
	public void setData(Subgraph data)
	{
		this.data = data;
	}

	public HGPersistentHandle getLogEntryHandle()
	{
		return logEntryHandle;
	}

	public void setLogEntryHandle(HGPersistentHandle logEntryHandle)
	{
		this.logEntryHandle = logEntryHandle;
	}

	public void setTimestamp(Timestamp timestamp)
	{
		this.timestamp = timestamp;
	}
	public Timestamp getTimestamp()
	{
		return timestamp;
	}
	
	public void setLastTimestamp(Object targetId, Timestamp timestamp)
	{
		lastTimestamps.put(targetId, timestamp);
	}
	public Timestamp getLastTimestamp(Object targetId)
	{
		return lastTimestamps.get(targetId);
	}

	public int compareTo(LogEntry value)
	{
		if (value == null) return 1;
		else return timestamp.compareTo(value.getTimestamp());
	}
}
