package hgtest.jxta;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGSearchResult;
import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.handle.UUIDPersistentHandle;
import org.hypergraphdb.query.AnyAtomCondition;
import org.hypergraphdb.query.AtomTypeCondition;
import org.hypergraphdb.query.HGQueryCondition;

public class DumpDatabaseContent {
	public static void main(String[] args){
		dumpDb("./DBs/Client1CacheDB", new AtomTypeCondition(User.class));
		//dumpDb("./DBs/Server1CacheDb", new AnyAtomCondition());

		dumpDb("./DBs/Server1DB", new AtomTypeCondition(User.class));
	}

	
	private static void dumpDb(String db, HGQueryCondition cond)
	{
		System.out.println("Content of: " + db);
		HyperGraph hg = new HyperGraph(db);
		
		HGSearchResult<HGHandle> rs = hg.find(cond);

		while (rs.hasNext()) 
		{ 
			HGHandle handle = rs.next();
			Object value = hg.get(handle); 
			System.out.println("Found: " + handle.toString() + " -> " + value.toString());
		}
		rs.close();

		hg.close();
	}
}
