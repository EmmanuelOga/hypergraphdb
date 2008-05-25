package org.hypergraphdb.peer.jxta;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import net.jxta.socket.JxtaSocket;

import org.hypergraphdb.peer.PeerForwarder;
import org.hypergraphdb.peer.protocol.Message;
import org.hypergraphdb.peer.protocol.Protocol;
import org.hypergraphdb.peer.protocol.Session;

/**
 * @author Cipri Costa
 * 
 * <p>
 * Forwards calls using the JXTA framework.The actual creation of the message is handled by the <code>Protocol</code> class.
 * </p>
 */
public class JXTAPeerForwarder implements PeerForwarder{
	private JXTAPeerConfiguration configuration;
	/**
	 * used to create the message
	 */
	private Protocol protocol = new Protocol();
	
	@Override
	public boolean configure(Object configuration) {
		boolean result = false;
		
		if (configuration instanceof JXTAPeerConfiguration){
			this.configuration = (JXTAPeerConfiguration)configuration;
			result = true;
		}
		System.out.println("Initializing forwarder manager");
		
		if (result){
			result = JXTAManager.init("HGDB1");
		}
		
		return result;		
	}
	
	
	@Override
	public Object forward(Message msg) {
		
		Object result = null;
		
		try {
        	// TODO actually choose peers to forward to
        	String peerId = configuration.getPeers().get(0);
            JxtaSocket socket = new JxtaSocket(JXTAManager.getNetPeerGroup(), null, HGAdvertisementsFactory.newAdvertisement(peerId), 5000, true);
        	
            OutputStream out = socket.getOutputStream();
            InputStream in = socket.getInputStream();
            Session session = new Session();
            
            //send message
            protocol.createRequest(out, msg, session);
            out.flush();
            System.out.println("Client sent a command");

            //receive answer
            result = protocol.handleResponse(in, session);
            
            //TODO log
            if (result != null){
            	System.out.println("received result: " + result.toString());
            }else{
            	System.out.println("received result: null");            	
            }            
        } catch (IOException e) {
            System.out.println("OutputPipe creation failure");
            e.printStackTrace();
        }

		return result;
	}

}
