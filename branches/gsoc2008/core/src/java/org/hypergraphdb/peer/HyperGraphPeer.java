package org.hypergraphdb.peer;

import static org.hypergraphdb.peer.Structs.getPart;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGPersistentHandle;
import org.hypergraphdb.HGSearchResult;
import org.hypergraphdb.HGStore;
import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.peer.jxta.DefaultPeerFilterEvaluator;
import org.hypergraphdb.peer.log.Log;
import org.hypergraphdb.peer.protocol.Performative;
import org.hypergraphdb.peer.serializer.GenericSerializer;
import org.hypergraphdb.peer.serializer.JSONReader;
import org.hypergraphdb.peer.workflow.CatchUpTaskClient;
import org.hypergraphdb.peer.workflow.CatchUpTaskServer;
import org.hypergraphdb.peer.workflow.GetInterestsTask;
import org.hypergraphdb.peer.workflow.PublishInterestsTask;
import org.hypergraphdb.peer.workflow.QueryTaskClient;
import org.hypergraphdb.peer.workflow.QueryTaskServer;
import org.hypergraphdb.peer.workflow.RememberTaskClient;
import org.hypergraphdb.peer.workflow.RememberTaskServer;
import org.hypergraphdb.query.AtomTypeCondition;
import org.hypergraphdb.query.HGAtomPredicate;
import org.hypergraphdb.query.HGQueryCondition;

/**
 * @author Cipri Costa
 *
 * <p>
 * Main class that implements the services accessible through the peer interface.
 * 
 * </p>
 */
public class HyperGraphPeer {
	
	private Object configuration;
	
	/**
	 * object used for communicating with other peers
	 */
	private PeerInterface peerInterface = null;
		
	/**
	 * The peer can be configured to store atoms in this local database
	 */
	private HyperGraph graph = null;
	/**
	 * this is used for serializing object that need to be sent to other peers (TODO: rename to tempDB)
	 */
	private HyperGraph cacheGraph = null;
	
	private HGTypeSystemPeer typeSystem = null;

	private PeerPolicy policy;
	
	private Log log;
	
	public HyperGraphPeer(PeerPolicy policy)
	{
		this.policy = policy;
	}
	
	public HyperGraphPeer(Object configuration, PeerPolicy policy)
	{
		this.configuration = configuration;
		this.policy = policy;
	}
	
	public HyperGraphPeer(File configFile, PeerPolicy policy)
	{
		loadConfig(configFile);
		this.policy = policy;
	}
	
	public void loadConfig(File configFile)
	{
		JSONReader reader = new JSONReader();

		try
		{
			configuration = getPart(reader.read(getContents(configFile)));
			
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	private String getContents(File file) throws IOException
	{
		StringBuilder contents = new StringBuilder();
	
		BufferedReader input =  new BufferedReader(new FileReader(file));
		try 
		{
			String line = null; 
			while (( line = input.readLine()) != null)
			{
				contents.append(line);
				contents.append(System.getProperty("line.separator"));
			}
		}
	    finally 
	    {
	    	input.close();
	    }

	    return contents.toString();
	}
	
	public boolean start()
	{
		boolean status = true;
		if (configuration != null)
		{
			//get required objects
			try
			{
				//create the local database
				boolean hasLocalStorage = (Boolean)getPart(configuration, PeerConfig.HAS_LOCAL_STORAGE);
				if (hasLocalStorage)
				{
					graph = new HyperGraph((String)getPart(configuration, PeerConfig.LOCAL_DB));
				}
				
				//create cache database - this should eventually be an actual cache, not just another database
				cacheGraph = new HyperGraph((String)getPart(configuration, PeerConfig.TEMP_DB)); 
				GenericSerializer.setTempDB(cacheGraph);

				//load and start interface
				String peerInterfaceType = (String)getPart(configuration, PeerConfig.INTERFACE_TYPE);
				peerInterface = (PeerInterface)Class.forName(peerInterfaceType).getConstructor().newInstance();
				
				if (peerInterface != null){
					peerInterface.configure(configuration);
					
					Thread thread = new Thread(peerInterface, "peerInterface");
	                thread.start();
	                
	                //configure services
	                if (hasLocalStorage)
	                {
	        			peerInterface.registerTaskFactory(Performative.CallForProposal, HGDBOntology.REMEMBER_ACTION, new RememberTaskServer.RememberTaskServerFactory(this));
	        			peerInterface.registerTaskFactory(Performative.Request, HGDBOntology.ATOM_INTEREST, new PublishInterestsTask.PublishInterestsFactory());
	        			peerInterface.registerTaskFactory(Performative.Request, HGDBOntology.QUERY, new QueryTaskServer.QueryTaskFactory(this));
	                }else{
	        			peerInterface.registerTaskFactory(Performative.Request, HGDBOntology.CATCHUP, new CatchUpTaskServer.CatchUpTaskServerFactory(this));
	                }
	        		peerInterface.registerTaskFactory(Performative.Inform, HGDBOntology.ATOM_INTEREST, new GetInterestsTask.GetInterestsFactory());

	        		typeSystem = new HGTypeSystemPeer(peerInterface, (graph == null) ? null : graph.getTypeSystem());
	        		log = new Log(cacheGraph, peerInterface);

	        		//TODO: this should not be an indefinite wait ... 
	        		if (!hasLocalStorage)
	        		{
	                	peerInterface.getPeerNetwork().waitForRemotePipe();
	                }

				}else{
					status = false;
					System.out.println("Can not start HGBD: peer interface could not be instantiated");
				}

			}catch(Exception ex){
				status = false;
				System.out.println("Can not start HGBD: " + ex);
			}
		}else {
			status = false;
			System.out.println("Can not start HGBD: configuration not loaded");
		}
		
		return status;
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

	public void registerType(HGPersistentHandle handle, Class<?> clazz)
	{
		if ((graph!= null) && (graph.getStore().getLink(handle) == null))
		{
			graph.getTypeSystem().defineTypeAtom(handle, clazz);
		}
		
		if(cacheGraph.getStore().getLink(handle) == null)
		{
			cacheGraph.getTypeSystem().defineTypeAtom(handle, clazz);
		}
	}

	public HGSearchResult<HGHandle> find(HGQueryCondition query)
	{
		return graph.find(query);
	}

	public HGPersistentHandle getPersistentHandle(HGHandle handle)
	{
		return graph.getPersistentHandle(handle);
	}

	public ArrayList<?> query(PeerFilterEvaluator evaluator, HGQueryCondition condition, boolean getObjects)
	{
		QueryTaskClient queryTask = new QueryTaskClient(peerInterface, cacheGraph, evaluator, condition, getObjects);
		queryTask.run();
		
		return queryTask.getResult();
	}

	public Object query(PeerFilterEvaluator evaluator, HGHandle handle)
	{
		QueryTaskClient queryTask = new QueryTaskClient(peerInterface, cacheGraph, evaluator, handle);
		queryTask.run();
		
		ArrayList<?> result = queryTask.getResult();

		if (result.size() > 0) return result.get(0);
		else return null;
	}

}
