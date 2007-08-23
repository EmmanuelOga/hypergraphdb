package org.hypergraphdb.app.tm;

import javax.xml.parsers.*;
import org.w3c.dom.*;
import org.w3c.dom.bootstrap.*;
import org.w3c.dom.ls.*;

public class TMXMLUtils
{
	public static void load(String uri, HGTopicMapSystem tmSystem)
	{
		try
		{
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse(uri);
			loadTopicMap(doc.getDocumentElement(), tmSystem);
		}
		catch (Exception ex)
		{
			throw new RuntimeException(ex);
		}
	}
	
	public static void loadTopicMap(Element tmNode, HGTopicMapSystem tmSystem) throws Exception
	{
	}
	
	public static String canonicalizeContent(Element e)
	{
		try
		{
			// TODO: this will only work if the DOMImplementationRegistry.PROPERTY system
			// property contains a DOM implementation source that supports the "LS 3.0"
			// feature where the LSSerializer support a setting of "canonical-form=true".
			DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance();
			DOMImplementation impl = registry.getDOMImplementation("LS 3.0");
			DOMImplementationLS implLS = (DOMImplementationLS)impl.getFeature("LS", "3.0");
			LSSerializer serializer = implLS.createLSSerializer();
			serializer.getDomConfig().setParameter("canonical-form", Boolean.TRUE);
			StringBuffer result = new StringBuffer();
			NodeList kids = e.getChildNodes();
			for (int i = 0; i < kids.getLength(); i++)
				result.append(serializer.writeToString(kids.item(i)));
			return result.toString();
		}
		catch (Exception ex)
		{
			// we don't throw for now because we rely on the XML implementation supporting
			// canonicalization
			ex.printStackTrace(System.err);
			StringBuffer result = new StringBuffer();
			NodeList kids = e.getChildNodes();
			for (int i = 0; i < kids.getLength(); i++)
				result.append(kids.item(i).getTextContent());
			return result.toString();			
		}
	}
}
