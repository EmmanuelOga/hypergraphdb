package org.hypergraphdb.peer;

import java.util.Iterator;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGPersistentHandle;
import org.hypergraphdb.HGStore;
import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.peer.protocol.Message;
import org.hypergraphdb.peer.protocol.MessageFactory;
import org.hypergraphdb.peer.protocol.MessageHandler;
import org.hypergraphdb.util.Pair;

/**
 * @author Cipri Costa
 *
 * <p>
 * Main class that implements the services accessible through the peer interface.
 * 
 * </p>
 */
public class HyperGraphPeer {
	
	private PeerConfiguration configuration;
	
	/**
	 * the object starts the server interface of the peer. Messages are received by this object and forwarded to functions in this class.
	 */
	private ServerInterface serverInterface = null;
	/**
	 * object used for sending requests to peers
	 */
	private PeerForwarder peerForwarder = null;
	/**
	 * The factory is configured by the peer. The template messages are then use to send/receive communications to/from peers
	 */
	private MessageFactory messageFactory = new MessageFactory();
	
	/**
	 * The peer can be configured to store atoms in this local database
	 */
	private HyperGraph graph = null;
	private HyperGraph cacheGraph = null;
	
	private HGTypeSystemPeer typeSystem = null;

	private PeerPolicy policy;
	
	/**
	 * @param configuration
	 */
	public HyperGraphPeer(PeerConfiguration configuration, PeerPolicy policy){
		this.configuration = configuration;
		this.policy = policy;
	}
	
	public boolean start(){
		
		//create the local database
		if (configuration.getHasLocalHGDB()){
			graph = new HyperGraph(configuration.getDatabaseName());
		}
		
		//create cache database - this should eventually be an actual cache, not just another database
		cacheGraph = new HyperGraph(configuration.getCacheDatabaseName()); 
		
		registerMessageTemplates();

		if (configuration.getHasServerInterface()){
			try{
				serverInterface = (ServerInterface)Class.forName(configuration.getServerInterfaceType()).getConstructor().newInstance();				
			}catch(Exception ex){
				ex.printStackTrace();
			}

			if (serverInterface != null){
				
				if(serverInterface.configure(configuration.getServerInterfaceConfiguration())){
					Thread thread = new Thread(serverInterface, "ServerInterface");
	                thread.start();
				}
			}
		}
	
		if (configuration.getCanForwardRequests()){
			try{
				peerForwarder = (PeerForwarder)Class.forName(configuration.getPeerForwarderType()).getConstructor().newInstance();

			}catch(Exception ex){
				ex.printStackTrace();
			}
			
			if (peerForwarder != null){
				//create type system peer

				peerForwarder.configure(configuration.getPeerForwarderConfiguration());

			}
		}
		
		typeSystem = new HGTypeSystemPeer(peerForwarder, (graph == null) ? null : graph.getTypeSystem());

		// TODO actually compute this
		return true;
	}
	
	private void registerMessageTemplates() {
		//set up message templates
		MessageFactory.registerMessageTemplate(ServiceType.ADD, new Message(ServiceType.ADD, new AddMessageHandler()));
		MessageFactory.registerMessageTemplate(ServiceType.GET, new Message(ServiceType.GET, new GetMessageHandler()));
	}

	void stop(){
		
	}

	
	/**
	 * This is where objects "enter" the system. The peer might decide to store them locally or forward them to other peers. 
	 * 
	 * @param atom
	 * @return
	 */
	public HGHandle add(Object atom){		
		System.out.println("adding atom: " + atom.toString());
		
		HGHandle handle = null;
		
		if (policy.shouldStore(atom))
		{
			//add to local store and return handle
			handle = graph.getPersistentHandle(graph.add(atom));
		}else{
			HGPersistentHandle cacheHandle = cacheGraph.getPersistentHandle(cacheGraph.add(atom));
			
			Subgraph subGraph = new Subgraph(cacheGraph, cacheHandle);
			
			Message msg = messageFactory.build(ServiceType.ADD, new Object[]{subGraph});
			Object result = peerForwarder.forward(msg);

			if (result instanceof HGHandle){
				handle = (HGHandle)result;
			}
		}

		return handle;
	}

		
	/**
	 * This is where serialized atoms reach the peer. They have been previously serialized by another entry peer.
	 * The function will get info out of the subgraph and decide what to store and eventually what to forward (not yet implemented).
	 * 
	 * @param graph
	 * @return
	 */
	public HGHandle addSubgraph(Subgraph subGraph)
	{
		HGStore store = graph.getStore();
		return storeSubgraph(subGraph, store);
		
	}

	private HGHandle storeSubgraph(Subgraph subGraph, HGStore store)
	{
		HGHandle handle = null;
		
		Iterator<Pair<HGPersistentHandle, Object>> iter = subGraph.iterator();
		while(iter.hasNext())
		{
			Pair<HGPersistentHandle, Object> item = iter.next();

			//return the first handle 
			if (handle == null) handle = item.getFirst();
			
			//TODO should make sure the handle is not already in there? 
			if (item.getSecond() instanceof byte[])
			{
				store.store(item.getFirst(), (byte[])item.getSecond());
			}else{
				store.store(item.getFirst(), (HGPersistentHandle[])item.getSecond());
			}
		}
		
		return handle;
	}
	
	public Object get(HGHandle handle){
		Object result = null;
		
		//check local db, see if the object exists locally
		if (graph != null)
		{
			result = graph.get(handle);
		}
		
		//if not locally stored
		//TODO need a better way to see if we queried for a non existing object
		if (result == null)
		{
			//TODO optimization - check cache, only get what we need from server
			//get data from the other peer
			Message msg = messageFactory.build(ServiceType.GET, new Object[]{handle});
			Subgraph subgraph = (Subgraph)peerForwarder.forward(msg);
		
			//store the result in cache
			storeSubgraph(subgraph, cacheGraph.getStore());
			
			//return result
			result = cacheGraph.get(handle);
			
			//TODO: delete from local storage
			
		}
		
		return result;
	}
	
	//TODO use streams?
	public Subgraph getSubgraph(HGHandle handle)
	{
		return new Subgraph(graph, (HGPersistentHandle)handle);
	}
	
	/**
	 * @param clazz
	 * @return
	 * 
	 * TODO: this should return TypeSystem interface when common interfaces are defined ...  
	 */
	public HGTypeSystemPeer getTypeSystem(){
		return typeSystem;
	}
	
	private boolean shouldForward() {
		// TODO add logic to see if the atom should be added here
		return configuration.getCanForwardRequests();
	}

	private class AddMessageHandler implements MessageHandler{
		public Object handleRequest(Object params[])
		{
			return addSubgraph((Subgraph)params[0]);
		}

		
	}
	private class GetMessageHandler implements MessageHandler{

		public Object handleRequest(Object[] params) {
			if (params[0] instanceof HGHandle){
				return getSubgraph((HGHandle)params[0]);
			}else return null;
		}
		
	}

}
