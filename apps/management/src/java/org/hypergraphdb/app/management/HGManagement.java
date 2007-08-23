package org.hypergraphdb.app.management;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.HGQuery.hg;

/**
 * <p>
 * This class implements top-level management operations as static methods.
 * </p>
 *  
 * @author Borislav Iordanov
 */
public class HGManagement 
{
	public static boolean isInstalled(HyperGraph graph, HGApplication app)
	{		
		return hg.findOne(graph, hg.and(hg.type(app.getClass()), hg.eq("name", app.getName()))) != null;
	}
	
	public static void ensureInstalled(HyperGraph graph, HGApplication app)
	{
		if (!isInstalled(graph, app))
		{
			app.install(graph);
			try { graph.add(app); }
			catch (Exception ex) { app.uninstall(graph); }
		}
	}	
	
	public static void remove(HyperGraph graph, HGApplication app)
	{
		HGHandle h = hg.findOne(graph, hg.and(hg.type(app.getClass()), hg.eq("name", app.getName())));
		if (h != null)
		{
			app.uninstall(graph);
			graph.remove(h);
		}
	}
}