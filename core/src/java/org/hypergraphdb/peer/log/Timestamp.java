package org.hypergraphdb.peer.log;

public class Timestamp
{
	private int counter;
	
	public Timestamp()
	{
		counter = 0;
	}

	public Timestamp(int counter)
	{
		this.counter = counter;
	}

	public Timestamp moveNext()
	{
		Timestamp result = new Timestamp(counter);

		counter++;
		return result;
	}
	
	public String toString()
	{
		return "time = " + ((Integer)counter).toString();
	}

	public int getCounter()
	{
		return counter;
	}

	public void setCounter(int counter)
	{
		this.counter = counter;
	}
	
	public Timestamp clone()
	{
		return new Timestamp(counter);
	}
	
}
