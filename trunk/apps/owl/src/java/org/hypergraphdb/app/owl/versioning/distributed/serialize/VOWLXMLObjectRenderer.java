package org.hypergraphdb.app.owl.versioning.distributed.serialize;

import static org.semanticweb.owlapi.vocab.OWLXMLVocabulary.IMPORT;
import static org.hypergraphdb.app.owl.versioning.distributed.serialize.VOWLXMLVocabulary.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.hypergraphdb.app.owl.versioning.distributed.serialize.VOWLXMLDocument.DATE_FORMAT;

import org.coode.owlapi.owlxml.renderer.OWLXMLObjectRenderer;
import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGSearchResult;
import org.hypergraphdb.HGQuery.hg;
import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.app.owl.core.OWLOntologyEx;
import org.hypergraphdb.app.owl.newver.ChangeMark;
import org.hypergraphdb.app.owl.newver.ChangeSet;
import org.hypergraphdb.app.owl.newver.MarkParent;
import org.hypergraphdb.app.owl.newver.Revision;
import org.hypergraphdb.app.owl.newver.RevisionMark;
import org.hypergraphdb.app.owl.newver.VersionedOntology;
import org.hypergraphdb.app.owl.newver.VOWLObjectVisitor;
import org.hypergraphdb.app.owl.versioning.change.VAddAxiomChange;
import org.hypergraphdb.app.owl.versioning.change.VAddImportChange;
import org.hypergraphdb.app.owl.versioning.change.VAddOntologyAnnotationChange;
import org.hypergraphdb.app.owl.versioning.change.VAddPrefixChange;
import org.hypergraphdb.app.owl.versioning.change.VAxiomChange;
import org.hypergraphdb.app.owl.versioning.change.VChange;
import org.hypergraphdb.app.owl.versioning.change.VImportChange;
import org.hypergraphdb.app.owl.versioning.change.VModifyOntologyIDChange;
import org.hypergraphdb.app.owl.versioning.change.VOWLChange;
import org.hypergraphdb.app.owl.versioning.change.VOntologyAnnotationChange;
import org.hypergraphdb.app.owl.versioning.change.VPrefixChange;
import org.hypergraphdb.app.owl.versioning.change.VRemoveAxiomChange;
import org.hypergraphdb.app.owl.versioning.change.VRemoveImportChange;
import org.hypergraphdb.app.owl.versioning.change.VRemoveOntologyAnnotationChange;
import org.hypergraphdb.app.owl.versioning.change.VRemovePrefixChange;
import org.hypergraphdb.util.Pair;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.vocab.Namespaces;

/**
 * VOWLXMLObjectRenderer.
 * 
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Feb 24, 2012
 */
public class VOWLXMLObjectRenderer implements VOWLObjectVisitor
{
	private VOWLXMLWriter writer;
	private OWLXMLObjectRenderer owlObjectRenderer;
	private VOWLXMLRenderConfiguration configuration;

	private Set<HGHandle> collectRevisions(VersionedOntology vo)
	{
		throw new UnsupportedOperationException();
//		ArrayList<Revision> L = new ArrayList<Revision>();
//		return L;
	}
	
	boolean isAddChange(VOWLChange c)
	{
		return c instanceof VAddAxiomChange || 
			   c instanceof VAddImportChange || 
			   c instanceof VAddOntologyAnnotationChange || 
			   c instanceof VAddPrefixChange;
	}

	boolean isRemoveChange(VOWLChange c)
	{
		return c instanceof VRemoveAxiomChange || 
			   c instanceof VRemoveImportChange || 
			   c instanceof VRemoveOntologyAnnotationChange || 
			   c instanceof VRemovePrefixChange;
	}
	
