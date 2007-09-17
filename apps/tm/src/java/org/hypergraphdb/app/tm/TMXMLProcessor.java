package org.hypergraphdb.app.tm;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGQuery.hg;
import org.hypergraphdb.atom.HGRel;
import org.tmapi.core.Association;
import org.tmapi.core.Locator;
import org.tmapi.core.TMAPIRuntimeException;
import org.tmapi.core.Topic;
import org.tmapi.core.TopicName;
import org.w3c.dom.*;
import java.util.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class TMXMLProcessor
{
	private String version = "1.0";
	private HGTopicMapSystem system;
	private boolean merge;
	private String iri;
	
	private Set<String> getItemIdentities(Element el, boolean examineAllChildren)
	{
		HashSet<String> result = new HashSet<String>();	
		NodeList kids = el.getChildNodes();
		for (int i = 0; i < kids.getLength(); i++)
		{
			Node n = kids.item(i);
			if (! (n instanceof Element))
				continue;
			else if (n.getNodeName().equals("itemIdentity"))
				result.add(U.handleIRI(((Element)n).getAttribute("href")));
			else if (!examineAllChildren)
				break;
		}
		return result;
	}

	/**
	 * <p>
	 * Retrieve the list of <code>Locator</code>s corresponding to the list
	 * of locator references. If a locator with a given reference does not exist, 
	 * add it to the system.
	 * </p>
	 * 
	 * @param uris A list of well-formed locator references.
	 * @return A list of locators, all already stored in the graph.
	 */
	private Set<HGHandle> getStoredLocators(Set<String> uris)
	{
		HashSet<HGHandle> result = new HashSet<HGHandle>();
		for (String u : uris)
			result.add(U.ensureLocator(system.getGraph(), U.makeLocator(iri, u)));
		return result;
	}
	
	private void syncLocators(Set<HGHandle> locators, Set<HGHandle> toAdd, HGTopicMapObjectBase object)
	{
		if (!merge)
		{
			Set<Locator> existingLocators = object.getSourceLocators();
			for (Locator l : existingLocators)
			{
				if (!locators.contains(system.getGraph().getHandle(l)))
					object.removeSourceLocator(l);
			}
		}
		HGHandle oHandle = system.getGraph().getHandle(object);
		for (HGHandle h : toAdd)
			system.getGraph().add(new HGRel(HGTM.SourceLocator, 
										    new HGHandle[] { h, oHandle} ),
								  HGTM.hSourceLocator);		
	}
	
	private void syncScopes(HGScopedObject x, Set<HGTopic> scope)
	{
		Set<HGTopic> existingScope = x.getScope();
		for (HGTopic t : scope)
			if (!existingScope.contains(t))
				x.addScopingTopic(t);
		if (!merge)
			for (HGTopic t : existingScope)
				if (!scope.contains(t))
					x.removeScopingTopic(t);		
	}
	
	private HGTopicMapObjectBase locate(Set<HGHandle> locators, Class type, Set<HGHandle> missing)
	{
		HGTopicMapObjectBase result = null;
		for (HGHandle h : locators)
		{
			Object x = U.getOneRelated(system.getGraph(), HGTM.hSourceLocator, h, null);
			if (x == null)
				missing.add(h);
			else if (type.isAssignableFrom(x.getClass()))
			{
				if (result == null)
					result = (HGTopicMapObjectBase)x;
				else if (result != x)
					throw new RuntimeException("Attempt a add a topic map object '" + type.getName() + 
							"' with a locator '" +
							((Locator)system.getGraph().get(h)).getReference() + "'" +
							" that is already used for another object '" + x + "'");
			}
			else
				throw new RuntimeException("Attempt a add a topic map  object '" + type.getName() + 
						"' with a locator '" +
						((Locator)system.getGraph().get(h)).getReference() + "'" +
						" that is already used for another object '" + x + "'");
		}		
		return result;
	}

	private HGTopic getTopicRef(Element el, HGTopicMap map)
	{
		String href = U.handleIRI(el.getAttribute("href"));
		Locator l = U.ensureLocator(system.getGraph(), iri, href);
		HGHandle lh = system.getGraph().getHandle(l);
		HGTopic topic = (HGTopic)U.getOneRelated(system.getGraph(), HGTM.hSubjectIdentifier, lh, null);
		if (topic == null)
			topic = (HGTopic)U.getOneRelated(system.getGraph(), HGTM.hSourceLocator, lh, null);
		if (topic == null)
		{
			topic = (HGTopic)map.createTopic();
			topic.addSourceLocator(l);
		}
		return topic;
	}
	
	private Set<HGTopic> getTopicRefs(Element el, HGTopicMap map)
	{		
		HashSet<HGTopic> result = new HashSet<HGTopic>();
		NodeList kids = el.getChildNodes();
		for (int i = 0; i < kids.getLength(); i++)
		{
			Node n = kids.item(i);
			if (! (n instanceof Element))
				continue;
			else if (n.getNodeName().equals("topicRef"))
				result.add(getTopicRef((Element)n, map));
		}
		return result;
	}
	
	private void handleReifier(String reifier, HGTopicMap map, HGHandle object)
	{		
		if (reifier == null)
			return;
		reifier = U.handleIRI(reifier);
		Locator l = U.makeLocator(reifier);
		HGHandle rl = U.ensureLocator(system.getGraph(), l);
		HGTopic rtopic = (HGTopic)U.getOneRelated(system.getGraph(), HGTM.hSourceLocator, rl, null);
		if (rtopic == null)
		{
			rtopic = (HGTopic)map.createTopic();
			rtopic.addSourceLocator(l);
		}
		U.setReifierOf(system.getGraph(), 
					   object, 
					   system.getGraph().getHandle(rtopic));
	}
	
	private void load2(Element top) throws Exception
	{		
		Set<HGHandle> locators = getStoredLocators(getItemIdentities(top, false));
		HashSet<HGHandle> toAdd = new HashSet<HGHandle>();
		HGTopicMap tm = (HGTopicMap)system.getTopicMap(iri);
		if (tm == null)
			tm = (HGTopicMap)locate(locators, HGTopicMap.class, toAdd);		
		if (tm == null)
			tm = (HGTopicMap)system.createTopicMap(iri);
		else
		{
			tm.setBaseLocator(U.ensureLocator(system.getGraph(), null, iri));
			system.getGraph().update(tm);
		}
		syncLocators(locators, toAdd, tm);		
		NodeList kids = top.getChildNodes();
		for (int i = 0; i < kids.getLength(); i++)
		{
			Node n = kids.item(i);
			if (! (n instanceof Element))
				continue;
			else if (n.getNodeName().equals("topic"))
				loadTopic2((Element)n, tm);
			else if (n.getNodeName().equals("association"))
				loadAssociation2((Element)n, tm);
		}		
	}
	
	private void loadVariant2(Element el, HGTopicMap map, HGTopicName name)
	{
		Set<HGHandle> locators = getStoredLocators(getItemIdentities(el, false));
		Set<HGHandle> toAdd = new HashSet<HGHandle>();
		HGVariant var = (HGVariant)locate(locators, HGVariant.class, toAdd);
		String value = "";
		Locator dataType = null;
		Set<HGTopic> scope = new HashSet<HGTopic>();
		
		int idx;		
		NodeList kids = el.getChildNodes();
		for (idx = 0; idx < kids.getLength(); idx++)
		{
			Node n = kids.item(idx);
			if (! (n instanceof Element))
				continue;
			else if (n.getNodeName().equals("scope"))
			{
				scope = getTopicRefs((Element)n, map);
			}
			else if (n.getNodeName().equals("resourceRef"))
			{
				String href = ((Element)n).getAttribute("href");
				value = U.handleIRI(href);
				dataType = (Locator)system.getGraph().get(HGTM.hSchemaAnyURI);
			}
			else if (n.getNodeName().equals("resourceData"))
			{
				Element e = (Element)n;
				String dtAttr = e.getAttribute("datatype");
				if (dtAttr == null)
					dtAttr = HGTM.schemaStringType;
				dataType = U.ensureLocator(system.getGraph(), null, dtAttr);				
				if (dtAttr.equals(HGTM.schemaAnyType))
					value = TMXMLUtils.canonicalizeContent(e);
				else if (dtAttr.equals(HGTM.schemaAnyURI))
					value = U.handleIRI(n.getTextContent());
				else
					value = n.getTextContent();
			}
			else
				break;
		}
		scope.addAll(name.getScope());
		if (var == null)
			var = (HGVariant)name.createVariant(value, dataType, scope);
		else 
		{
			var.setValue(value);
			var.setDataType(dataType);
			syncScopes(var, scope);
			system.getGraph().update(var);
		}		
		syncLocators(locators, toAdd, var);
		handleReifier(el.getAttribute("reifier"), map, system.getGraph().getHandle(var));
	}
	
	private void loadName2(Element el, HGTopicMap map, HGTopic topic)
	{
		Set<HGHandle> locators = getStoredLocators(getItemIdentities(el, false));
		Set<HGHandle> toAdd = new HashSet<HGHandle>();
		HGTopicName name = (HGTopicName)locate(locators, HGTopicName.class, toAdd);
		String value = "";
		HGTopic type = null;
		Set<HGTopic> scope = null;
		
		int idx;		
		NodeList kids = el.getChildNodes();
		for (idx = 0; idx < kids.getLength(); idx++)
		{
			Node n = kids.item(idx);
			if (! (n instanceof Element))
				continue;
			else if (n.getNodeName().equals("type"))
			{
				Set<HGTopic> types = getTopicRefs((Element)n, map);
				if (types.size() > 0)
					type = types.iterator().next();
			}
			else if (n.getNodeName().equals("scope"))
			{
				scope = getTopicRefs((Element)n, map);
			}
			else if (n.getNodeName().equals("value"))
			{
				value = n.getTextContent();
			}
			else
				break;
		}
		
		if (type == null)
			type = (HGTopic)system.getGraph().get(HGTM.hTopicNameTopic);
		
		if (name == null)
			name = (HGTopicName)topic.createTopicName(value, type, scope);
		else 
		{
			name.setType(type);
			name.setValue(value);
			system.getGraph().update(name);
			syncScopes(name, scope);
		}
		syncLocators(locators, toAdd, name);

		// 
		// Add variants
		//
		for (; idx < kids.getLength(); idx++)
		{
			Node n = kids.item(idx);
			if (! (n instanceof Element))
				continue;
			else if (n.getNodeName().equals("variant"))
			{
				loadVariant2((Element)el, map, name);
			}
			else
				throw new RuntimeException("Unexpected element '" + 
						n.getNodeName() + "' in topic name tag with value '" + 
						value + "'");
		}
		
		//
		// Once the name has been properly added, we can add the reifier property, is present.
		//
		handleReifier(el.getAttribute("reifier"), map, system.getGraph().getHandle(name));
	}
	
	private void loadOccurrence2(Element el, HGTopicMap map, HGTopic topic)
	{
		Set<HGHandle> locators = getStoredLocators(getItemIdentities(el, false));
		Set<HGHandle> toAdd = new HashSet<HGHandle>();
		HGOccurrence occ = (HGOccurrence)locate(locators, HGOccurrence.class, toAdd);
		String value = "";
		Locator dataType = null;
		HGTopic type = null;
		Set<HGTopic> scope = null;
		
		int idx;		
		NodeList kids = el.getChildNodes();
		for (idx = 0; idx < kids.getLength(); idx++)
		{
			Node n = kids.item(idx);
			if (! (n instanceof Element))
				continue;
			else if (n.getNodeName().equals("type"))
			{
				Set<HGTopic> types = getTopicRefs((Element)n, map);
				if (types.size() > 0)
					type = types.iterator().next();
			}
			else if (n.getNodeName().equals("scope"))
			{
				scope = getTopicRefs((Element)n, map);
			}
			else if (n.getNodeName().equals("resourceRef"))
			{
				String href = ((Element)n).getAttribute("href");
				value = U.handleIRI(href);
				dataType = (Locator)system.getGraph().get(HGTM.hSchemaAnyURI);
			}
			else if (n.getNodeName().equals("resourceData"))
			{
				Element e = (Element)n;
				String dtAttr = e.getAttribute("datatype");
				if (dtAttr == null)
					dtAttr = HGTM.schemaStringType;
				dataType = U.ensureLocator(system.getGraph(), null, dtAttr);				
				if (dtAttr.equals(HGTM.schemaAnyType))
					value = TMXMLUtils.canonicalizeContent(e);
				else if (dtAttr.equals(HGTM.schemaAnyURI))
					value = U.handleIRI(n.getTextContent());
				else
					value = n.getTextContent();
			}
			else
				break;
		}
		
		if (occ == null)
			occ= (HGOccurrence)topic.createOccurrence(value, type, scope);
		else 
		{
			occ.setType(type);
			occ.setValue(value);
			occ.setDataType(dataType);
			system.getGraph().update(occ);
			syncScopes(occ, scope);			
		}
		syncLocators(locators, toAdd, occ);
		handleReifier(el.getAttribute("reifier"), map, system.getGraph().getHandle(occ));		
	}

	private HGAssociationRole loadRole2(Element el, HGAssociation ass, HGTopicMap map)
	{
		Set<HGHandle> locators = getStoredLocators(getItemIdentities(el, false));
		Set<HGHandle> toAdd = new HashSet<HGHandle>();
		HGAssociationRole role = (HGAssociationRole)locate(locators, HGAssociationRole.class, toAdd);
	  	HGTopic player = null, type = null;
		NodeList kids = el.getChildNodes();
		for (int i = 0; i < kids.getLength(); i++)
		{
			Node n = kids.item(i);
			if (! (n instanceof Element))
				continue;
			else if (n.getNodeName().equals("type"))
			{
				Set<HGTopic> refs = getTopicRefs((Element)n, map);
				if (refs.size() == 0) throw new RuntimeException("No role type defined for " + role);
				type = refs.iterator().next();
			}
			else if (n.getNodeName().equals("topicRef"))
			{
				 player = getTopicRef((Element)n, map);
			}
		}
		if (type == null)
			throw new RuntimeException("Role without a type " + role);
		if (player == null)
			throw new RuntimeException("Role without a player " + role);		
		if (role != null)
		{
			if (role.getAssociation() != ass)
				throw new RuntimeException("Attempting to reassign a role " + 
										   role + " to a different association.");
			boolean update = false;
			if (role.getPlayer() != player) { role.setPlayer(player); update = true; }
			if (role.getType() != type) { role.setType(type); update = true; }
			if (update) system.getGraph().update(role);
		}
		else
		{
			HGHandle [] roleTargets = new HGHandle[] { system.getGraph().getHandle(player), 
													   system.getGraph().getHandle(type), 
													   system.getGraph().getHandle(ass)};
			HGHandle hRole = hg.findOne(system.getGraph(), hg.and(hg.type(HGAssociationRole.class),
																  hg.orderedLink(roleTargets)));
			if (hRole != null)
				role = (HGAssociationRole)system.getGraph().get(hRole);
			else
			{
				role = new HGAssociationRole(roleTargets);
				role.graph = system.getGraph();
				system.getGraph().add(role);
			}
		}
		handleReifier(el.getAttribute("reifier"), map, system.getGraph().getHandle(role));		
		return role;
	}
	
	private void loadTopic2(Element el, HGTopicMap map)
	{
		String id = el.getAttribute("id");
		Set<HGHandle> locators = getStoredLocators(getItemIdentities(el, true)); 
		locators.add(U.ensureLocator(system.getGraph(), U.makeLocator(iri + "#" + id)));
		Set<HGHandle> toAdd = new HashSet<HGHandle>();
		HGTopic topic = (HGTopic)locate(locators, HGTopic.class, toAdd);
		if (topic == null)
			topic = (HGTopic)map.createTopic();
		syncLocators(locators, toAdd, topic);
		Set<Locator> subjectLocators = topic.getSubjectLocators();
		Set<Locator> subjectIdentifiers = topic.getSubjectIdentifiers();
		NodeList kids = el.getChildNodes();
		for (int i = 0; i < kids.getLength(); i++)
		{
			Node n = kids.item(i);
			if (! (n instanceof Element))
				continue;
			else if (n.getNodeName().equals("subjectLocator"))
			{
				String href = U.handleIRI(((Element)n).getAttribute("href"));
				Locator l = U.ensureLocator(system.getGraph(), iri, href);
				if (!subjectLocators.contains(l))
					topic.addSubjectLocator(l);				
			}
			else if (n.getNodeName().equals("subjectIdentifier"))
			{
				String href = U.handleIRI(((Element)n).getAttribute("href"));
				Locator l = U.ensureLocator(system.getGraph(), iri, href);
				if (!subjectIdentifiers.contains(l))
					topic.addSubjectIdentifier(l);
			}
			else if (n.getNodeName().equals("instanceOf"))
			{
				Set<HGTopic> types = getTopicRefs((Element)n, map);
				Set<Topic> existingTypes = topic.getTypes();
				for (HGTopic t : types)
				{
					if (!existingTypes.contains(t))
						topic.addType(t);
				}
				if (!merge)
					for (Topic t : existingTypes)
						if (!types.contains(t))
							topic.removeType(t);
			}
			else if (n.getNodeName().equals("name"))
			{
				loadName2((Element)n, map, topic);
			}
			else if (n.getNodeName().equals("occurrence"))
			{
				loadOccurrence2((Element)n, map, topic);
			}
			else
				throw new RuntimeException("Unexpected element '" + 
										   n.getNodeName() + "' in <topic>");			
		}
	}
	
	private void loadAssociation2(Element el, HGTopicMap map)
	{
		Set<HGHandle> locators = getStoredLocators(getItemIdentities(el, true)); 
		Set<HGHandle> toAdd = new HashSet<HGHandle>();
		HGAssociation ass = (HGAssociation)locate(locators, HGAssociation.class, toAdd);
		Set<HGAssociationRole> roles = new HashSet<HGAssociationRole>();
		Set<HGTopic> scope = new HashSet<HGTopic>();
		boolean isnew = false;
		if (ass == null)
		{
			ass = (HGAssociation)map.createAssociation();
			isnew = true;
		}
		syncLocators(locators, toAdd, ass);		
		NodeList kids = el.getChildNodes();
		for (int i = 0; i < kids.getLength(); i++)
		{
			Node n = kids.item(i);
			if (! (n instanceof Element))
				continue;
			else if (n.getNodeName().equals("type"))
			{
				Set<HGTopic> refs = getTopicRefs((Element)n, map);
				if (refs.size() == 0) throw new RuntimeException("No role type defined for " + ass);				
				ass.setType(refs.iterator().next());
			}
			else if (n.getNodeName().equals("scope"))
			{
				scope = getTopicRefs((Element)n, map);				
			}
			else if (n.getNodeName().equals("role"))
			{
				roles.add(loadRole2((Element)n, ass, map));
			}
			else
				throw new RuntimeException("Unexpected element '" + 
										   n.getNodeName() + "' in <association>");
		}
		if (!roles.isEmpty())
		{
			Set<HGAssociationRole> existingRoles = new HashSet<HGAssociationRole>();
			existingRoles.addAll(ass.getAssociationRoles());
			Set<HGAssociationRole> removed = new HashSet<HGAssociationRole>();
			boolean setChanged = false;
			if (merge)
			{
				for (HGAssociationRole r : roles)
					if (!existingRoles.contains(r))
					{
						existingRoles.add(r);
						setChanged = true;
					}
			}
			else
			{
				for (HGAssociationRole r : existingRoles)
					if (!roles.contains(r))
					{
						setChanged = true;
						removed.add(r);
					}			
				setChanged = setChanged || existingRoles.size() != roles.size();
				if (setChanged)
					existingRoles = roles;
			}
			if (setChanged)
			{
				HGHandle [] newTargetSet = new HGHandle[existingRoles.size()];
				int i = 0;
				for (HGAssociationRole r : existingRoles)
					newTargetSet[i++] = system.getGraph().getHandle(r);
				ass.setTargetSet(newTargetSet);
				system.getGraph().update(ass);
				for (HGAssociationRole r : removed)
					try { r.remove(); } catch (Exception ex) { throw new TMAPIRuntimeException(ex); }
			}
		}
		else if (isnew)
			throw new RuntimeException("Association " + ass + " defined with no roles whatsoever.");
		this.syncScopes(ass, scope);
		handleReifier(el.getAttribute("reifier"), map, system.getGraph().getHandle(ass));		
	}
	
	public TMXMLProcessor(HGTopicMapSystem system, boolean merge, String iri)
	{
		if (system == null)
			throw new IllegalArgumentException("system parameter is null");
		if (iri == null || iri.length() == 0)
			throw new IllegalArgumentException("IRI of document is null or empty.");
		this.system = system;
		this.merge = merge;
		this.iri = U.handleIRI(iri);		
	}
	
	public void loadTo(Document doc)
	{
		try
		{
			Element top = doc.getDocumentElement();
			String vattr = top.getAttribute("version");
			if (vattr != null)
				version = vattr;
			if ("2.0".equals(version))
				load2(top);
		}
		catch (Exception ex)
		{
			throw new RuntimeException(ex);
		}
	}
	
	/**
	 * <p>
	 * Export a topic map into an XML DOM element. 
	 * </p>
	 * 
	 * @param system The topic map system.
	 * @param iri The IRI of the topic map (i.e. its base locator).
	 * @param version The desired XTM version - either 1.0 or 2.0. If <code>null</code>,
	 * version 2.0 is assumed.
	 * @return A DOM <code>Element</code> of the topic map. 
	 */
	public Element exportMap(HGTopicMapSystem system, String iri, String version)
	{
		try
		{
			HGTopicMap map = (HGTopicMap)system.getTopicMap(iri);
			if (map == null)
				return null;
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.newDocument();
			if (version == null || "2.0".equals(version))
			{
				Element el = doc.createElement("topicMap");
				el.setAttribute("version", "2.0");
				el.setAttribute("xmlns", "http://www.topicmaps.org/xtm/");
				for (Topic t : map.getTopics())
					exportTopic2(el, t);							
				for (Association a : map.getAssociations())
					exportAssociation2(el, a);
				for (Locator l : map.getSourceLocators())
					exportLocator2(el, l);
				return el;
			}
			else
			{
				throw new IllegalArgumentException("Unsupport XTM version " + version);
			}
		}		
		catch (Exception ex)
		{
			throw new RuntimeException(ex);
		}
	}
	
	private void exportTopic2(Element parentEl, Topic t)
	{
		Element el = parentEl.getOwnerDocument().createElement("topic");
		for (Object n : t.getTopicNames())
			exportName2(el, (TopicName)n);
		parentEl.appendChild(el);
	}
	
	private void exportName2(Element parentEl, TopicName name)
	{
		Element el = parentEl.getOwnerDocument().createElement("name");	
	}
	
	private void exportAssociation2(Element parentEl, Association a)
	{
		Element el = parentEl.getOwnerDocument().createElement("association");		
	}
	
	private void exportLocator2(Element parentEl, Locator l)
	{
		Element el = parentEl.getOwnerDocument().createElement("association");		
	}
	
}