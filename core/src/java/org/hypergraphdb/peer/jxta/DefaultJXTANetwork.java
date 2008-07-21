package org.hypergraphdb.peer.jxta;

import java.io.File;
import java.net.URI;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.jxta.credential.AuthenticationCredential;
import net.jxta.discovery.DiscoveryEvent;
import net.jxta.discovery.DiscoveryListener;
import net.jxta.discovery.DiscoveryService;
import net.jxta.document.Advertisement;
import net.jxta.endpoint.EndpointService;
import net.jxta.endpoint.MessageTransport;
import net.jxta.exception.PeerGroupException;
import net.jxta.id.IDFactory;
import net.jxta.impl.endpoint.relay.RelayClient;
import net.jxta.membership.Authenticator;
import net.jxta.peer.PeerID;
import net.jxta.peergroup.PeerGroup;
import net.jxta.peergroup.PeerGroupID;
import net.jxta.pipe.PipeID;
import net.jxta.platform.NetworkConfigurator;
import net.jxta.platform.NetworkManager;
import net.jxta.protocol.AccessPointAdvertisement;
import net.jxta.protocol.DiscoveryResponseMsg;
import net.jxta.protocol.ModuleImplAdvertisement;
import net.jxta.protocol.PeerAdvertisement;
import net.jxta.protocol.PeerGroupAdvertisement;
import net.jxta.protocol.PipeAdvertisement;

import org.hypergraphdb.query.HGAtomPredicate;
import org.hypergraphdb.util.Pair;

/**
 * @author Cipri Costa
 *
 * <p>
 * Handles problems related to the JXTA network : intialize, stop, discovery, publishing, etc
 * </p>
 */
public class DefaultJXTANetwork implements JXTANetwork{
	private static int RDV_WAIT_TIMEOUT = 3000;
	
	private static NetworkManager peerManager = null;
	private static PeerGroup netPeerGroup = null;
	private static PeerGroup hgdbGroup = null;
	
	private LinkedList<Advertisement> ownAdvs = new LinkedList<Advertisement>();
	private Map<Advertisement, HGAtomPredicate> peerAdvs = Collections.synchronizedMap(new HashMap<Advertisement, HGAtomPredicate>());
	private Map<Advertisement, String> peerAdvIds = new HashMap<Advertisement, String>();
	private Set<PipeID> ownPipes = new HashSet<PipeID>();
	
	private int advTimetoLive;
	