	public VOWLXMLObjectRenderer(VOWLXMLWriter writer, VOWLXMLRenderConfiguration configuration)
	{
		this.writer = writer;
		this.configuration = configuration;
		owlObjectRenderer = new OWLXMLObjectRenderer(writer);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.hypergraphdb.app.owl.versioning.VOWLObjectVisitor#visit(org.hypergraphdb
	 * .app.owl.versioning.VersionedOntology)
	 */
	@Override
	public void visit(VersionedOntology vo)
	{
		HyperGraph graph = vo.ontology().getHyperGraph();
		
		writer.writeStartElement(VERSIONED_ONTOLOGY);
		writer.writeAttribute(VOWLXMLVocabulary.NAMESPACE + "ontologyID", vo.getOntology().getPersistent().toString());
		
		Set<HGHandle> revisions = collectRevisions(vo);
		HashSet<MarkParent> parentLinks = new HashSet<MarkParent>();
		
		for (HGHandle revisionHandle : revisions)
		{
			Revision revision = graph.get(revisionHandle);
			visit(revision);
			List<MarkParent> links = hg.getAll(graph, hg.and(hg.type(MarkParent.class), 
																   hg.incident(revisionHandle)));
			for (MarkParent parentLink : links)
			{
				if (parentLinks.contains(parentLink))
					continue;
				visit(parentLink);
				parentLinks.add(parentLink);
			}
		}
		
		for (MarkParent revisionLink : parentLinks)
		{
			if (!revisions.contains(revisionLink.parent()) || !revisions.contains(revisionLink.child()))
				continue;
			RevisionMark parentMark = vo.getMarkForRevision(revisionLink.parent());
			RevisionMark childMark =  vo.getMarkForRevision(revisionLink.child());
			visit(parentMark);
			visit(childMark);
			HGHandle currentMarkHandle = parentMark.mark();
			HGSearchResult<HGHandle> rs = graph.find(hg.dfs(currentMarkHandle, 
					hg.type(MarkParent.class), null, true, false));		
			try
			{
				while (true)
				{
					ChangeMark mark = graph.get(currentMarkHandle);
					visit(mark);
					ChangeSet<VersionedOntology> changeSet = graph.get(mark.changeset());
					visit(changeSet);
				
					if (!currentMarkHandle.equals(childMark.mark()) && rs.hasNext())
						currentMarkHandle = rs.next();
					else 
						break;
				}
			}
			finally
			{
				rs.close();
			}			
		}
		
		// Data
		
		// TODO
		if (configuration.revisionSnapshot() != null)
		{
			OWLOntologyEx ontologyData = vo.getCurrentRevision().equals(configuration.revisionSnapshot()) ?
					vo.ontology() : vo.getRevisionData(configuration.revisionSnapshot());
			// Render Ontology Data
			writer.startOntologyData(ontologyData);
			ontologyData.accept(owlObjectRenderer);
			writer.endOntologyData();
			// Need to render Ontology contained Format Prefixes here.
			if (!ontologyData.getPrefixes().isEmpty())
			{
				writer.writeStartElement(VOWLXMLVocabulary.V_PREFIX_MAP);
				Map<String, String> prefixMap = ontologyData.getPrefixes();
				for (Map.Entry<String, String> prefix : prefixMap.entrySet())
				{
					writer.writeStartElement(VOWLXMLVocabulary.V_PREFIX_MAP_ENTRY);
					writer.writeAttribute("prefixName", prefix.getKey());
					writer.writeAttribute("namespace", prefix.getValue());
					writer.writeEndElement();
				}
				writer.writeEndElement();
			}
		}
		// VersionedOntology
		writer.writeEndElement();
	}

	public void visit(MarkParent parentLink)
	{
		writer.writeStartElement(MARK_PARENT);
		writer.writeAttribute("parent", parentLink.parent().getPersistent().toString());
		writer.writeAttribute("child", parentLink.child().getPersistent().toString());
		writer.writeEndElement();
	}

	public void visit(RevisionMark revisionMark)
	{
		writer.writeStartElement(REVISION_MARK);
		writer.writeAttribute("revision", revisionMark.revision().toString());
		writer.writeAttribute("mark", revisionMark.mark().toString());
		writer.writeEndElement();
	}

