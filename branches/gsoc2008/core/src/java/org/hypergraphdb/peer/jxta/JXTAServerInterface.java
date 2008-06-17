package org.hypergraphdb.peer.jxta;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import net.jxta.id.IDFactory;
import net.jxta.pipe.PipeID;
import net.jxta.protocol.PipeAdvertisement;
import net.jxta.socket.JxtaServerSocket;

import org.hypergraphdb.peer.ServerInterface;
import org.hypergraphdb.peer.protocol.Protocol;
import org.hypergraphdb.peer.protocol.Session;


/**
 * @author Cipri Costa
 *
 * <p>
 * Class that receives service requests from the JXTA network and forwards them to the service implementation. 
 * </p>
 */
public class JXTAServerInterface implements ServerInterface{

	private JXTAPeerConfiguration config;
	/**
	 * used to format messages
	 */
	private Protocol protocol = new Protocol();
	private JXTANetwork jxtaNetwork = new DefaultJXTANetwork();
	PipeAdvertisement pipeAdv = null;
	public JXTAServerInterface(){
		
	}
	
	public boolean configure(Object config) {
		boolean result = false;
		
		if (config instanceof JXTAPeerConfiguration){
			this.config = (JXTAPeerConfiguration)config;
			result = true;
		}
		
		if (result){
			result = jxtaNetwork.init(this.config);
		}

		if (result)
		{
			PipeID pipeID = IDFactory.newPipeID(jxtaNetwork.getPeerGroup().getPeerGroupID());
			System.out.println("created pipe: " + pipeID.toString());
			pipeAdv = HGAdvertisementsFactory.newPipeAdvertisement(pipeID, this.config.getPeerName());
			jxtaNetwork.publishAdv(pipeAdv);
			jxtaNetwork.start();
		}

		return result;		
	}

	public void run() {
		
        System.out.println("Starting ServerSocket");
        JxtaServerSocket serverSocket = null;
        
        try {
        	serverSocket = new JxtaServerSocket(jxtaNetwork.getPeerGroup(), pipeAdv);
            serverSocket.setSoTimeout(0);
        } catch (IOException e) {
            System.out.println("failed to create a server socket");
            e.printStackTrace();
        }
        
        //TODO implement a stop method
        while (true) {
            try {
                System.out.println("Waiting for connections");
                Socket socket = serverSocket.accept();
                if (socket != null) {
                    System.out.println("New socket connection accepted");
                    Thread thread = new Thread(new ConnectionHandler(socket), "Connection Handler Thread");
                    thread.start();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
	}

	private class ConnectionHandler implements Runnable{
		private Socket socket;
		
		public ConnectionHandler(Socket socket){
			this.socket = socket;
		}

		private void handleRequest(Socket socket) {
            try {
                InputStream in = socket.getInputStream();
                OutputStream out = socket.getOutputStream();
                Session session = new Session();

                //get the data through the protocol
                Object result = protocol.readMessage(in, session);
                //use protocol to define the response
                protocol.createResponse(out, result, session);	
                out.flush();

                out.close();
                in.close();

                socket.close();
                
                System.out.println("Connection closed");
            } catch (Exception ie) {
                ie.printStackTrace();
            }
        }

		public void run() {
			handleRequest(socket);
		}
	}
}
