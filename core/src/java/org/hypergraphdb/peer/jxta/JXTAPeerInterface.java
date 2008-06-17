package org.hypergraphdb.peer.jxta;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Set;
import java.util.UUID;

import net.jxta.discovery.DiscoveryEvent;
import net.jxta.discovery.DiscoveryListener;
import net.jxta.document.Advertisement;
import net.jxta.id.IDFactory;
import net.jxta.pipe.PipeID;
import net.jxta.protocol.DiscoveryResponseMsg;
import net.jxta.protocol.PipeAdvertisement;
import net.jxta.socket.JxtaServerSocket;
import net.jxta.socket.JxtaSocket;

import org.apache.servicemix.beanflow.ActivityHelper;
import org.hypergraphdb.peer.PeerInterface;
import org.hypergraphdb.peer.protocol.Message;
import org.hypergraphdb.peer.protocol.OldMessage;
import org.hypergraphdb.peer.protocol.Performative;
import org.hypergraphdb.peer.protocol.Protocol;
import org.hypergraphdb.peer.protocol.Session;
import org.hypergraphdb.peer.workflow.ActivityFactory;
import org.hypergraphdb.peer.workflow.ConversationActivity;
import org.hypergraphdb.peer.workflow.ConversationFactory;
import org.hypergraphdb.peer.workflow.PeerFilterActivity;
import org.hypergraphdb.peer.workflow.ReceiveActivity;
import org.hypergraphdb.util.Pair;

/**
 * @author Cipri Costa
 * 
 * <p>
 * Forwards calls using the JXTA framework.The actual creation of the message is handled by the <code>Protocol</code> class.
 * </p>
 */
public class JXTAPeerInterface implements PeerInterface, DiscoveryListener{
	private JXTAPeerConfiguration config;
	PipeAdvertisement pipeAdv = null;

	/**
	 * used to create the message
	 */
	private Protocol protocol = new Protocol();
	
	private JXTANetwork jxtaNetwork = new DefaultJXTANetwork();

	private HashMap<UUID, ReceiveActivity> receiveActivities = new HashMap<UUID, ReceiveActivity>();
	private HashMap<Pair<Performative, String>, ConversationFactory> conversationFactories = new HashMap<Pair<Performative,String>, ConversationFactory>();
	
	public boolean configure(Object configuration) {
		boolean result = false;
		
		if (configuration instanceof JXTAPeerConfiguration){
			this.config = (JXTAPeerConfiguration)configuration;
			result = true;
		}
		System.out.println("Initializing forwarder manager");
		
		if (result)
		{
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
	
	
	public Object forward(Object peer, OldMessage msg) {
		
		Object result = null;
		/*
    	// TODO actually choose peers to forward to
		Set<Advertisement> advs =  jxtaNetwork.getAdvertisements();
		
		synchronized (advs)
		{
			for(Advertisement adv : advs)
			{
				if (shouldSend(adv, peer))
				{
					try
					{
			            JxtaSocket socket = new JxtaSocket(jxtaNetwork.getPeerGroup(), null, (PipeAdvertisement)adv, 5000, true);
			        	
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
			            
			            if (peer == null)
			            {
			            	//go until first match
			            	if (result != null) break;
			            }else{
			            	//go until first connection to peer succeds
			            	break;
			            }
			            	
					}catch (IOException e) {
			            System.out.println("Communication failure: " + adv.getID());
			            e.printStackTrace();
			        }
				}
			}
			System.out.println("Finished sending");
		}
*/
		return result;
	}

	private boolean shouldSend(Advertisement adv, Object peer)
	{
		//for the time being ... something very simple
		if ((peer != null) && (adv instanceof PipeAdvertisement))
		{
			return peer.toString().equals(((PipeAdvertisement)adv).getName());
		}
		
		return true;
	}


	public void discoveryEvent(DiscoveryEvent ev)
	{
	       DiscoveryResponseMsg res = ev.getResponse();

	        // let's get the responding peer's advertisement
	        System.out.println(" [  Got a Discovery Response [" + res.getResponseCount() + " elements]  from peer : " + ev.getSource() + "  ]");

	        Advertisement adv;
	        Enumeration en = res.getAdvertisements();

	        if (en != null) {
	            while (en.hasMoreElements()) {
	                adv = (Advertisement) en.nextElement();
	                System.out.println(adv);
	            }
	        }
		
	}

	@Override
	public PeerFilterActivity newFilterActivity()
	{
		return new JXTAPeerFilterActivity(jxtaNetwork.getAdvertisements());
	}


	@Override
	public ActivityFactory newSendActivityFactory()
	{
		return new JXTASendActivityFactory(jxtaNetwork.getPeerGroup(), pipeAdv);
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
            	System.out.println("JXTAPeerInterface: connection received");
            	
            	InputStream in = socket.getInputStream();
            	//OutputStream out = socket.getOutputStream();
            	//TODO remove
                Session session = new Session();

                //get the data through the protocol
                Message msg = protocol.readMessage(in, session);
                
                Pair<Performative, String> key = new Pair<Performative, String>(msg.getPerformative(), msg.getAction());
                if (conversationFactories.containsKey(key))
                {
                	ConversationActivity<?> conversation = conversationFactories.get(key).newConversation(JXTAPeerInterface.this);
                	conversation.init();
                	ActivityHelper.start(conversation);
                	conversation.handleMessage(msg);
                }
                
                //use protocol to define the response
                //protocol.createResponse(out, result, session);	
                //out.flush();

                //out.close();
                in.close();

                socket.close();
                
                System.out.println("JXTAPeerInterface: connection closed");
            } catch (Exception ie) {
                ie.printStackTrace();
            }
        }

		public void run() {
			handleRequest(socket);
		}
	}

	public void registerReceiveHook(UUID conversationId, Performative performative, String handleFunc)
	{
		// TODO Auto-generated method stub
		
	}


	public void registerActivity(Performative performative, String action, ConversationFactory convFactory)
	{
		Pair<Performative, String> key = new Pair<Performative, String>(performative, action);
		
		conversationFactories.put(key, convFactory);
	}


}
