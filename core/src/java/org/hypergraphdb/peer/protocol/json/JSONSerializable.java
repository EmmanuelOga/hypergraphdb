package org.hypergraphdb.peer.protocol.json;

import org.json.JSONObject;

/**
 * @author ciprian.costa
 * One way to embedd objects into messages. Implementors just need to provide a way to get a JSON object with their data.
 */

public interface JSONSerializable
{
	JSONObject getData();
}
