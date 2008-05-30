package org.hypergraphdb.peer.serializer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


public class StringSerializer extends PrimitiveTypeSerializer {

	@Override
	public Object deserialize(InputStream in) {
		return deserializeString(in);
	}

	@Override
	public void serialize(OutputStream out, Object data) {
		if ((data == null) || (data instanceof String)){
			IntSerializer.serializeInt(out, SerializerManager.STRING_SERIALIZER_ID);
			serializeString(out, (String)data);
		}
	}

	public static String deserializeString(InputStream in) {
		try {
			int length = IntSerializer.deserializeInt(in);
			
			if (length < 0){
				return null;
			}else{
				byte[] data = new byte[length];
				in.read(data);
				return new String(data);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}

	public static void serializeString(OutputStream out, String data) {
		try {
			if (data == null){
				out.write(-1);
			}else{
				byte[] dataBytes = data.getBytes();
				IntSerializer.serializeInt(out, dataBytes.length);
				out.write(dataBytes);
	
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
}
