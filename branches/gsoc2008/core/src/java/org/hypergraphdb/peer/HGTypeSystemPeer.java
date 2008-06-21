package org.hypergraphdb.peer;

import javax.xml.soap.MessageFactory;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGPersistentHandle;
import org.hypergraphdb.HGTypeSystem;
import org.hypergraphdb.handle.HGLiveHandle;

public class HGTypeSystemPeer {
	
	private PeerInterface peerForwarder;  
	private HGTypeSystem typeSystem;
//	private MessageFactory messageFactory = new MessageFactory();

	public HGTypeSystemPeer(PeerInterface peerForwarder, HGTypeSystem typeSystem){
		this.peerForwarder = peerForwarder;
		this.typeSystem = typeSystem;
		
		registerMessageTemplates();
	}
	
	public HGHandle getTypeHandle(Class<?> clazz){
		if (shouldForward()){

			Object result = null;//peerForwarder.forward(null, msg);
			
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
		//MessageFactory.registerMessageTemplate(ServiceType.GET_TYPE_HANDLE, new OldMessage(ServiceType.GET_TYPE_HANDLE, new GetTypeHandleMessageHandler()));
	}

/*	private class GetTypeHandleMessageHandler implements MessageHandler{

		public Object handleRequest(Object[] params) {
			if ((typeSystem != null) && (params[0] instanceof Class)){
				return typeSystem.getTypeHandle((Class)params[0]);
			}else {
				return null;
			}
		}
		
	}*/
}
