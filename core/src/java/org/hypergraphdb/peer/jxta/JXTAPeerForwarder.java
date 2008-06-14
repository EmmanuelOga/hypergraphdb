package org.hypergraphdb.peer.jxta;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Set;

import net.jxta.discovery.DiscoveryEvent;
import net.jxta.discovery.DiscoveryListener;
import net.jxta.document.Advertisement;
import net.jxta.protocol.DiscoveryResponseMsg;
import net.jxta.protocol.PipeAdvertisement;
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
public class JXTAPeerForwarder implements PeerForwarder, DiscoveryListener{
	private JXTAPeerConfiguration config;
	/**
	 * used to create the message
	 */
	private Protocol protocol = new Protocol();
	
	private JXTANetwork jxtaNetwork = new DefaultJXTANetwork();
	
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
		
		if(result)
		{
			jxtaNetwork.start();
		}
		return result;		
	}
	
	
	public Object forward(Object peer, Message msg) {
		
		Object result = null;
		
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

}
