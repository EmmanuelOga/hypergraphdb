package org.hypergraphdb.app.wordnet;

import java.util.Date;

import org.hypergraphdb.HGGraphHolder;
import org.hypergraphdb.HyperGraph;

/**
 * 
 * <p>
 * Represents some global WordNet statistic that can be calculated and saved in the
 * HyperGraphDB instance. Global statistics usually take time to calculate and they
 * are stable for a given WordNet version/dataset. A single <code>WNStat</code>
 * implementation holds the value calculated and implements the actual algorithm
 * to do the calculation. The resulting value of the statistic can be anything
 * storable in a HyperGraphDB (from a simple number to a complex structure that
 * the type system can handle). 
 * </p>
 * 
 * <p>
 * <code>WNStat</code> are singletons automatically stored in the database. To get
 * the instance of a specific <code>WNStat</code> call the {@link WNGraph#getStatistic(Class)}
 * method.
 * </p>
 * 
 * <p>
 * Because the type of the result value is arbitrary (modulo storability in HGDB), this
 * class can in fact by used to manage any sort of precomputed values on the WordNet database.
 * But we keep the name <code>WNStat</code> because it reflects the main intent.
 * </p>
 * 
 * <p>
 * Note: implementations should be default constructible.
 * </p>
 * 
 * @author Borislav Iordanov
 *
 */
public abstract class WNStat<T> implements HGGraphHolder
{
	private Date calculationTimestamp = null;
	private T value = null;
	protected HyperGraph graph = null;
	
	/**
	 * <p>
	 * Implement this method to perform the actual calculation of this
	 * statistic and return its value.
	 * </p>
	 */
	protected abstract T doCalculation();

	/**
	 * <p>Perform a calculation of this statistic, regardless of whether it
	 * already has a value or not. Then update its value as well as the time stamp
	 * of the calculation in the HyperGraphDB storage and return the value.
	 * </p>
	 *   
	 * @return The value of the statistic.
	 */
	public T calculate()
	{
		value = doCalculation();
		calculationTimestamp = new Date();
		graph.update(this);
		return value;
	}
	
	public boolean isCalculated() 
	{ 
		return value != null; 
	}
	
	public T getValue()
	{
		return value;
	}
	
	public void setValue(T value)
	{
		this.value = value;
	}

	public Date getCalculationTimestamp()
	{
		return calculationTimestamp;
	}

	public void setCalculationTimestamp(Date calculationTimestamp)
	{
		this.calculationTimestamp = calculationTimestamp;
	}

	public HyperGraph getHyperGraph()
	{
		return graph;
	}

	public void setHyperGraph(HyperGraph graph)
	{
		this.graph = graph;
	}	
}