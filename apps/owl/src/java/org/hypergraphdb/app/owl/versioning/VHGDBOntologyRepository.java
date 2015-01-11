package org.hypergraphdb.app.owl.versioning;

import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGPersistentHandle;
import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.HGQuery.hg;
import org.hypergraphdb.app.owl.HGDBOntology;
import org.hypergraphdb.app.owl.HGDBOntologyRepository;
import org.hypergraphdb.app.owl.versioning.VHGDBOntologyRepository;
import org.hypergraphdb.transaction.HGTransactionConfig;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyChangeListener;
import org.semanticweb.owlapi.model.OWLOntologyID;

/**
 * VHGDBOntologyRepository.
 * 
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Jan 18, 2012
 */
public class VHGDBOntologyRepository extends HGDBOntologyRepository implements OWLOntologyChangeListener
{
	/**
	 * Will print time every 100 changes.
	 */
	private static boolean DBG = false;
	private static boolean DBG_CHANGES = false;

	public VHGDBOntologyRepository(String location)
	{
		super(location);
	}

	public List<VersionedOntology> getVersionControlledOntologies()
	{
		List<VersionedOntology> l = getHyperGraph().getAll(hg.type(VersionedOntology.class));
		return l;
	}

	/**
	 * Returns the Version controlled Ontology or null.
	 * 
	 * @param onto
	 * @return the versioned ontology or null, if not found.
	 */
	public VersionedOntology getVersionControlledOntology(final OWLOntology onto)
	{
		return getHyperGraph().getTransactionManager().ensureTransaction(new Callable<VersionedOntology>()
		{
			public VersionedOntology call()
			{
				// TODO maybe not loaded here? -> NPE; Check out callers
				HGHandle ontoHandle = getHyperGraph().getHandle(onto);
				if (ontoHandle == null)
				{
					if (DBG)
						System.out.println("NULL for onto " + onto);
					return null;
				}
				else
				{
					HGPersistentHandle ontoPHandle = ontoHandle.getPersistent();
					for (VersionedOntology vo : getVersionControlledOntologies())
					{
						if (vo.getHeadRevision().getOntologyUUID().equals(ontoPHandle))
						{
							return vo;
						}
					}
					return null;
				}
			}
		}, HGTransactionConfig.READONLY);
	}

	/**
	 * Adds version control to the given ontology. A VersionedOntology object
	 * with an initial revision will be created and changes will be recorded.
	 * 
	 * @param o
	 * @param user
	 * @return
	 */
	public VersionedOntology addVersionControl(final HGDBOntology o, final String user)
	{
		final HyperGraph graph = getHyperGraph();
		if (isVersionControlled(o))
			throw new IllegalStateException("Ontology already version controlled" + o.getOntologyID());
		return getHyperGraph().getTransactionManager().ensureTransaction(new Callable<VersionedOntology>()
		{
			public VersionedOntology call()
			{
				VersionedOntology newVO = new VersionedOntology(o, user, graph);
				graph.add(newVO);
				return newVO;
			}
		});
	}

	/**
	 * Removes version control.
	 * 
	 * @param vo
	 */
	public void removeVersionControl(final VersionedOntology vo)
	{
		final HyperGraph graph = getHyperGraph();
		getHyperGraph().getTransactionManager().ensureTransaction(new Callable<Object>()
		{
			public Object call()
			{
				vo.clear();
				HGHandle voHandle = graph.getHandle(vo);
				graph.remove(voHandle, true);
				return null;
			}
		});
	}

	public boolean isVersionControlled(OWLOntology o)
	{
		// TODO optimize this
		return getVersionControlledOntology(o) != null;
	}

	/**
	 * For each ontology, check if version controlled and commit. If the head
	 * changeset is not empty, a new revision will be created.
	 * 
	 * 
	 * @param ontologies
	 *            a list of ontologies, non version controlled will be ignored
	 * @param user
	 */
	public void commitAllVersioned(final List<OWLOntology> ontologies, final String user, final String commentForAll)
	{
		getHyperGraph().getTransactionManager().ensureTransaction(new Callable<Object>()
		{
			public Object call()
			{
				for (OWLOntology o : ontologies)
				{
					VersionedOntology vo = getVersionControlledOntology(o);
					if (vo != null)
					{
						vo.commit(user, commentForAll);
					}
				}
				return null;
			}
		});
	}

	//
	// OVERWRITING SUPERCLASS DELETE_ONTOLOGY
	//

	/**
	 * Deletes an ontology from the graph. If it is version controlled, the
	 * associated version controlled ontology will be deleted also.
	 */
	public boolean deleteOntology(final OWLOntologyID ontologyId)
	{
		return getHyperGraph().getTransactionManager().ensureTransaction(new Callable<Boolean>()
		{
			public Boolean call()
			{
				HGHandle ontologyHandle = getOntologyHandleByID(ontologyId);
				OWLOntology ontology = getHyperGraph().get(ontologyHandle);
				if (isVersionControlled(ontology))
				{
					VersionedOntology vOntology = getVersionControlledOntology(ontology);
					removeVersionControl(vOntology);
				}
				return VHGDBOntologyRepository.super.deleteOntology(ontologyId);
			}
		});
	}

	// ---------------------------------------------------------------------------------------
	// CHANGE LISTENER IMPLEMENTATION
	// All ontology changes pass through here.
	// ---------------------------------------------------------------------------------------

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.semanticweb.owlapi.model.OWLOntologyChangeListener#ontologiesChanged
	 * (java.util.List)
	 */
	private ThreadLocal<Boolean> ignoreChanges = new ThreadLocal<Boolean>();

	public void ignoreChangeEvents(boolean b)
	{
		ignoreChanges.set(b);
	}

	public boolean shouldIgnoreChangeEvents()
	{
		return ignoreChanges.get() != null && ignoreChanges.get();
	}

	// public void ontologiesChanged(final List<? extends OWLOntologyChange>
	// changes) throws OWLException {
	// }
	// ---------------------------------------------------------------------------------------

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.semanticweb.owlapi.model.OWLOntologyChangeListener#ontologiesChanged
	 * (java.util.List)
	 */
	@Override
	public void ontologiesChanged(final List<? extends OWLOntologyChange> changes) throws OWLException
	{
		if (shouldIgnoreChangeEvents())
			return;
		getHyperGraph().getTransactionManager().ensureTransaction(new Callable<Object>()
		{
			public Object call()
			{
				if (DBG_CHANGES)
					System.out.println("" + new Date() + " VHGDB processes applied changes: " + changes.size());
				int i = 0;
				for (OWLOntologyChange c : changes)
				{
					if (DBG_CHANGES)
					{
						i++;
						if (i % 100 == 0)
						{
							System.out.println("" + new Date() + " VHGDB changes done: " + i);
						}
					}
					// get versioned onto
					if (isVersionControlled(c.getOntology()))
					{
						VersionedOntology vo = getVersionControlledOntology(c.getOntology());
						// VOWLChange vc = VOWLChangeFactory.create(c,
						// getHyperGraph());
						vo.addAppliedChange(c);
					}
				}
				if (DBG_CHANGES)
					System.out.println("" + new Date() + " VHGDB changes done: " + i);
				// forced to use Callable:
				return null;
			}
		});
	}
}