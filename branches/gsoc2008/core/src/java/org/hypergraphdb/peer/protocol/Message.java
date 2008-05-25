package org.hypergraphdb.peer.protocol;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.hypergraphdb.peer.HyperGraphPeer;
import org.hypergraphdb.peer.ServiceType;

/**
 * @author Cipri Costa
 *
 * <p>
 * Used by <code>Protocol</code> to read/write service requests. Also capable of executing a message against the local peer.
 * </p>
 */
public class Message {
	private ServiceType serviceType;
	/**
	 * serializer used for parameters
	 */
	private ObjectSerializer serializer;
	/**
	 * peer to execute requests on
	 */
	private HyperGraphPeer hg;
	/**
	 * Service request parameters
	 */
	private Object params[];
	
	/**
	 * Does the actual execution of the message on the local peer.
	 */
	private MessageHandler handler;
	
	public Message(ServiceType serviceType, MessageHandler handler, HyperGraphPeer hg){
		this.serviceType = serviceType;
		this.handler = handler;
		this.hg = hg;
	}
	
	public Message(ServiceType serviceType, MessageHandler handler){
		this.serviceType = serviceType;
		this.handler = handler;
	}
	
	public Message clone(){
		Message msg = new Message(serviceType, handler, hg);
		return msg;
	}

	/**
	 * @param in Stream to read the message from
	 * @return The result of the message execution
	 * @throws IOException
	 */
	public Object dispatch(InputStream in) throws IOException {
		//get param count
		int paramCount = in.read();
		
		//load params
		params = new Object[paramCount];
		for(int i=0;i<paramCount;i++){
			params[i] = serializer.deserialize(in);
			
			System.out.println("param: " + params[i].toString());
		}
		
		//execute & return
		return handler.handleRequest(hg, params);
	}
	
	/**
	 * @param out The streem to write the message to.
	 * @throws IOException
	 */
	public void write(OutputStream out) throws IOException {
		//write service type
		out.write(serviceType.ordinal());
		
		//write param count
		out.write(params.length);
		
		//write values
		for(Object param : params){
			serializer.serialize(out, param);
		}
		
	}
	
	public ObjectSerializer getSerializer() {
		return serializer;
	}

	public void setSerializer(ObjectSerializer serializer) {
		this.serializer = serializer;
	}

	public Object[] getParams() {
		return params;
	}

	public void setParams(Object[] params) {
		this.params = params;
	}


}
