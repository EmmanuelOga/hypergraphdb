package org.hypergraphdb.peer.protocol;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

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
	public Object handleRequest(InputStream in, Session session) throws IOException{
		Object result = null;
		
		//get & verify signature
		if (verifySignature(in)){
			//loop -> get message; dispatch message; write response;
			Message msg = messageFactory.build(in);
				
			//dispatch
			result = msg.dispatch(in);
			session.setSerializer(msg.getSerializer());
		}else{
			System.out.println("ERROR: Signature does not match");
		}
		
		//TODO for now just returning the last response
		return result;
	}
	
	public Object handleResponse(InputStream in, Session session) throws IOException{
		Object result = null;
		
		if (verifySignature(in)){
			result = session.getSerializer().deserialize(in);
		}
		
		return result;
	}

	//TODO can send multiple messages?
	public void createRequest(OutputStream out, Message msg, Session session) throws IOException{
		writeSignature(out);
		
		msg.write(out);
		
		session.setSerializer(msg.getSerializer());
	}

	public void createResponse(OutputStream out, Object response, Session session) throws IOException{
		writeSignature(out);

		session.getSerializer().serialize(out, response);
		//write response
		
	}
	
	private boolean verifySignature(InputStream in) throws IOException {
		byte[] signature = new byte[SIGNATURE.length];
		if (in.read(signature) == SIGNATURE.length){
			return Arrays.equals(signature, SIGNATURE);
		}
		
		return false;
	}

	private void writeSignature(OutputStream out) throws IOException{
		out.write(SIGNATURE);

	}
	
}
