package org.hypergraphdb.peer.jxta;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Timer;

import net.jxta.peergroup.PeerGroup;
import net.jxta.protocol.PipeAdvertisement;
import net.jxta.socket.JxtaSocket;

import org.hypergraphdb.peer.protocol.Message;
import org.hypergraphdb.peer.protocol.Protocol;
import org.hypergraphdb.peer.protocol.Session;
import org.hypergraphdb.peer.workflow.SendActivity;

public class JXTASendActivity extends SendActivity
{
	private PeerGroup peerGroup;
	private Object result;
	
	public JXTASendActivity(PeerGroup peerGroup)
	{
		this.peerGroup = peerGroup;
	}

	@Override
	public void run()
	{
		try
		{
			JxtaSocket socket = new JxtaSocket(peerGroup, null, (PipeAdvertisement)target, 5000, true);

	        OutputStream out = socket.getOutputStream();
	        InputStream in = socket.getInputStream();
	        Session session = new Session();
	        
	        Protocol protocol = new Protocol();
	        //send message
	        protocol.createRequest(out, getMessage(), session);
	        out.flush();
	        System.out.println("Client sent a command");

	        //receive answer
	        result = protocol.handleResponse(in, session);
		} catch (IOException e)
		{
			fail("IOException", e);
			e.printStackTrace();
		}
	}

	@Override
	public void startWithTimeout(Timer arg0, long arg1)
	{
		// TODO Auto-generated method stub
		
	}

}
