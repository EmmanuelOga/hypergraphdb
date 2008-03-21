package org.hypergraphdb.app.wordnet;

import org.hypergraphdb.*;

public class SemTools
{
	private HyperGraph graph;
	
	public SemTools(HyperGraph graph)
	{
		this.graph = graph;		 
	}
	
	public double getInformationContent(HGHandle synset)
	{
		return 0;
	}
	
	public HGHandle getLeastCommonSubsumer(HGHandle synset1, HGHandle synset2)
	{
		return null;
	}
	
	public double getPathSimilarity(HGHandle s1, HGHandle s2)
	{
		return 0.0;
	}
	
	public double getWuPalmerSimilarity(HGHandle s1, HGHandle s2)
	{
		return 0.0;
	}
	
	public double getLeacockChodorowSimilarity(HGHandle s1, HGHandle s2)
	{
		return 0.0;
	}
}