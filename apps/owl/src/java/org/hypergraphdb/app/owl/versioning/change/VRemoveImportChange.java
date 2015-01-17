package org.hypergraphdb.app.owl.versioning.change;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.app.owl.newver.VersionedOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.RemoveImport;

/**
 * VRemoveImportChange.
 * 
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Jan 17, 2012
 */
public class VRemoveImportChange extends VImportChange
{
	public VRemoveImportChange(HGHandle... args)
	{
		super(args[0]);
	}

	@Override
	public VChange<VersionedOntology> inverse()
	{
		return new VAddImportChange(getImportDeclarationHandle());
	}

	@Override
	public boolean isEffective(VersionedOntology versioned)
	{
		return versioned.ontology().getImportsDeclarations().contains(getImportDeclaration());
	}

	@Override
	public OWLOntologyChange toOWLChange(VersionedOntology versioned)
	{
		return new RemoveImport(versioned.ontology(), getImportDeclaration());
	}
}