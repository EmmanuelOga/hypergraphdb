package org.hypergraphdb.peer.log;

import org.hypergraphdb.HGPersistentHandle;
import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.peer.Subgraph;

public class LogEntry
{
	private Subgraph data;
	private HGPersistentHandle logEntryHandle;
	public LogEntry(Object value, HyperGraph logDb)
	{
		logEntryHandle = logDb.getPersistentHandle(logDb.add(value));
		
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
	
}
