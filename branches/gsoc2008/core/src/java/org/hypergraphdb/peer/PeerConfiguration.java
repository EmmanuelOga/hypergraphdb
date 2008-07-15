package org.hypergraphdb.peer;

/**
 * @author ciprian.costa
 *
 */
/**
 * @author ciprian.costa
 *
 */
public class PeerConfiguration {
	
	private String cacheDatabaseName;
	
	private boolean hasLocalHGDB;
	private String databaseName;
	
	private boolean hasServerInterface;
	private boolean canForwardRequests;

	private String peerInterfaceType;
	private Object peerInterfaceConfiguration;
	
	private boolean waitForRemotePipe;
	
	public PeerConfiguration(boolean hasLocalHGDB, String databaseName, 
			boolean hasServerInterface, boolean canForwardRequests, 
			String peerInterfaceType, Object peerInterfaceConfiguration,
			String cacheDatabaseName)
	{
		this.hasLocalHGDB = hasLocalHGDB;
		this.databaseName = databaseName;
		
		this.hasServerInterface = hasServerInterface;
		
		this.canForwardRequests = canForwardRequests;
		this.peerInterfaceType = peerInterfaceType;
		this.peerInterfaceConfiguration = peerInterfaceConfiguration;
		
		this.cacheDatabaseName = cacheDatabaseName;
		
		this.waitForRemotePipe = false;
	}

	
	public boolean getHasLocalHGDB() {
		return hasLocalHGDB;
	}

	public void setHasLocalHGDB(boolean hasLocalHGDB) {
		this.hasLocalHGDB = hasLocalHGDB;
	}

	public String getDatabaseName() {
		return databaseName;
	}

	public void setDatabaseName(String databaseName) {
		this.databaseName = databaseName;
	}

	public boolean getHasServerInterface() {
		return hasServerInterface;
	}

	public void setHasServerInterface(boolean hasServerInterface) {
		this.hasServerInterface = hasServerInterface;
	}

	public boolean getCanForwardRequests() {
		return canForwardRequests;
	}


	public void setCanForwardRequests(boolean canForwardRequests) {
		this.canForwardRequests = canForwardRequests;
	}


	public String getPeerInterfaceType() {
		return peerInterfaceType;
	}


	public void setPeerInterfaceType(String peerForwarderType) {
		this.peerInterfaceType = peerForwarderType;
	}


	public Object getPeerInterfaceConfiguration() {
		return peerInterfaceConfiguration;
	}


	public void setPeerInterfaceConfiguration(Object peerForwarderConfiguration) {
		this.peerInterfaceConfiguration = peerForwarderConfiguration;
	}


	public String getCacheDatabaseName()
	{
		return cacheDatabaseName;
	}


	public void setCacheDatabaseName(String cacheDatabaseName)
	{
		this.cacheDatabaseName = cacheDatabaseName;
	}


	public boolean getWaitForRemotePipe()
	{
		return waitForRemotePipe;
	}


	public void setWaitForRemotePipe(boolean waitForRemotePipe)
	{
		this.waitForRemotePipe = waitForRemotePipe;
	}


}
