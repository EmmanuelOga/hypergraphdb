package org.hypergraphdb.peer.jxta;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Timer;

import javax.activation.MimeType;

import net.jxta.document.Advertisement;
import net.jxta.document.AdvertisementFactory;
import net.jxta.document.Document;
import net.jxta.document.MimeMediaType;
import net.jxta.document.StructuredDocument;
import net.jxta.document.StructuredDocumentFactory;
import net.jxta.document.StructuredDocumentUtils;
import net.jxta.document.TextElement;
import net.jxta.document.XMLDocument;
import net.jxta.document.XMLElement;
import net.jxta.id.IDFactory;
import net.jxta.impl.document.DOMXMLDocument;
import net.jxta.peer.PeerID;
import net.jxta.peergroup.PeerGroup;
import net.jxta.pipe.PipeID;
import net.jxta.pipe.PipeService;
import net.jxta.protocol.PeerAdvertisement;
import net.jxta.protocol.PipeAdvertisement;
import net.jxta.socket.JxtaSocket;

import org.hypergraphdb.peer.protocol.Message;
import org.hypergraphdb.peer.protocol.OldMessage;
import org.hypergraphdb.peer.protocol.Protocol;
import org.hypergraphdb.peer.protocol.Session;
import org.hypergraphdb.peer.workflow.PeerRelatedActivity;
import org.hypergraphdb.util.Pair;

public class JXTASendActivity extends PeerRelatedActivity
{
	private PeerGroup peerGroup;
	private Advertisement pipeAdv;
	private Object result;
	
	public JXTASendActivity(PeerGroup peerGroup, Advertisement pipeAdv)
	{
		this.peerGroup = peerGroup;
		this.pipeAdv = pipeAdv;
	}

	@Override
	public void run()
	{
		try
		{
			PipeAdvertisement targetPipeAdv = null;
			PeerID peerId = null;
			
			if (target instanceof String)
			{
				StructuredDocument doc = StructuredDocumentFactory.newStructuredDocument(MimeMediaType.XMLUTF8, new StringReader(target.toString()));
				targetPipeAdv = (PipeAdvertisement) AdvertisementFactory.newAdvertisement((XMLElement)doc.getRoot());
//				peerId = PeerID.create(new URI("urn:jxta:uuid-59616261646162614E50472050325033C953E85D810F44E9ADC7F848FBDAA0C403"));
			}else{
				Pair<Advertisement, Advertisement> advPair = (Pair<Advertisement, Advertisement>)target;
				
//				peerId = PeerID.create(new URI("urn:jxta:uuid-59616261646162614E50472050325033982C6152F13A4B8AA781E4445BDFAE7103"));
				//peerId =  null;//((PeerAdvertisement)advPair.getFirst()).getPeerID();
				targetPipeAdv = (PipeAdvertisement)advPair.getSecond();
			}
			
			System.out.println("Sending " + msg.toString() + " to adv: " + targetPipeAdv.toString());
			
 			JxtaSocket socket = new JxtaSocket(peerGroup, peerId, targetPipeAdv, 5000, true);

	        OutputStream out = socket.getOutputStream();
	        InputStream in = socket.getInputStream();
	        Session session = new Session();
	        
	        Protocol protocol = new Protocol();
	        //send message
	        Message msg = getMessage();
	        if (msg.getReplyTo() == null)
	        {
	        	msg.setReplyTo(pipeAdv.toString());
	        }
	        protocol.writeMessage(out, getMessage(), session);
	        out.flush();
	        System.out.println("Client sent a command");

	        //receive answer
	        result = null;//protocol.handleResponse(in, session);
		} catch (IOException e)
		{
			e.printStackTrace();
			//fail("IOException", e);
		} 
	}

	@Override
	public void startWithTimeout(Timer arg0, long arg1)
	{
		// TODO Auto-generated method stub
		
	}

}