	public void visit(ChangeMark changeMark)
	{
		writer.writeStartElement(CHANGE_MARK);
		writer.writeAttribute("target", changeMark.target().toString());
		writer.writeAttribute("changeSet", changeMark.changeset().toString());
		writer.writeEndElement();		
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.hypergraphdb.app.owl.versioning.VOWLObjectVisitor#visit(org.hypergraphdb
	 * .app.owl.versioning.Revision)
	 */
	@Override
	public void visit(Revision revision)
	{
		writer.writeStartElement(REVISION);
		// writer.writeStartElement("ontologyID");
		writer.writeAttribute("ontology", revision.versioned().getPersistent().toString());
		writer.writeAttribute("revision", revision.getAtomHandle().getPersistent().toString());
		writer.writeAttribute("user", revision.user());
		writer.writeAttribute("timestamp", "" + revision.timestamp());
		writer.writeAttribute("revisionComment", revision.comment());
		writer.writeEndElement();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.hypergraphdb.app.owl.versioning.VOWLObjectVisitor#visit(org.hypergraphdb
	 * .app.owl.versioning.ChangeSet)
	 */
	@Override
	public void visit(ChangeSet<VersionedOntology> changeSet)
	{
		writer.writeStartElement(CHANGE_SET);
		writer.writeAttribute("timestamp", Long.toString(changeSet.timestamp()));
		for (VChange<VersionedOntology> c : changeSet.changes())
			((VOWLChange)c).accept(this);
		writer.writeEndElement();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.hypergraphdb.app.owl.versioning.VOWLObjectVisitor#visit(org.hypergraphdb
	 * .app.owl.versioning.change.VAxiomChange)
	 */
	@Override
	public void visit(VAxiomChange change)
	{
		if (isAddChange(change))
		{
			writer.writeStartElement(V_ADD_AXIOM_CHANGE);
		}
		else if (isRemoveChange(change))
		{
			writer.writeStartElement(V_REMOVE_AXIOM_CHANGE);
		}
		else
		{
			throw new IllegalArgumentException("Implementation error: Change neither add nor remove" + change);
		}
		change.getAxiom().accept(owlObjectRenderer);

		writer.writeEndElement();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.hypergraphdb.app.owl.versioning.VOWLObjectVisitor#visit(org.hypergraphdb
	 * .app.owl.versioning.change.VImportChange)
	 */
	@Override
	public void visit(VImportChange change)
	{
		if (isAddChange(change))
		{
			writer.writeStartElement(V_ADD_IMPORT_CHANGE);
		}
		else if (isRemoveChange(change))
		{
			writer.writeStartElement(V_REMOVE_IMPORT_CHANGE);
		}
		else
		{
			throw new IllegalArgumentException("Implementation error: Change neither add nor remove" + change);
		}
		writer.writeStartElement(IMPORT);
		writer.writeTextContent(change.getImportDeclaration().getURI().toString());
		writer.writeEndElement();
		writer.writeEndElement();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.hypergraphdb.app.owl.versioning.VOWLObjectVisitor#visit(org.hypergraphdb
	 * .app.owl.versioning.change.VOntologyAnnotationChange)
	 */
	@Override
	public void visit(VOntologyAnnotationChange change)
	{
		if (isAddChange(change))
		{
			writer.writeStartElement(V_ADD_ONTOLOGY_ANNOTATION_CHANGE);
		}
		else if (isRemoveChange(change))
		{
			writer.writeStartElement(V_REMOVE_ONTOLOGY_ANNOTATION_CHANGE);
		}
		else
		{
			throw new IllegalArgumentException("Implementation error: Change neither add nor remove" + change);
		}
		change.getOntologyAnnotation().accept(owlObjectRenderer);
		writer.writeEndElement();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.hypergraphdb.app.owl.versioning.VOWLObjectVisitor#visit(org.hypergraphdb
	 * .app.owl.versioning.change.VPrefixChange)
	 */
	@Override
	public void visit(VPrefixChange change)
	{
		if (isAddChange(change))
		{
			writer.writeStartElement(V_ADD_PREFIX_CHANGE);
		}
		else if (isRemoveChange(change))
		{
			writer.writeStartElement(V_REMOVE_PREFIX_CHANGE);
		}
		else
		{
			throw new IllegalArgumentException("Prefixchange neither add nor remove: " + change);
		}
		// Write NEW
		writer.writeAttribute(Namespaces.OWL + "prefixName", change.getPrefixName());
		writer.writeAttribute(Namespaces.OWL + "prefix", change.getPrefix());
		writer.writeEndElement();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.hypergraphdb.app.owl.versioning.VOWLObjectVisitor#visit(org.hypergraphdb
	 * .app.owl.versioning.change.VModifyOntologyIDChange)
	 */
	@Override
	public void visit(VModifyOntologyIDChange change)
	{
		if (VOWLChange.isModifyChange(change))
		{
			writer.writeStartElement(V_MODIFY_ONTOLOGY_ID_CHANGE);
		}
		else
		{
			throw new IllegalArgumentException("Implementation error: Change must be modification but was not" + change);
		}
		OWLOntologyID newOntologyID = change.getNewOntologyID();
		OWLOntologyID oldOntologyID = change.getOldOntologyID();
		// Write NEW
		writer.writeStartElement(V_MODIFY_ONTOLOGY_ID_NEW_ID);
		writer.writeAttribute(Namespaces.OWL + "ontologyIRI", newOntologyID.getOntologyIRI().toString());
		if (newOntologyID.getVersionIRI() != null)
		{
			writer.writeAttribute(Namespaces.OWL + "versionIRI", newOntologyID.getVersionIRI().toString());
		}
		writer.writeEndElement();
		// Write OLD
		writer.writeStartElement(V_MODIFY_ONTOLOGY_ID_OLD_ID);
		writer.writeAttribute(Namespaces.OWL + "ontologyIRI", oldOntologyID.getOntologyIRI().toString());
		if (newOntologyID.getVersionIRI() != null)
		{
			writer.writeAttribute(Namespaces.OWL + "versionIRI", oldOntologyID.getVersionIRI().toString());
		}
		writer.writeEndElement();
		// End change
		writer.writeEndElement();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.hypergraphdb.app.owl.versioning.VOWLObjectVisitor#visit(org.hypergraphdb
	 * .app.owl.versioning.distributed.serialize.VOWLXMLRenderConfiguration)
	 */
	@Override
	public void visit(VOWLXMLRenderConfiguration configuration)
	{
		writer.writeStartElement(VOWLXMLVocabulary.RENDER_CONFIGURATION);
		writer.writeAttribute(VOWLXMLVocabulary.NAMESPACE + "unCommittedChanges", "" + configuration.isUncommittedChanges());
		writer.writeAttribute(VOWLXMLVocabulary.NAMESPACE + "maxDepth", "" + configuration.maxDepth());
		writer.writeAttribute(VOWLXMLVocabulary.NAMESPACE + "firstRevision", "" + configuration.firstRevision());
		writer.writeAttribute(VOWLXMLVocabulary.NAMESPACE + "revisionSnapshot", "" + configuration.revisionSnapshot());
		writer.writeStartElement(VOWLXMLVocabulary.ROOTS);
		for (HGHandle root : configuration.roots())
		{
			writer.writeStartElement(VOWLXMLVocabulary.HGHANDLE);
			writer.writeAttribute(VOWLXMLVocabulary.NAMESPACE + "value", root.getPersistent().toString());
			writer.writeEndElement();
		}
		writer.writeEndElement(); // Roots
		writer.writeStartElement(VOWLXMLVocabulary.HEADS);
		for (HGHandle head : configuration.heads())
		{
			writer.writeStartElement(VOWLXMLVocabulary.HGHANDLE);
			writer.writeAttribute(VOWLXMLVocabulary.NAMESPACE + "value", head.getPersistent().toString());
			writer.writeEndElement();
		}
		writer.writeEndElement(); // Heads
		writer.writeEndElement();
	}

}