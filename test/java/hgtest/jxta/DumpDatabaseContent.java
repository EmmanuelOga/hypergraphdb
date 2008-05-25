package hgtest.jxta;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGSearchResult;
import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.query.AnyAtomCondition;

public class DumpDatabaseContent {
	public static void main(String[] args){
		
		HyperGraph hg = new HyperGraph("./TestDB");
		
		HGSearchResult rs = hg.find(new AnyAtomCondition());

		while (rs.hasNext()) 
		{ 
			HGHandle handle = (HGHandle)rs.next();
			Object value = hg.get(handle); 
			System.out.println("Found: " + handle.toString() + " -> " + value.toString());
		}
		rs.close();

	}
}
