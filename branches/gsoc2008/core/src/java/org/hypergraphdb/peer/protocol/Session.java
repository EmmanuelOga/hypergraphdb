package org.hypergraphdb.peer.protocol;

/**
 * @author Cipri Costa
 *
 * <p>
 * The session is an object that exist wile a message is handled. It is used to store data that has to be available 
 * all through the handler.
 * </p>
 */
public class Session {
	/**
	 * The serializer used by the handler. Both the request and the response must use the same handler.
	 */
	private ObjectSerializer serializer;
	
	public Session(){
		
	}

	public ObjectSerializer getSerializer() {
		return serializer;
	}

	public void setSerializer(ObjectSerializer serializer) {
		this.serializer = serializer;
	}
}
