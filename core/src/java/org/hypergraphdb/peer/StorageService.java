package org.hypergraphdb.peer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGPersistentHandle;
import org.hypergraphdb.HGStore;
import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.event.HGAtomAddedEvent;
import org.hypergraphdb.event.HGEvent;
import org.hypergraphdb.event.HGListener;
import org.hypergraphdb.peer.log.Log;
import org.hypergraphdb.peer.workflow.RememberTaskClient;

/**
 * @author ciprian.costa
 *
 *  Handles storage in the context of replication - manages log
 */
public class StorageService
{
	private HyperGraph graph;
	private HyperGraph logGraph;

	Set<HGHandle> ownAddedHandles = new HashSet<HGHandle>();
	private PeerInterface peerInterface;
	private Log log;
	
	private boolean autoSkip = false;
	
	public StorageService()
	{
		
	}

	public StorageService(HyperGraph graph, HyperGraph logGraph, PeerInterface peerInterface, Log log)
	{
		this.graph = graph;
		this.logGraph = logGraph;
		this.peerInterface = peerInterface;
		this.log = log;
		
		graph.getEventManager().addListener(HGAtomAddedEvent.class, new AtomAddedListner());
	}
	
	private HGHandle storeSubgraph(Subgraph subGraph, HGStore store)
	{
		return SubgraphManager.store(subGraph, store);
	}
	
	public HGHandle addSubgraph(Subgraph subGraph)
	{
		//TODO remake to add directly to store and INDEX
		HGStore store = logGraph.getStore();
		HGHandle handle = storeSubgraph(subGraph, store);
		
		ownAddedHandles.add(handle);
		
		graph.add((HGPersistentHandle)handle, logGraph.get(handle));
		
		return handle;
		
	}

	
	private class AtomAddedListner implements HGListener
	{

		public Result handle(HyperGraph hg, HGEvent event)
		{
			//someone added an object - get it and propagate (unless it is added by us) ...
			HGAtomAddedEvent addedEvent = (HGAtomAddedEvent)event;
			HGHandle handle = addedEvent.getAtomHandle();
			if (autoSkip || ownAddedHandles.contains(handle))
			{
				//we added it ... just skip
				System.out.println("Own add detected: " + handle);
				ownAddedHandles.remove(handle);
			}else{
				//someone else added ... propagate ... 
				System.out.println("Add to propagate: " + handle);

				RememberTaskClient client = new RememberTaskClient(peerInterface, hg.get(handle), log, hg, hg.getPersistentHandle(handle));
				client.run();
			}
			
			return Result.ok;
		}
		
	}


	public void registerType(HGPersistentHandle handle, Class<?> clazz)
	{
		if ((graph!= null) && (graph.getStore().getLink(handle) == null))
		{
			autoSkip = true;
			graph.getTypeSystem().defineTypeAtom(handle, clazz);
			autoSkip = false;
		}
		
		if(logGraph.getStore().getLink(handle) == null)
		{
			logGraph.getTypeSystem().defineTypeAtom(handle, clazz);
		}
		
	}
}
