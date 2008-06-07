package org.hypergraphdb.peer;

import java.io.InputStream;
import java.io.OutputStream;

public interface Serializer
{
	void writeToStream(OutputStream out);
	Object readFromStream(InputStream in);
}
