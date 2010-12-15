/* 
 * This file is part of the HyperGraphDB source distribution. This is copyrighted 
 * software. For permitted uses, licensing options and redistribution, please see  
 * the LicensingInformation file at the root level of the distribution.  
 * 
 * Copyright (c) 2005-2010 Kobrix Software, Inc.  All rights reserved. 
 */
package org.hypergraphdb.peer.protocol;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

public class ProtocolUtils {

	public static boolean verifySignature(InputStream in, byte[] signature) 
	{
		byte[] streamData = new byte[signature.length];		
		try
		{
			int count = 0;
			for (int read = 0; read > -1 && count < streamData.length; )
			{
				count += read;
				read = in.read(streamData, count, streamData.length - count);
			}
			return count == streamData.length && Arrays.equals(streamData, signature);
		}
		catch(IOException ex)
		{
			ex.printStackTrace();
		}		
		return false;
	}
	
	public static void writeSignature(OutputStream out, byte[] signature)
	{
		try
		{
			out.write(signature);
		}
		catch(IOException ex)
		{
			ex.printStackTrace();
		}
	}
}
