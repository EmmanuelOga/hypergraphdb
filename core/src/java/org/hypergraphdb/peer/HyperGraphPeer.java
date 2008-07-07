package org.hypergraphdb.peer;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGPersistentHandle;
import org.hypergraphdb.HGStore;
import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.peer.log.Log;
import org.hypergraphdb.peer.protocol.Performative;
import org.hypergraphdb.peer.serializer.GenericSerializer;
import org.hypergraphdb.peer.workflow.CatchUpTaskClient;
import org.hypergraphdb.peer.workflow.CatchUpTaskServer;
import org.hypergraphdb.peer.workflow.GetInterestsTask;
import org.hypergraphdb.peer.workflow.PublishInterestsTask;
import org.hypergraphdb.peer.workflow.RememberTaskClient;
import org.hypergraphdb.peer.workflow.RememberTaskServer;
import org.hypergraphdb.query.HGAtomPredicate;

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
	 * object used for communicating with other peers
	 */
	private PeerInterface peerInterface = null;
		
	/**
	 * The peer can be configured to store atoms in this local database
	 */
	private HyperGraph graph = null;
	/**
	 * this is used for serializing object that need to be sent to other peers
	 */
	private HyperGraph cacheGraph = null;
	
	private HGTypeSystemPeer typeSystem = null;

	private PeerPolicy policy;
	
	private Log log;
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
		
		GenericSerializer.setTempDB(cacheGraph);
		
		if (configuration.getCanForwardRequests() || configuration.getHasServerInterface()){
			try{
				peerInterface = (PeerInterface)Class.forName(configuration.getPeerInterfaceType()).getConstructor().newInstance();
			}catch(Exception ex){
				ex.printStackTrace();
			}
			
			if (peerInterface != null){
				//create type system peer

				peerInterface.configure(configuration.getPeerInterfaceConfiguration());
				
				Thread thread = new Thread(peerInterface, "peerInterface");
                thread.start();
			}
		}

		if (configuration.getHasServerInterface()){
			peerInterface.registerTaskFactory(Performative.CallForProposal, HGDBOntology.REMEMBER_ACTION, new RememberTaskServer.RememberTaskServerFactory(this));
			peerInterface.registerTaskFactory(Performative.Request, HGDBOntology.ATOM_INTEREST, new PublishInterestsTask.PublishInterestsFactory());
		}else{
			peerInterface.registerTaskFactory(Performative.Request, HGDBOntology.CATCHUP, new CatchUpTaskServer.CatchUpTaskServerFactory(this));
		}

		peerInterface.registerTaskFactory(Performative.Inform, HGDBOntology.ATOM_INTEREST, new GetInterestsTask.GetInterestsFactory());
		
		typeSystem = new HGTypeSystemPeer(peerInterface, (graph == null) ? null : graph.getTypeSystem());
		log = new Log(cacheGraph, peerInterface);

		// TODO actually compute this
		return true;
	}
	
	public void catchUp()
	{
		CatchUpTaskClient catchUpTask = new CatchUpTaskClient(peerInterface, null, this);
		catchUpTask.run();
	}
	public void setAtomInterests(HGAtomPredicate pred)
	{
		peerInterface.setAtomInterests(pred);
		
		PublishInterestsTask publishTask = new PublishInterestsTask(peerInterface, pred);
		publishTask.run();
	}
	

	void stop(){
		
	}

	
	/**
	 * This is where objects "enter" the system. The peer might decide to store them locally or forward them to other peers. 
	 * 
	 * @param value
	 * @return
	 */
	public HGHandle add(Object value){		
		System.out.println("adding atom: " + value.toString());
		
		HGHandle handle = null;
		
		if (policy.shouldStore(value))
		{
			//add to local store and return handle
			handle = graph.getPersistentHandle(graph.add(value));
		}else{
			RememberTaskClient activity = new RememberTaskClient(peerInterface, value, log, cacheGraph);
			activity.run();
			handle = activity.getResult();
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
		//TODO remake to add directly to store and INDEX
		HGStore store = cacheGraph.getStore();
		HGHandle handle = storeSubgraph(subGraph, store);
		
		graph.add((HGPersistentHandle)handle, cacheGraph.get(handle));
		return handle;
	}

	private HGHandle storeSubgraph(Subgraph subGraph, HGStore store)
	{
		return SubgraphManager.store(subGraph, store);
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
			Object peerResult = null;//peerInterface.forward(null, msg);
			if (peerResult != null)
			{
				Subgraph subgraph = (Subgraph)peerResult;
		
				//store the result in cache
				storeSubgraph(subgraph, cacheGraph.getStore());
			
				//return result
				result = cacheGraph.get(handle);

				//TODO: delete from local storage
			}			
		}
		
		return result;
	}
	
	//TODO use streams?
	public Subgraph getSubgraph(HGHandle handle)
	{
		if (graph.getStore().containsLink((HGPersistentHandle)handle))
		{
			System.out.println("Handle found in local repository");
			return new Subgraph(graph, (HGPersistentHandle)handle);			
		}else {
			System.out.println("Handle NOT found in local repository");
			return null;
		}
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

	/**
	 * will broadcast messages and update the peers knowledge of the neighbouring peers
	 */
	public void updateNetworkProperties()
	{
		GetInterestsTask task = new GetInterestsTask(peerInterface);
		
		task.run();
	}

	public Log getLog()
	{
		return log;
	}

	public void setLog(Log log)
	{
		this.log = log;
	}
	
	
/*	private boolean shouldForward() {
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
*/}
