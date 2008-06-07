package org.hypergraphdb.peer;

public class PeerConfiguration {
	
	private String cacheDatabaseName;
	
	private boolean hasLocalHGDB;
	private String databaseName;
	
	private boolean hasServerInterface;
	private String serverInterfaceType;
	private Object serverInterfaceConfiguration;
	
	private boolean canForwardRequests;
	private String peerForwarderType;
	private Object peerForwarderConfiguration;
	
	public PeerConfiguration(boolean hasLocalHGDB, String databaseName, 
			boolean hasServerInterface, String serverInterfaceType, Object serverInterfaceConfiguration, 
			boolean canForwardRequests, String peerForwarderType, Object peerForwarderConfiguration,
			String cacheDatabaseName)
	{
		this.hasLocalHGDB = hasLocalHGDB;
		this.databaseName = databaseName;
		
		this.hasServerInterface = hasServerInterface;
		this.serverInterfaceType = serverInterfaceType;
		this.serverInterfaceConfiguration = serverInterfaceConfiguration;
		
		this.canForwardRequests = canForwardRequests;
		this.peerForwarderType = peerForwarderType;
		this.peerForwarderConfiguration = peerForwarderConfiguration;
		
		this.cacheDatabaseName = cacheDatabaseName;
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

	public String getServerInterfaceType() {
		return serverInterfaceType;
	}


	public void setServerInterfaceType(String serverInterfaceType) {
		this.serverInterfaceType = serverInterfaceType;
	}


	public Object getServerInterfaceConfiguration() {
		return serverInterfaceConfiguration;
	}

	public void setServerInterfaceConfiguration(
			Object serverInterfaceConfiguration) {
		this.serverInterfaceConfiguration = serverInterfaceConfiguration;
	}


	public boolean getCanForwardRequests() {
		return canForwardRequests;
	}


	public void setCanForwardRequests(boolean canForwardRequests) {
		this.canForwardRequests = canForwardRequests;
	}


	public String getPeerForwarderType() {
		return peerForwarderType;
	}


	public void setPeerForwarderType(String peerForwarderType) {
		this.peerForwarderType = peerForwarderType;
	}


	public Object getPeerForwarderConfiguration() {
		return peerForwarderConfiguration;
	}


	public void setPeerForwarderConfiguration(Object peerForwarderConfiguration) {
		this.peerForwarderConfiguration = peerForwarderConfiguration;
	}


	public String getCacheDatabaseName()
	{
		return cacheDatabaseName;
	}


	public void setCacheDatabaseName(String cacheDatabaseName)
	{
		this.cacheDatabaseName = cacheDatabaseName;
	}
	
}
