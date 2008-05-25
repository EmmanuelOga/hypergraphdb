package org.hypergraphdb.peer.protocol;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;

import org.hypergraphdb.HGPersistentHandle;
import org.hypergraphdb.handle.UUIDPersistentHandle;

/**
 * @author Cipri Costa
 *
 * <p>
 * The root of all the serialization/deserialization mechanism. Probably going to be replaced when the mechanism is defined.
 * </p>
 */
public class ObjectSerializer {

	public ObjectSerializer(){
	}
	
	public void serialize(OutputStream out, Object data) throws IOException{
		if (data instanceof Serializable){

			out.write(0);
			ObjectOutputStream objStream = new ObjectOutputStream(out);
			objStream.writeObject(data);
		}else{
			//TODO this is just temporary
			if (data instanceof HGPersistentHandle){
				//this is suppose to mark next as special ...
				out.write(1);
				out.write(((HGPersistentHandle)data).toByteArray());
			}			
		}
	}
	
	public Object deserialize(InputStream in) throws IOException{
		int type = in.read();
		Object result = null;
		
		if (type == 0){
			ObjectInputStream objStream = null;

			objStream = new ObjectInputStream(in);
			try {
				result = objStream.readObject();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}else if (type == 1){
			byte[] data = new byte[16];
			if (in.read(data) == data.length){
				result = UUIDPersistentHandle.makeHandle(data);
			}
		}

		return result;
	}

}
