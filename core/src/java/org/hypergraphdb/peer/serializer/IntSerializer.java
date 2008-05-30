package org.hypergraphdb.peer.serializer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


public class IntSerializer extends PrimitiveTypeSerializer {

	@Override
	public Object deserialize(InputStream in) {
		return deserializeInt(in);
	}

	@Override
	public void serialize(OutputStream out, Object data) {
		if ((data == null) || (data instanceof Integer)){
			serializeInt(out, SerializerManager.INT_SERIALIZER_ID);
			serializeInt(out, (Integer) data);
		}
	}
	
	public static Integer deserializeInt(InputStream in){
		try{
			int nullByte = in.read();
			if (nullByte ==  0) return null;
			else{
		        int ch1 = in.read();
		        int ch2 = in.read();
		        int ch3 = in.read();
		        int ch4 = in.read();
		        int i = ((ch1 & 0xFF) << 24) | ((ch2 & 0xFF) << 16) | ((ch3 & 0xFF) << 8) | (ch4 & 0xFF);
		        
		        return new Integer(i);
			}
		}catch(IOException ex){
			ex.printStackTrace();
		}
		return null;
	}
	
	public static void serializeInt(OutputStream out, Integer data) {
		try{
	
			if (data == null){
				out.write(0);
			}else{
				out.write(1);	
				
	        	int v = ((Integer) data).intValue();
	        	out.write((byte)((v >>> 24) & 0xFF)); 
	        	out.write((byte)((v >>> 16) & 0xFF));
	        	out.write((byte)((v >>> 8) & 0xFF)); 
	        	out.write((byte)((v >>> 0) & 0xFF));
	    	}
		}catch(IOException ex){
			ex.printStackTrace();
		}

	}
}
