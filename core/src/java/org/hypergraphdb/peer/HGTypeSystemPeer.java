package org.hypergraphdb.peer;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGPersistentHandle;
import org.hypergraphdb.HGTypeSystem;
import org.hypergraphdb.handle.HGLiveHandle;
import org.hypergraphdb.peer.protocol.Message;
import org.hypergraphdb.peer.protocol.MessageFactory;
import org.hypergraphdb.peer.protocol.MessageHandler;

public class HGTypeSystemPeer {
	
	private PeerForwarder peerForwarder;  
	private HGTypeSystem typeSystem;
	private MessageFactory messageFactory = new MessageFactory();

	public HGTypeSystemPeer(PeerForwarder peerForwarder, HGTypeSystem typeSystem){
		this.peerForwarder = peerForwarder;
		this.typeSystem = typeSystem;
		
		registerMessageTemplates();
	}
	
	public HGHandle getTypeHandle(Class<?> clazz){
		if (shouldForward()){
			Message msg = messageFactory.build(ServiceType.GET_TYPE_HANDLE, new Object[]{clazz});
			Object result = peerForwarder.forward(msg);
			
			if (result instanceof HGHandle) return (HGHandle) result;
			else return null;
		}else{
			HGHandle handle = typeSystem.getTypeHandle(clazz);
			
	        if (!(handle instanceof HGPersistentHandle))
	            handle = ((HGLiveHandle)handle).getPersistentHandle();
	        
			return handle;			
		}
		
	}
	
	private boolean shouldForward(){
		return (peerForwarder != null);
	}
	
	private void registerMessageTemplates() {
		MessageFactory.registerMessageTemplate(ServiceType.GET_TYPE_HANDLE, new Message(ServiceType.GET_TYPE_HANDLE, new GetTypeHandleMessageHandler()));
	}

	private class GetTypeHandleMessageHandler implements MessageHandler{

		public Object handleRequest(Object[] params) {
			if ((typeSystem != null) && (params[0] instanceof Class)){
				return typeSystem.getTypeHandle((Class)params[0]);
			}else {
				return null;
			}
		}
		
	}
}
