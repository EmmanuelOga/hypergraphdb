package org.hypergraphdb.peer.jxta;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.UUID;

import net.jxta.discovery.DiscoveryEvent;
import net.jxta.discovery.DiscoveryListener;
import net.jxta.document.Advertisement;
import net.jxta.id.IDFactory;
import net.jxta.pipe.PipeID;
import net.jxta.protocol.DiscoveryResponseMsg;
import net.jxta.protocol.PipeAdvertisement;
import net.jxta.socket.JxtaServerSocket;

import org.hypergraphdb.peer.PeerFilter;
import org.hypergraphdb.peer.PeerInterface;
import org.hypergraphdb.peer.PeerRelatedActivity;
import org.hypergraphdb.peer.PeerRelatedActivityFactory;
import org.hypergraphdb.peer.protocol.Message;
import org.hypergraphdb.peer.protocol.Performative;
import org.hypergraphdb.peer.protocol.Protocol;
import org.hypergraphdb.peer.workflow.TaskActivity;
import org.hypergraphdb.peer.workflow.TaskFactory;
import org.hypergraphdb.util.Pair;



/**
 * @author Cipri Costa
 *
 * Implements the PeerInterface interface and manages the communication with the other peers int he JXTA network.
 * Also manages resources like task allocation and threads.
 */

public class JXTAPeerInterface implements PeerInterface/*, DiscoveryListener*/{
	private JXTAPeerConfiguration config;
	PipeAdvertisement pipeAdv = null;
	
	/**
	 * used to create the message
	 */
	private Protocol protocol = new Protocol();
	
	private JXTANetwork jxtaNetwork = new DefaultJXTANetwork();

	private HashMap<Pair<Performative, String>, TaskFactory> taskFactories = new HashMap<Pair<Performative,String>, TaskFactory>();
	private HashMap<UUID, TaskActivity<?>> tasks = new HashMap<UUID, TaskActivity<?>>();
	
	public boolean configure(Object configuration) 
	{
		boolean result = false;
		
		System.out.println("JXTAPeerInterface: configure");

		if (configuration instanceof JXTAPeerConfiguration){
			this.config = (JXTAPeerConfiguration)configuration;
			result = true;
		}		
		
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

	public PeerFilter newFilterActivity()
	{
		return new JXTAPeerFilter(jxtaNetwork.getAdvertisements());
	}


	public PeerRelatedActivityFactory newSendActivityFactory()
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

                //get the data through the protocol
                Message msg = protocol.readMessage(in);
                System.out.println("received: " + msg.toString());
                if (tasks.containsKey(msg.getTaskId()))
                {
                	tasks.get(msg.getTaskId()).handleMessage(msg);
                }else{
	                Pair<Performative, String> key = new Pair<Performative, String>(msg.getPerformative(), msg.getAction());
	                if (taskFactories.containsKey(key))
	                {
	                	TaskActivity<?> task = taskFactories.get(key).newTask(JXTAPeerInterface.this, msg);

	                	new Thread(task).start();
	                }
                }
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

	public void registerTask(UUID taskId, TaskActivity<?> task)
	{
		tasks.put(taskId, task);
	}


	public void execute(PeerRelatedActivity activity)
	{
		activity.run();
//		new Thread(activity).start();
	
	}


	public void registerTaskFactory(Performative performative, String action, TaskFactory taskFactory)
	{
		Pair<Performative, String> key = new Pair<Performative, String>(performative, action);
		
		taskFactories.put(key, taskFactory);
		
	}


}