	public boolean init(JXTAPeerConfiguration config)
	{
		
		//Start network
	    try 
	    {
	    	System.out.println("Initializing instance " + config.getPeerName() + " ...");   	
	    	URI configURI = new File(new File(".jxta"), config.getPeerName()).toURI();
	    	
	    	System.out.println("Using config file: " + configURI.toString());
	    	peerManager = new NetworkManager(NetworkManager.ConfigMode.EDGE, config.getPeerName(), configURI);
	    	
	    	dumpNetworkConfig(peerManager.getConfigurator());
	    	
	    	peerManager.startNetwork();
	    } catch (Exception e) {
	    	e.printStackTrace();
	    }
	    
	    //Join custom group
	    if (peerManager != null)
	    {
	    	netPeerGroup = peerManager.getNetPeerGroup();
	    	try
			{
				joinCustomGroup(config);
			} catch (Exception e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    }
	    
	    
	    //wait for rendezvous if needed
	    System.out.println("Waiting for rendezvous: " + config.getNeedsRdvConn());
	    if (config.getNeedsRdvConn())
    	{
    		boolean rdvFound = false;
    		while (!rdvFound)
    		{
	    		System.out.println("start waiting for rendezvous...");
	    		rdvFound = peerManager.waitForRendezvousConnection(3000);
    			
	    		if (rdvFound) System.out.println("Rendevous found!");
	    		else System.out.println("No rendevous found...");
    		}
    	}

	    //wait for realy
	    System.out.println("Waiting for realy: " + config.getNeedsRelayConn());
    	if (config.getNeedsRelayConn())
    	{
    		boolean relayFound = false;
    		while (!relayFound)
    		{
    			System.out.println("start waiting for relays...");
    			relayFound = waitForRelay();
    			
	    		if (relayFound) System.out.println("Relay found!");
	    		else 
	    		{
	    			System.out.println("No relays found...");
	    			
	    			try
					{
						Thread.sleep(3000);
					} catch (InterruptedException e)
					{
					}
	    		}
    		}
    	}
	    	
	   this.advTimetoLive = config.getAdvTimeToLive();
	    			
		System.out.println("Finished initializing");

		return (netPeerGroup != null);
	}
	
	private boolean waitForRelay()
	{
		boolean hasRelay = false;
		
        EndpointService endpoint = netPeerGroup.getEndpointService();

        Iterator it = endpoint.getAllMessageTransports();

        while (it.hasNext()) {
            MessageTransport mt = (MessageTransport) it.next();

            try {
                if (mt instanceof RelayClient) {
                    RelayClient er = (RelayClient) mt;
                    System.out.println("Active Relay Servers for '" + mt.getProtocolName() + "' : ");

                    List allRelays = er.getActiveRelays(null);

                    if ((null == allRelays) || allRelays.isEmpty()) {
                    	System.out.println("\t(none)");
                    } else {
                    	hasRelay = true;
                        for (Object allRelay : allRelays) {
                            AccessPointAdvertisement ap = (AccessPointAdvertisement) allRelay;
                            System.out.println("\t" + getPeerName(mt, ap) + " [" + ap.getPeerID() + "]");
                        }
                    }
                }
            } catch (Exception ex) 
            {
                ex.printStackTrace();
            }
        }
        
        return hasRelay;
	}

    private String getPeerName(MessageTransport mt, AccessPointAdvertisement adv) {

        EndpointService endpoint = mt.getEndpointService();
        DiscoveryService discovery = endpoint.getGroup().getDiscoveryService();

        PeerID id = adv.getPeerID();
        String res = id.toString();

        Enumeration advs;

        try {
            advs = discovery.getLocalAdvertisements(DiscoveryService.PEER, "PID", id.toString());
        } catch (Exception ez) {
            return res;
        }

        while (advs.hasMoreElements()) {
            try {
                PeerAdvertisement padv = (PeerAdvertisement) advs.nextElement();
                return padv.getName();
            } catch (Exception ez) {
                System.out.println("failed with " + ez);

            }
        }

        return res;
    }

	private void joinCustomGroup(JXTAPeerConfiguration config) throws Exception
	{
		System.out.println("Joining group " + config.getPeerGroupName());
		PeerGroupID groupId = IDFactory.newPeerGroupID(netPeerGroup.getPeerGroupID(), config.getPeerGroupName().getBytes());
		
		//try to find it, if not, publish it
		Enumeration<Advertisement> advs;
		
		advs = netPeerGroup.getDiscoveryService().getLocalAdvertisements(DiscoveryService.GROUP, "GID", groupId.toString());
		if (advs != null)
		{
			if (advs.hasMoreElements())
			{
				Advertisement adv = advs.nextElement();
				if (adv != null)
				{
					System.out.println("Found local group advertisement... ");
					hgdbGroup = netPeerGroup.newGroup(adv);				
				}
			}
		}

		if (hgdbGroup == null)
		{
			DiscoveryListener listener = new DiscoveryListener()
			{
				public void discoveryEvent(DiscoveryEvent evnt)
				{
					Advertisement adv = evnt.getSearchResults().nextElement();
					
					System.out.println("Remote group: " + adv);
					System.out.println("Found remote group advertisement... ");
					try
					{
						hgdbGroup = netPeerGroup.newGroup(adv);
					} catch (PeerGroupException e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}		
				}
			};
			
			netPeerGroup.getDiscoveryService().getRemoteAdvertisements(null, DiscoveryService.GROUP, null, null, 100, listener);
			
			try
			{
				Thread.sleep(3000);
			}catch(InterruptedException ex){}
	
			netPeerGroup.getDiscoveryService().removeDiscoveryListener(listener);
		}
		
		if (hgdbGroup == null)
		{
			System.out.println("Can not find local group advertisements. Creating a new group ... ");

			ModuleImplAdvertisement implAdv = netPeerGroup.getAllPurposePeerGroupImplAdvertisement();
			hgdbGroup = netPeerGroup.newGroup(groupId, implAdv, config.getPeerGroupName(), "");
			

			System.out.println("publishing group...");
			
			PeerGroupAdvertisement adv = hgdbGroup.getPeerGroupAdvertisement();
			netPeerGroup.getDiscoveryService().remotePublish(adv);
			
			System.out.println("Group published successfully.");
		}
		
		System.out.println("Relay id: " + hgdbGroup.relayProtoClassID);
		System.out.println("Group adv:" + hgdbGroup.getImplAdvertisement());
		System.out.println("NetGroup adv:" + netPeerGroup.getImplAdvertisement());
		System.out.println("Joining group...");
		AuthenticationCredential cred = new AuthenticationCredential(hgdbGroup, null, null);
		Authenticator auth = hgdbGroup.getMembershipService().apply(cred);
	
		if (auth != null)
		{
			if (auth.isReadyForJoin())
			{
				hgdbGroup.getMembershipService().join(auth);
				System.out.println("group joined");
			}
		}
	}

	public void start()
	{
    	new Thread(new AdvPublisher()).start();
    	new Thread(new AdvSubscriber()).start();
	}
	
	public PeerGroup getPeerGroup()
	{
		return netPeerGroup;
	}
	
	public void publishAdv(Advertisement adv)
	{
		synchronized(this)
		{
			ownAdvs.addLast(adv);
		}
		netPeerGroup.getDiscoveryService().remotePublish(adv);
	}
	public Advertisement getPipeAdv()
	{
		return ownAdvs.getFirst();
	}
	public Set<Advertisement> getAdvertisements()
	{
		return peerAdvs.keySet();
	}
	private class AdvPublisher implements Runnable
	{
		public void run()
		{
	        DiscoveryService discoveryService = hgdbGroup.getDiscoveryService();

	        long expiration = 5 * 1000;//DiscoveryService.DEFAULT_EXPIRATION;
	        long waittime = 5 * 1000;//DiscoveryService.DEFAULT_EXPIRATION;
	        
	        try {
	            while (true) {
	            	for(Advertisement adv : ownAdvs)
	            	{
	            		//System.out.println("publishing: " + adv.getID().toString());
	            		
	            		discoveryService.publish(adv, advTimetoLive, expiration);
	            		discoveryService.remotePublish(adv, expiration);
	            	}
	                try {
	                    Thread.sleep(waittime);
	                } catch (Exception e) {
	                }
	            }
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
		}
	}
	
	private class AdvSubscriber implements Runnable, DiscoveryListener
	{

		public void run()
		{
	        long waittime = 1000L;
	        DiscoveryService discoveryService = hgdbGroup.getDiscoveryService();

	        try {
	        	Enumeration<Advertisement> localAdvs = discoveryService.getLocalAdvertisements(DiscoveryService.ADV, null, null);
	        	loadAdvs(localAdvs, "local");
	        	
	        	// Add ourselves as a DiscoveryListener for DiscoveryResponse events
	        	discoveryService.addDiscoveryListener(this);
	        	
	        	while (true)
	        	{
	        		//System.out.println("Getting remote advertisements");
		        	discoveryService.getRemoteAdvertisements(null,  DiscoveryService.ADV,  null, null, 100, null);
	                try {
	                    Thread.sleep(waittime);
	                } catch (Exception e) {
	                    // ignored
	                }

		        	
	        	}
	        } catch (Exception e) {
	            e.printStackTrace();
	        }

		}

		public void discoveryEvent(DiscoveryEvent ev)
		{
	        DiscoveryResponseMsg res = ev.getResponse();

	        // let's get the responding peer's advertisement
	        String peerName = ev.getSource().toString();
	        	        
	        //System.out.println(" [  Got a Discovery Response [" + res.getResponseCount() + " elements]  from peer : " + peerName + "  ]");
	        /*PeerAdvertisement peerAdv = res.getPeerAdvertisement();
	       */	        
	        //not interested in selfs advertisements

	        Enumeration<Advertisement> advs = res.getAdvertisements();

	        loadAdvs(advs, peerName);
		}
		
		private void loadAdvs(Enumeration<Advertisement> advs, String peerName)
		{
	        Advertisement adv;
	        
	        if (advs != null) {
	            while (advs.hasMoreElements()) {
	                adv = (Advertisement) advs.nextElement();
	                if (adv instanceof PipeAdvertisement)
	                {
	                	if (!peerAdvs.containsKey(adv))
	                	{
	                		PipeID pipeId = (PipeID) ((PipeAdvertisement)adv).getPipeID();
	                		
	                		if (!ownPipes.contains(pipeId))
	                		{
		                		peerAdvs.put(adv, null);
		                		peerAdvIds.put(adv, peerName);
		                		System.out.println("New Pipe from " + peerName + " (" + pipeId + ")");	                			
	                		}
	                		
	                	}
	                }
	            }
	        }

		}
		
	}
	public boolean hasRemotePipes()
	{
		return !peerAdvs.isEmpty();
	}
	private void dumpNetworkConfig(NetworkConfigurator configurator)
	{
		System.out.println("Configuration: ");
		System.out.println("	PeerID = " + configurator.getPeerID().toString());
		System.out.println("	Name = " + configurator.getName());
		
		//System.out.println(configurator.getPlatformConfig().toString());
		
	}

	public void addOwnPipe(PipeID pipeId)
	{
		ownPipes.add(pipeId);
	}

	public HGAtomPredicate getAtomInterests(Object peerId)
	{
		return peerAdvs.get(peerId);
	}

	public void setAtomInterests(Object peerId, HGAtomPredicate interest)
	{
		System.out.println("Peer " + ((PipeAdvertisement)peerId).getName() + " is interested in " + interest);
		peerAdvs.put((Advertisement)peerId, interest);
	}



	@Override
	public Object getPeerId(Object peer)
	{
		return peerAdvIds.get(peer);
	}

	public void waitForRemotePipe()
	{
		while (peerAdvs.isEmpty())
		{
			System.out.println("DefaultJXTANetwork: waiting for remote pipe advertisement");
			try
			{
				Thread.sleep(1000);
			} catch (InterruptedException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
