package org.hypergraphdb.peer.protocol;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import org.hypergraphdb.peer.ServiceType;

/**
 * @author Cipri Costa
 * <p>
 * Handles various creation strategies for messages. This is the class where the type of serializer used should be determined.
 * </p>
 */
public class MessageFactory {
	/**
	 * all the templates that are used for exposed service types
	 */
	private static HashMap<ServiceType, OldMessage> messageTemplates = new HashMap<ServiceType, OldMessage>();
	
	public static void registerMessageTemplate(ServiceType serviceType, OldMessage msg){
		messageTemplates.put(serviceType, msg);
	}
	
	/**
	 * @param in Stream that holds the message
	 * @return The Message object that can be used to further read the stream content 
	 * @throws IOException
	 * 
	 * This function just detects the message type and creates a message object from the appropriate template
	 */
	public OldMessage build(InputStream in) throws IOException{
		//read message type
		ServiceType serviceType = getServiceType(in);
		//read encoding type

		OldMessage msg = getMessage(serviceType);
						
		return msg;
	}

	/**
	 * @param serviceType
	 * @param params
	 * @return
	 * 
	 * Creates a message for a given service and a set of parameters. 
	 */
	public OldMessage build(ServiceType serviceType, Object[] params){
		
		OldMessage msg = getMessage(serviceType);

		if (msg != null){
			msg.setParams(params);
		}
		
		return msg;
	}
	
	private OldMessage getMessage(ServiceType serviceType){
		//create message
		OldMessage msg = null;
		OldMessage msgTemplate = messageTemplates.get(serviceType);
		if (msgTemplate != null){
			msg = msgTemplate.clone();
		}
		
		//configure &load message
		if (msg != null){
			//set object serializer
			//TODO factory of some sort to create other types of serializers
			msg.setSerializer(new ObjectSerializer());
		}

		return msg;
	}
	
	private static ServiceType getServiceType(InputStream in) throws IOException{
		ServiceType type = null;
		
		int pos = in.read();
		
		ServiceType[] values = ServiceType.values();
		if ((pos > -1) && (pos < values.length)){
			type = values[pos];
		}
		
		System.out.println("MessageFactory read: " + type.name());
		return type;
	}

}
