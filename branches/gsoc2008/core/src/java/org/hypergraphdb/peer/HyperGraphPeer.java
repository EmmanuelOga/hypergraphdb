package org.hypergraphdb.peer;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.peer.protocol.Message;
import org.hypergraphdb.peer.protocol.MessageHandler;
import org.hypergraphdb.peer.protocol.MessageFactory;

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
	private HyperGraph hg = null;
	
	
	private HGTypeSystemPeer typeSystem = null;
	
	/**
	 * @param configuration
	 */
	public HyperGraphPeer(PeerConfiguration configuration){
		this.configuration = configuration;
	}
	
	public boolean start(){
		
		if (configuration.getHasLocalHGDB()){
			hg = new HyperGraph(configuration.getDatabaseName());
		}
		
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
		
		typeSystem = new HGTypeSystemPeer(peerForwarder, (hg == null) ? null : hg.getTypeSystem());

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

	public HGHandle add(Object atom){		
		System.out.println("adding atom: " + atom.toString());
		
		HGHandle handle = null;
		
		if (shouldForward()){
			// TODO forward
			Message msg = messageFactory.build(ServiceType.ADD, new Object[]{atom});
			Object result = peerForwarder.forward(msg);
			
			if (result instanceof HGHandle){
				handle = (HGHandle)result;
			}
		}else {
			// TODO store locally
			handle = hg.getPersistentHandle(hg.add(atom));
		}
		
		return handle;
	}

	public Object get(HGHandle handle){
		Object result = null;
		
		if (shouldForward()){
			Message msg = messageFactory.build(ServiceType.GET, new Object[]{handle});
			result = peerForwarder.forward(msg);
		}else{
			result = hg.get(handle);
		}
		
		return result;
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

		/* (non-Javadoc)
		 * @see org.hypergraphdb.peer.protocol.MessageHandler#handleRequest(java.lang.Object, java.lang.Object[])
		 * 
		 * TODO: add logic to handle different signatures
		 */
		@Override
		public Object handleRequest(Object[] params) {
			return add(params[0]);
		}
		
	}
	private class GetMessageHandler implements MessageHandler{

		@Override
		public Object handleRequest(Object[] params) {
			if (params[0] instanceof HGHandle){
				return get((HGHandle)params[0]);
			}else return null;
		}
		
	}
	
}
