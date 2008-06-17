package org.hypergraphdb.peer.protocol;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author Cipri Costa
 *
 * <p>
 * Class that manages the basic format of the input/output streams. All functions use the <code>Session</code> as a 
 * mean of sending data from one function to another (the functions are usually called in pair). 
 * </p>
 * 
 * <p>
 * Usually, the "client" peer will call createRequest and then handleResponse while the "server" peer will call handleRequest
 * followed by a createResponse call.
 * </p> 
 */
public class Protocol {
	private final static byte[] SIGNATURE = "HGBD".getBytes();
	private static MessageFactory messageFactory = new MessageFactory();
	
	
	public Protocol(){
		
	}
	
	
	
	/**
	 * @param in
	 * @param session 
	 * @return
	 * @throws IOException
	 */
	public Message readMessage(InputStream in, Session session) throws IOException{
		Message result = null;
		
		//get & verify signature
		if (ProtocolUtils.verifySignature(in, SIGNATURE)){
			ObjectSerializer serializer = new ObjectSerializer();
			
			result = (Message)serializer.deserialize(in);
			
			//			Message msg = messageFactory.build(in);
				
			//dispatch
			session.setSerializer(serializer);
		}else{
			System.out.println("ERROR: Signature does not match");
		}
		
		//TODO for now just returning the last response
		return result;
	}
	
	public Object handleResponse(InputStream in, Session session) throws IOException{
		Object result = null;
		
		if (ProtocolUtils.verifySignature(in, SIGNATURE)){
			result = session.getSerializer().deserialize(in);
		}
		
		return result;
	}

	//TODO can send multiple messages?
	public void writeMessage(OutputStream out, Message msg, Session session) throws IOException{
		writeSignature(out);

		//TODO serialization type should be configurable
		ObjectSerializer serializer = new ObjectSerializer();
		serializer.serialize(out, msg);
		
		//TODO no longer needed
		session.setSerializer(serializer);
	}

	public void createResponse(OutputStream out, Object response, Session session) throws IOException{
		writeSignature(out);

		session.getSerializer().serialize(out, response);
		//write response
		
	}
	

	private void writeSignature(OutputStream out) throws IOException{
		out.write(SIGNATURE);

	}
	
}
