package hgtest.atomref;

import hgtest.SimpleBean;

import org.hypergraphdb.annotation.AtomReference;

public class SimpleBeanContainer2
{
	@AtomReference("symbolic")
	private SimpleBean x;

	public SimpleBean getX()
	{
		return x;
	}

	public void setX(SimpleBean x)
	{
		this.x = x;
	}	
}
