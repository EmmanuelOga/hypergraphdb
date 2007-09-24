	package org.hypergraphdb.app.tm;

import java.net.URI;

import org.tmapi.core.Locator;

public class URILocator implements Locator
{
	private URI uri = null;
	
	public URILocator()
	{		
	}
	
	public URILocator(String uri)
	{
		try { this.uri = new URI(uri); }
		catch (Exception ex) { throw new RuntimeException(ex); }
	}
	
	public URILocator(URI uri)
	{
		this.uri = uri;
	}
	
	public String getNotation()
	{		
		return "URI";
	}

	public void setReference(String uri)
	{
		try { this.uri = new URI(uri); }
		catch (Exception ex) { throw new RuntimeException(ex); }
	}
	
	public String getReference()
	{
		return uri.toString();
	}

	public Locator resolveRelative(String relativePath)
	{
		try
		{
			if (uri.toString().endsWith("/"))
				return new URILocator(new URI(uri.toString() + relativePath));
			else
				return new URILocator(new URI(uri.toString() + "/" + relativePath));
		}
		catch (Exception ex)
		{
			throw new RuntimeException(ex);
		}
	}
	
	public Locator resolveLocal(String localName)
	{
		String s = uri.toString();
		if (s.endsWith("/"))
			s = s.substring(0, s.length() - 1);
		return new URILocator(s + "#" + localName);
	}
	
	public String toExternalForm()
	{
		return uri.toString();
	}
	
	public String toString()
	{
		return toExternalForm();
	}
}