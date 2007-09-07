/*
 * This file is part of the XSD for HyperGraphDB source distribution. This is copyrighted
 * software. For permitted uses, licensing options and redistribution, please see
 * the LicensingInformation file at the root level of the distribution.
 *
 * Copyright (c) 2007
 * Kobrix Software, Inc.  All rights reserved.
 */
package org.hypergraphdb.app.xsd;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGQuery;
import org.hypergraphdb.HGSearchResult;
import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.query.ComparisonOperator;
import org.hypergraphdb.query.HGQueryCondition;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Capable of importing a subset of simpleTypes.
 */
public class SchemaImporter extends DefaultHandler
{
    /*package*/ HyperGraph hg;
    private String targetNamespace;
    private Map<String, String> importedNamespaces;
    private String schemaFile;

    private ComplexTypeImporter complexType;
    private SimpleTypeImporter simpleType;

    /**
     *
     * @param uri String
     */
    public void importSchema(String schemaFile)
    {
        HGQueryCondition q = HGQuery.hg.value(schemaFile,ComparisonOperator.EQ);
        HGSearchResult<HGHandle> rs = hg.find(q);

        if(!rs.hasNext())
        {
            this.schemaFile = schemaFile;

            try
            {
                SAXParserFactory parserFactory = SAXParserFactory.newInstance();
                parserFactory.setValidating(false);
                parserFactory.setNamespaceAware(false);

                SAXParser parser = parserFactory.newSAXParser();
                InputStream is = getClass().getResourceAsStream(schemaFile);
                parser.parse(is, this);
            } catch (Exception exception)
            {
                exception.printStackTrace();
            }
        }

        rs.close();
    }

    /**
     *
     */
    public SchemaImporter(HyperGraph hg)
    {
        this.hg = hg;
    }

    /**
     *
     */
    public void startElement(String uri, String localName, String qName,
                             Attributes attributes)
    {
        /**@todo make it namespace-aware.*/

        if ("xs:schema".equals(qName))
        {
            targetNamespace = attributes.getValue("targetNamespace");

            importedNamespaces = new HashMap<String, String>();

            for (int i = 0; i < attributes.getLength(); i++)
            {
                String name = attributes.getQName(i);

                final String prefix = "xmlns";
                if (name.startsWith(prefix))
                {
                    name = name.substring(prefix.length());

                    if (0 == name.length())
                    {
                        //the default namespace.
                        name = "";
                    } else if (':' == name.charAt(0))
                    {
                        name = name.substring(1);
                    } else
                    {
                        throw new RuntimeException(
                            "Unrecognized namespace definition: " +
                            attributes.getQName(i) + '.');
                    }

                    importedNamespaces.put(name, attributes.getValue(i));
                }
            }
        } else if ("xs:simpleType".equals(qName))
        {
            simpleType = new SimpleTypeImporter(this);
            simpleType.startDefinition(attributes);
        } else if ("xs:complexType".equals(qName))
        {
            complexType = new ComplexTypeImporter(this);
            complexType.startDefinition(attributes);
        } else if ("xs:sequence".equals(qName))
        {
            complexType.startSequence(attributes);
        } else if ("xs:element".equals(qName))
        {
            complexType.startElement(attributes);
        } else if ("xs:attribute".equals(qName))
        {
            complexType.doAttribute(attributes);
        } else if ("xs:restriction".equals(qName))
        {
            simpleType.startRestriction(attributes);
        } else if ("xs:minInclusive".equals(qName))
        {
            simpleType.doMinInclusive(attributes);
        } else if ("xs:minExclusive".equals(qName))
        {
            simpleType.doMinExclusive(attributes);
        } else if ("xs:maxInclusive".equals(qName))
        {
            simpleType.doMaxInclusive(attributes);
        } else if ("xs:maxExclusive".equals(qName))
        {
            simpleType.doMaxExclusive(attributes);
        } else if ("xs:pattern".equals(qName))
        {
            simpleType.doPattern(attributes);
        } else if ("xs:totalDigits".equals(qName))
        {
            simpleType.doTotalDigits(attributes);
        } else if ("xs:fractionDigits".equals(qName))
        {
            simpleType.doFractionDigits(attributes);
        }

    } //startElement.

    /**
     *
     */
    public void endElement(String url, String localName, String qName) throws
        SAXException
    {
        if("xs:schema".equals(qName))
        {
            /**@todo write the timestamp also to do updates?*/
            /**@todo what about an "unimport" operation?*/
            hg.add(schemaFile);

        } else if ("xs:simpleType".equals(qName))
        {
            simpleType.endDefinition();
            simpleType = null;
        } else if ("xs:complexType".equals(qName))
        {
            complexType.endDefinition();
            complexType = null;
        }

    } //endElement.

    /**
     *
     * @param e SAXParseException
     */
    public void error(SAXParseException e)
    {
        e.printStackTrace();
    }

    /**
     *
     * @param e SAXParseException
     */
    public void warning(SAXParseException e)
    {
        e.printStackTrace();
    }

    /**
     *
     * @param e SAXParseException
     */
    public void fatalError(SAXParseException e)
    {
        e.printStackTrace();
    }

    /**
     *
     * @param qName String
     * @return String
     */
    /*package*/ String resolveToUri(String qName)
    {
        /**@todo supply the implementation*/

        int colon = qName.indexOf(':');
        String localName = qName.substring(1 + colon);

        return targetNamespace + '#' + localName;
    }

    /**
     *
     * @param qName String
     * @return String
     */
    public String resolveUri(String qName)
    {
        int colon = qName.indexOf(':');
        String localName = qName.substring(1 + colon);
        String ns = qName.substring(0, colon);

        String uri = importedNamespaces.get(ns);

        if (null == uri)
        {
            throw new RuntimeException("No namespace URI registered for " +
                                       qName + '.');
        }

        return uri + '#' + localName;
    }

} //SchemaImporter.
