package org.hypergraphdb.peer.serializer;

import java.io.InputStream;
import java.io.OutputStream;


public interface HGSerializer {
	public void writeData(OutputStream out, Object data, ObjectPool objectPool);
	public Object readData(InputStream in, ObjectPool objectPool);
}
