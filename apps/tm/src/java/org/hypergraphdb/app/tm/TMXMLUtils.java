package org.hypergraphdb.app.tm;

import java.io.File;
import java.io.FileWriter;

import javax.xml.parsers.*;
import org.w3c.dom.*;
import org.w3c.dom.bootstrap.*;
import org.w3c.dom.ls.*;

import com.sun.org.apache.xml.internal.serialize.XMLSerializer;

public class TMXMLUtils
{
	/**
	 * <p>
	 * Load a topic map.
	 * </p>
	 * 
	 * @param tmSystem The topic map system (bound to a HyperGraphDB instance) where to load the map.
	 * @param uri The URI of the XML file of the topic map.
	 * @param locatorURI The URI of the base locator to the topic map.
	 * @param merge Whether to merge or replace existing TM entities for this topic map.
	 */
	public static void load(HGTopicMapSystem tmSystem, String uri, String locatorURI, boolean merge)
	{
		try
		{
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse(uri);
			TMXMLProcessor processor = new TMXMLProcessor(tmSystem, merge, locatorURI);
			processor.loadTo(doc);
		}
		catch (Exception ex)
		{
			throw new RuntimeException(ex);
		}
	}
	
	public static void writeToFile(File outputFile, HGTopicMapSystem system, String iri, String version)
	{
		TMXMLProcessor processor = new TMXMLProcessor(system, iri);
		Document doc = processor.getXmlDocument(version);
		try
		{
			XMLSerializer serializer = new XMLSerializer();
			FileWriter out = new FileWriter(outputFile);
			serializer.setOutputCharStream(out);
			serializer.serialize(doc);
			out.close();
		}
		catch (Throwable t)
		{
			throw new RuntimeException(t);
		}
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
