package org.hypergraphdb.app.owl.versioning;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.Callable;

import org.hypergraphdb.HGGraphHolder;
import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGLink;
import org.hypergraphdb.HGPersistentHandle;
import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.annotation.HGIgnore;
import org.hypergraphdb.app.owl.HGDBOntology;
import org.hypergraphdb.app.owl.core.OWLOntologyEx;
import org.hypergraphdb.app.owl.core.OWLTempOntologyImpl;
import org.hypergraphdb.app.owl.versioning.change.VOWLChange;
import org.hypergraphdb.app.owl.versioning.change.VOWLChangeFactory;
import org.hypergraphdb.transaction.HGTransactionConfig;
import org.hypergraphdb.util.Pair;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.AddImport;
import org.semanticweb.owlapi.model.AddOntologyAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLImportsDeclaration;
import org.semanticweb.owlapi.model.OWLMutableOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyManager;

/**
 * A VersionedOntology represents all revisions and changesets of one versioned
 * ontology. Only one concrete owlontology (revision data) is currently
 * maintained. This is the workingset (Head + uncommitted changes). All added
 * changes are instantly persisted in changesets and survive downtime. Each
 * commit leads to a new revision and opens a new empty changeset.
 * 
 * Conflicting changes are never applied to the ontology here.
 * 
 * Revisions are ordered by RevisionID. The first revision is called base
 * revision, the last head revision. The changeset that accepts changes until
 * the next commit is called head changeset.
 * 
 * Usage: By the time we add Version control, we have a revision. This is
 * initially both, base and head revision. It's data is the OWLOntology at that
 * time. Subsequent changes will be added to the head changeset and applied to
 * the head revision data. Rollback: will undo all changes in the head
 * changeset. Commit: will create a new head revision and a new empty head
 * changeset, closing the old head changeset.
 * 
 * 
 * Implementation: Usage of Pair objects. One pair refers to one revision and
 * the changeset that was applied after the revision.
 * 
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Jan 13, 2012
 */
public class VersionedOntology implements HGLink, HGGraphHolder, VersioningObject
{

	/**
	 * The list of all changeSet and Revision Pairs. Each pair represents the
	 * changeset that leads/led to the pair's revision.
	 */
	private List<HGHandle> revisionAndChangeSetPairs;

	protected HyperGraph graph;

	private SortedSet<Integer> workingSetConflicts = new TreeSet<Integer>();

	public VersionedOntology(HGHandle... targets)
	{
		revisionAndChangeSetPairs = new ArrayList<HGHandle>(Arrays.asList(targets));
		// assert at least one Pair.
	}

	/**
	 * Creates a versionedOntology containing the given list of revisions and
	 * changesets. The last changeset is allowed to miss (workingset), an empty
	 * one will be created. Revision objects must not to be stored in the graph
	 * before calling. Changesets, Changes and axioms, importdecls and
	 * ontoAnnotations must be stored in the graph and currently loaded. The
	 * caller shall add the Versionedontology object to the graph.
	 * 
	 * @param revisions
	 * @param changeSets
	 */
	public VersionedOntology(final List<Revision> revisions, final List<ChangeSet> changeSets, HyperGraph graph)
	{
		this.graph = graph;
		if (revisions.size() - changeSets.size() > 1)
			throw new IllegalArgumentException("Sizes must match by 1;" + " only last changeset may be omitted. \n Revisions: "
					+ revisions.size() + " Changesets: " + changeSets.size());
		if (revisions.size() - changeSets.size() < 0)
			throw new IllegalArgumentException("Must not have less revisions than changesets.");
		revisionAndChangeSetPairs = new ArrayList<HGHandle>(revisions.size());
		graph.getTransactionManager().ensureTransaction(new Callable<Object>()
		{
			public Object call()
			{
				for (int i = 0; i < revisions.size(); i++)
				{
					HGHandle curCsHandle;
					if (i < changeSets.size())
					{
						curCsHandle = VersionedOntology.this.graph.getHandle(changeSets.get(i));
					}
					else if (i == changeSets.size())
					{
						curCsHandle = VersionedOntology.this.graph.add(new ChangeSet());
					}
					else
					{
						throw new IllegalArgumentException("Too many changesets. This should be unreachable code.");
					}
					if (curCsHandle == null)
					{
						throw new NullPointerException("Changeset must be in graph and loaded:" + changeSets.get(i));
					}
					Pair<Revision, HGHandle> pair = new Pair<Revision, HGHandle>(revisions.get(i), curCsHandle);
					HGHandle pairHandle = VersionedOntology.this.graph.add(pair);
					revisionAndChangeSetPairs.add(pairHandle);
				}
				return null;
			}
		});
		// assert at least one Pair.
	}

	/**
	 * Creates an
	 * 
	 * Should be called within HGTransaction.
	 * 
	 * @param onto
	 *            an ontology already stored in the graph
	 * @param user
	 */
	public VersionedOntology(HGDBOntology onto, String user, HyperGraph graph)
	{
		this.graph = graph;
		// link to onto as head and base copy
		initialize(onto, user);
	}

	private void initialize(final HGDBOntology onto, final String user)
	{
		graph.getTransactionManager().ensureTransaction(new Callable<Object>()
		{
			public Object call()
			{
				revisionAndChangeSetPairs = new ArrayList<HGHandle>();
				commitInternal(graph.getHandle(onto).getPersistent(), user, Revision.REVISION_FIRST, "Initial Revision");
				return null;
			}
		});
	}

	public Revision getHeadRevision()
	{
		return getRevision(revisionAndChangeSetPairs.size() - 1);
	}

	public ChangeSet getWorkingSetChanges()
	{
		return getChangeSet(revisionAndChangeSetPairs.size() - 1);
	}

	/**
	 * The number of changes that will be committed. (Workingset - conflicts)
	 * 
	 * @return
	 */
	public int getNrOfCommittableChanges()
	{
		int wsChanges = getWorkingSetChanges().size();
		int wsConflicts = getWorkingSetConflicts().size();
		if (wsChanges - wsConflicts < 0)
			throw new IllegalStateException("Invariant broken: More conflicts than changes.");
		return wsChanges - wsConflicts;
	}

	/**
	 * 
	 * @return a sorted set of indices of conflicting changes in the workingset.
	 */
	public SortedSet<Integer> getWorkingSetConflicts()
	{
		return workingSetConflicts;
	}

	/**
	 * This will remove all conflicting changes from the workingsetchanges.
	 */
	private void clearWorkingSetConflicts()
	{
		if (!workingSetConflicts.isEmpty())
		{
			getWorkingSetChanges().removeChangesAt(workingSetConflicts);
			setWorkingSetConflicts(new TreeSet<Integer>());
		}
	}

	/**
	 * Only public for bean serialization. Do not use.
	 * 
	 * @param workingSetConflicts
	 *            the workingSetConflicts to set
	 */
	private void setWorkingSetConflicts(SortedSet<Integer> workingSetConflicts)
	{
		this.workingSetConflicts = workingSetConflicts;
		// Allow setting by Hypergraph while no graph is set here.
		if (graph != null)
		{
			graph.update(this);
		}
	}

	/**
	 * Gets the workingsetData. Do not expect the ontology to have an OWLManager
	 * set.
	 * 
	 * @return
	 */
	public HGDBOntology getWorkingSetData()
	{
		return graph.get(getHeadRevision().getOntologyUUID());
	}

	/**
	 * Gets the revision Data representing a particular revision. This is an
	 * expensive operation. Only Committed changes will be included. Returns a
	 * partial in memory Ontology, that has in memory sets and indices, but
	 * shares graph loaded axioms and Entities. It is expected to fail during a
	 * GC run, if after onto creation: A) the head ontology gets reverted to a
	 * state before the r revision AND GC is run after that OR B) Version
	 * Control get's removed from the head ontology, which deletes all changes
	 * AND GC is run after that OR C) The head ontology gets Removed AND GC is
	 * run after that
	 * 
	 * @param r
	 * @return
	 */
	public OWLTempOntologyImpl getRevisionData(Revision targetRevision)
	{
		// Assert revision is in VersionedOnto
		HGDBOntology latest = getWorkingSetData();
		// Create an empty copy.
		OWLTempOntologyImpl memOnto = copyIntoPartialInMemOnto(latest);
		// Revert all Changesets from HEAD to r in mem
		List<ChangeSet> changeSetsToRevert = getChangeSetsFromRevisionToLast(targetRevision);
		// Iterate over list last to first
		ListIterator<ChangeSet> csIt = changeSetsToRevert.listIterator(changeSetsToRevert.size());
		while (csIt.hasPrevious())
		{
			ChangeSet cs = csIt.previous();
			if (cs == getWorkingSetChanges())
			{
				cs.reverseApplyTo(memOnto, getWorkingSetConflicts());
			}
			else
			{
				cs.reverseApplyTo(memOnto);
			}
		}
		return memOnto;
	}

	/**
	 * Returns an OWLOntology representing the given revision by rolling back
	 * changes in memory. If the given revision is the head and
	 * includeUncommited is true, the workingsetData will be returned.
	 * 
	 * @param targetRevisionIndex
	 *            [0..getArity()[
	 * @param includeUncommitted
	 * @return
	 */
	public OWLOntologyEx getRevisionData(int targetRevisionIndex, boolean includeUncommitted)
	{
		if (targetRevisionIndex == getArity() - 1 && targetRevisionIndex >= 0 && includeUncommitted)
		{
			// Workingset requested
			return getWorkingSetData();
		}
		else
		{
			Revision target = getRevision(targetRevisionIndex);
			return getRevisionData(target);
		}
	}

	/**
	 * Returns a view of all changesets from the one after r including the after
	 * head uncommitted changeset.
	 * 
	 * @param r
	 * @return a sublist from changeset after r including the changeset after
	 *         head.
	 */
	public List<ChangeSet> getChangeSetsFromRevisionToLast(Revision r)
	{
		int i = indexOf(r);
		List<ChangeSet> allCS = getChangeSets();
		return allCS.subList(i, allCS.size()); // toIndex is exclusive
	}

	/**
	 * Returns a partial in memory Ontology, that has in memory sets and
	 * indices, but relies on persisted axioms and entities. It is expected to
	 * fail during a GC run, if after onto creation: A) the Head ontology gets
	 * reverted to a state before the r revision AND GC is run after that OR B)
	 * Version Control get's removed from the head ontology, which deletes all
	 * changes AND GC is run after that OR C) The head ontology gets Removed AND
	 * GC is run after that
	 * 
	 * @param original
	 * @return
	 */
	private OWLTempOntologyImpl copyIntoPartialInMemOnto(HGDBOntology original)
	{
		OWLOntologyManager manager = original.getOWLOntologyManager();
		if (manager == null)
		{
			System.out.println("copyIntoPartialInMemOnto: OWLTempOntologyImpl: creating custom in memory manager for new onto.");
			manager = OWLManager.createOWLOntologyManager();
		}
		OWLTempOntologyImpl memOnto = new OWLTempOntologyImpl(manager, original.getOntologyID());
		// Copy A) ImportDeclarations
		for (OWLImportsDeclaration id : original.getImportsDeclarations())
		{
			memOnto.applyChange(new AddImport(memOnto, id));
		}
		// Copy B) ImportDeclarations
		for (OWLAnnotation an : original.getAnnotations())
		{
			memOnto.applyChange(new AddOntologyAnnotation(memOnto, an));
		}
		// Copy C) Axioms
		for (OWLAxiom ax : original.getAxioms())
		{
			memOnto.applyChange(new AddAxiom(memOnto, ax));
		}
		// Copy D) Prefixes
		memOnto.setPrefixesFrom(original.getPrefixes());
		return memOnto;
	}

	/**
	 * Should be called within HGTransaction.
	 * 
	 * @param index
	 * @return
	 */
	private Revision getRevision(int index)
	{
		HGHandle pairHandle = revisionAndChangeSetPairs.get(index);
		Pair<Revision, HGHandle> pair = graph.get(pairHandle);
		return pair.getFirst();
	}

	/**
	 * Should be called within HGTransaction.
	 * 
	 * @param index
	 * @return
	 */
	private ChangeSet getChangeSet(int index)
	{
		HGHandle pairHandle = revisionAndChangeSetPairs.get(index);
		Pair<Revision, HGHandle> pair = graph.get(pairHandle);
		HGHandle csHandle = pair.getSecond();
		return graph.get(csHandle);
	}

	/**
	 * Creates a new Pair object, adds it to graph and it's handle to our
	 * pairlist, and updates or adds this versioned Ontology. All conflicting
	 * changes will be removed from the changeset during the commit.
	 *
	 * Structure created: <code>
	 * pairList.add(pairHandle --First--> Revision (Persistenthandle, int revision)
	 *                         --Second-> changeSetHandle --> ChangeSet(empty));
	 * </code>
	 * 
	 * Should be called within HGTransaction.
	 * 
	 * @param ontoHandle
	 * @param user
	 * @param revision
	 *            sets the revision (of the new head revision)
	 */
	private void commitInternal(HGPersistentHandle ontoHandle, String user, int revision, String comment)
	{
		clearWorkingSetConflicts();
		// assert revision > Pairs.getLast().GetFirst.GetRevision)
		// assert user != null
		// assert ontoHandle != null; pointin to onto.
		// asssert head change set not empty
		// assert head.getOntologyID.equals(ontohandle)
		Revision newRevision = new Revision();
		newRevision.setOntologyUUID(ontoHandle);
		newRevision.setRevision(revision);
		newRevision.setUser(user);
		newRevision.setRevisionComment(comment);
		// ChangeSet
		ChangeSet emptyCs = new ChangeSet();
		newRevision.setTimeStamp(emptyCs.getCreatedDate());
		HGHandle changeSetHandle = graph.add(emptyCs);
		//
		addPair(newRevision, changeSetHandle);
	}

	/**
	 * Adds a revision and changeset to the versionedOntology by creating a new
	 * Pair. Tests, if both are in the graph and throws exception if not.
	 * 
	 * 
	 * @param r
	 * @param cs
	 */
	private void addPair(Revision revision, HGHandle changesetHandle)
	{
		// HGHandle changesetHandle = graph.getHandle(changeset);
		if (changesetHandle == null)
			throw new IllegalArgumentException("Changeset must be in graph");
		Pair<Revision, HGHandle> pair = new Pair<Revision, HGHandle>(revision, changesetHandle);
		HGHandle pairHandle = graph.add(pair);
		revisionAndChangeSetPairs.add(pairHandle);
		// this link needs to be graph.updated now.
		if (graph.getHandle(this) != null)
		{
			graph.update(this);
		} // 2012.01.27 hilpold BIG PROBLEM WAS:
			// else {
			// graph.add(this);
			// }
	}

	/**
	 * Removes an empty WorkingSet Changeset from the graph and replaces it with
	 * the given changeset.
	 * 
	 * The old workingset changeset will be deleted from the graph only, if it
	 * is empty.
	 * 
	 * After this method gets called, the versioned ontology appears to have
	 * uncommitted changes. This versionedOntology will be graph.updated.
	 * 
	 * @param newChangeSetHandle
	 *            a handle pointing to a changeset.
	 */
	private void replaceWorkingChangeSet(HGHandle newChangeSetHandle, boolean enforceEmpty)
	{
		if (enforceEmpty && !getWorkingSetChanges().isEmpty())
			throw new IllegalStateException("The Workingchanges set must be empty.");
		HGHandle headPairHandle = revisionAndChangeSetPairs.get(revisionAndChangeSetPairs.size() - 1);
		Pair<Revision, HGHandle> headPair = graph.get(headPairHandle);
		Pair<Revision, HGHandle> newHeadPair = new Pair<Revision, HGHandle>(headPair.getFirst(), newChangeSetHandle);
		HGHandle newHeadPairHandle = graph.add(newHeadPair);
		// remove old
		revisionAndChangeSetPairs.set(revisionAndChangeSetPairs.size() - 1, newHeadPairHandle);
		HGHandle oldHead = headPair.getSecond();
		ChangeSet oldHeadChangeset = graph.get(oldHead);
		if (oldHeadChangeset.isEmpty())
		{
			graph.remove(headPair.getSecond(), true);
		} // else leave a non empty changset in the graph.
		graph.remove(headPairHandle, true);
		graph.update(this);
	}

	/**
	 * Removes the pair, linked changesets and changes from the graph.
	 * 
	 * <code>
	 * pairList.add(pairHandle --First--> Revision (Persistenthandle, int revision)
	 * 
	 *                         --Second-> changeSetHandle --> ChangeSet(empty));
	 * </code>
	 * 
	 * Should be called within HGTransaction.
	 */
	private void removePair(HGHandle pairHandle, boolean clearChangeSet)
	{
		Pair<Revision, HGHandle> pair = graph.get(pairHandle);
		// Revision will be removed with pair removal
		HGHandle changeSetHandle = pair.getSecond();
		ChangeSet changeSet = graph.get(changeSetHandle);
		// Clear changeset
		if (clearChangeSet)
		{
			changeSet.clear();
		}
		// graph.remove(changeSetHandle, true);
		graph.remove(pairHandle, true);
		graph.update(this);
	}

	/**
	 * Returns the first revision.
	 * 
	 * @return
	 */
	public Revision getBaseRevision()
	{
		return graph.getTransactionManager().ensureTransaction(new Callable<Revision>()
		{
			public Revision call()
			{
				return getRevision(0);
			}
		}, HGTransactionConfig.READONLY);
	}

	/**
	 * Returns the changeset that was created after(!) the given revision ID.
	 * (This is NOT the changeset that lead to the given revision.)
	 * 
	 * Should be called within HGTransaction.
	 * 
	 * @param rId
	 * @return the Changeset or null, if it does not exist.
	 */
	public ChangeSet getChangeSet(final RevisionID rId)
	{
		return graph.getTransactionManager().ensureTransaction(new Callable<ChangeSet>()
		{
			public ChangeSet call()
			{
				int i = indexOf(rId);
				if (i == -1)
					return null;
				HGHandle pairHandle = revisionAndChangeSetPairs.get(i);
				Pair<Revision, HGHandle> pair = graph.get(pairHandle);
				return graph.get(pair.getSecond());
			}
		}, HGTransactionConfig.READONLY);
	}

	/**
	 * Returns a list of changesets in the same order as revisions. The list,
	 * but not its entries may be modified without affecting the versioned
	 * ontology.
	 * 
	 * @return
	 */
	public List<ChangeSet> getChangeSets()
	{
		return graph.getTransactionManager().ensureTransaction(new Callable<List<ChangeSet>>()
		{
			public List<ChangeSet> call()
			{
				List<ChangeSet> returnedList = new ArrayList<ChangeSet>(revisionAndChangeSetPairs.size());
				for (HGHandle pairHandle : revisionAndChangeSetPairs)
				{
					Pair<Revision, HGHandle> pair = graph.get(pairHandle);
					returnedList.add(graph.<ChangeSet> get(pair.getSecond()));
				}
				return returnedList;
			}
		}, HGTransactionConfig.READONLY);
	}

	/**
	 * Should be called within HGTransaction.
	 * 
	 * @param rId
	 * @return
	 */
	private int indexOf(RevisionID rId)
	{
		for (int i = revisionAndChangeSetPairs.size() - 1; i >= 0; i--)
		{
			HGHandle pairHandle = revisionAndChangeSetPairs.get(i);
			Pair<Revision, HGHandle> pair = graph.get(pairHandle);
			if (rId.equals(pair.getFirst()))
			{
				return i;
			}
		}
		return -1;
	}

	// /**
	// * Deletes the last pair after applying an undo of all changes.
	// * This is only allowed if the workingset is empty or keepWorkingset is
	// true.
	// * <code>
	// *
	// * 1. reverse Apply previous change set cs'
	// * 2. clear cs'
	// * 3. Delete current head pair
	// *
	// * Head is now previous revision, data is before cs', cs' is head
	// changeset and empty.
	// * </code>
	// *
	// * Should be called within HGTransaction.
	// *
	// * @throws IllegalStateException, if current head changeset is not empty.
	// */
	// private void revertHeadToPreviousRevision(boolean keepWorkingSet) {
	// if (!getWorkingSetChanges().isEmpty() || keepWorkingSet) {
	// throw new
	// IllegalStateException("Need to rollback head before rolling back one revision or set keepWorkingset");
	// }
	// if (!(getNrOfRevisions() > 1)) {
	// throw new
	// IllegalStateException("Cannot roll back Head, because Head is Base.");
	// }
	// int indexPrevious = revisionAndChangeSetPairs.size() - 2;
	// ChangeSet workingSetChanges = getWorkingSetChanges();
	// HGDBOntology workingSetData = getWorkingSetData();
	// if (keepWorkingSet) {
	// workingSetChanges.reverseApplyTo(workingSetData);
	// }
	// ChangeSet preHeadCs = getChangeSet(indexPrevious);
	// preHeadCs.reverseApplyTo((OWLMutableOntology)getWorkingSetData());
	// preHeadCs.clear();
	// if (keepWorkingSet) {
	// workingSetChanges.applyTo(getWorkingSetData());
	// // Replace Changeset
	// HGHandle preHeadCsHandle = graph.getHandle(preHeadCs);
	// if (!graph.replace(preHeadCsHandle, workingSetChanges)) {
	// throw new
	// IllegalStateException("Could not replace old pre head changeset with previous workingSet");
	// }
	// }
	// // delete cur head, making prev cur.
	// HGHandle pairHandle =
	// revisionAndChangeSetPairs.remove(revisionAndChangeSetPairs.size() - 1);
	// removePair(pairHandle); //will graph.update this
	// }

	/**
	 * Rolls back changes in the current head changeset and clears it.
	 * 
	 * Should be called within HGTransaction.
	 */
	private void rollbackWorkingChangeSet()
	{
		int index = revisionAndChangeSetPairs.size() - 1;
		ChangeSet s = getChangeSet(index);
		clearWorkingSetConflicts();
		s.reverseApplyTo((OWLMutableOntology) getWorkingSetData(), getWorkingSetConflicts());
		s.clear(); // will graph.update
		// The head changeset is now empty and data represents state before
		// changes.
		// Conflicts will be clear.
	}

	/**
	 * Returns all Revisions ordered lowest/oldest revision first. The list, but
	 * not its entries may be modified without affecting the versioned ontology.
	 */
	public List<Revision> getRevisions()
	{
		return graph.getTransactionManager().ensureTransaction(new Callable<List<Revision>>()
		{
			public List<Revision> call()
			{
				List<Revision> returnedList = new ArrayList<Revision>(revisionAndChangeSetPairs.size());
				for (HGHandle pairHandle : revisionAndChangeSetPairs)
				{
					Pair<Revision, HGHandle> pair = graph.get(pairHandle);
					returnedList.add(pair.getFirst());
				}
				return returnedList;
			}
		}, HGTransactionConfig.READONLY);
	}

	/**
	 * The number of revisions, which is equal to the number of changesets.
	 * 
	 * @return the number of revisions and changesets.
	 */
	public int getNrOfRevisions()
	{
		return revisionAndChangeSetPairs.size();
	}

	/**
	 * Anonymous commit of current changeset resulting in a new revision.
	 */
	public void commit()
	{
		// if working changeset not empty, within one transaction
		// create and persist new Pair P'
		// newRevision = Revision + 1
		// timestamps new revision
		// setUser("Anonymous")
		// newChangeSet = EmptyChangeSet
		// add P' to end of list.
		//
		commit(Revision.USER_ANONYMOUS, Revision.REVISION_INCREMENT, null);
	}

	/**
	 * Commits all head changes and creates a new head revision with an empty
	 * change set.
	 * 
	 * @param user
	 */
	public void commit(String user, String comment)
	{
		commit(user, Revision.REVISION_INCREMENT, comment);
	}

	/**
	 * Commits all head changes and creates a new head revision with an empty
	 * change set.
	 * 
	 * This method ensures a HGTransaction.
	 * 
	 * @param revisionIncrement
	 */
	public void commit(final String user, final int revisionIncrement, final String comment)
	{
		graph.getTransactionManager().ensureTransaction(new Callable<Object>()
		{
			public Object call()
			{
				int headRevision = getHeadRevision().getRevision();
				commitInternal(getHeadRevision().getOntologyUUID(), user, headRevision + revisionIncrement, comment);
				return null;
			}
		});
	}

	/**
	 * Removes the last commit and makes all changes contained in the commit
	 * pending again. After calling this method the Versionedontology will have
	 * workingsetChanges. This should only be called right after a commit and
	 * must not be called if pending changes exist.
	 * 
	 * @throws IllegalStateException
	 *             if workingset changes exist.
	 * @throws IllegalStateException
	 *             if there is only the inital revision.
	 */
	public void undoCommit()
	{
		if (!getWorkingSetChanges().isEmpty())
			throw new IllegalStateException("Cannot undo because pending changes exist.");
		if (!(revisionAndChangeSetPairs.size() > 1))
			throw new IllegalStateException("Cannot undo because only the initial commit exist.");
		graph.getTransactionManager().ensureTransaction(new Callable<Object>()
		{
			public Object call()
			{
				HGHandle headAndPendingHandle = revisionAndChangeSetPairs.get(revisionAndChangeSetPairs.size() - 1);
				removePair(headAndPendingHandle, true);
				return null;
			}
		});
	}

	/**
	 * Undoes all changes in the current uncommitted working changeset, if any
	 * and re-intializes the changeset. Currently the current changeset must be
	 * the working changeset after head.
	 * 
	 * This method ensures a HGTransaction.
	 */
	public void rollback()
	{
		graph.getTransactionManager().ensureTransaction(new Callable<Object>()
		{
			public Object call()
			{
				rollbackWorkingChangeSet();
				return null;
			}
		});
	}

	/**
	 * The workingsetconflicts might change after calling this, if earlier
	 * additions or removals get removed, leading to a different state of the
	 * ontology before applying the workinsetchanges.
	 * 
	 * @param rId
	 * @param keepWorkingSet
	 */
	public void revertHeadTo(final RevisionID rId, final boolean keepWorkingSet)
	{
		final int revertToIndex = indexOf(rId);
		if (revertToIndex == -1)
			throw new IllegalStateException("Revert: No such revision: " + rId);
		if (!getWorkingSetChanges().isEmpty() && !keepWorkingSet)
			throw new IllegalStateException("Revert Error: Head changeset not empty, needs rollback or set keepWorkingSet.");
		if (revertToIndex == getNrOfRevisions() - 1)
		{
			System.err.println("RevertHeadTo called with last revision. Nothing to do. " + rId);
			return;
		}
		graph.getTransactionManager().ensureTransaction(new Callable<Object>()
		{
			public Object call()
			{
				// Roll back workingsetChanges, but keep them for reapplication
				// after removing Revisions.
				// reapplication will determine all conflicting changes.
				int workingSetChangesIndex = revisionAndChangeSetPairs.size() - 1;
				ChangeSet workingSetChanges = getWorkingSetChanges();
				HGDBOntology workingSetData = getWorkingSetData();
				SortedSet<Integer> workingSetConflicts = getWorkingSetConflicts();
				if (keepWorkingSet)
				{
					workingSetChanges.reverseApplyTo(workingSetData, workingSetConflicts);
				}
				// 2nd check, repeatable! could be -1 on repeat, but that's ok
				// below.
				// 1,C; 2,C; H,WSC WSC...WorkingSetChanges
				// 0 1 2 size: 3 => 2 calls
				ChangeSet preHeadCs = null;
				for (int curHeadIndex = workingSetChangesIndex; curHeadIndex > revertToIndex; curHeadIndex--)
				{
					int preHeadIndex = curHeadIndex - 1;
					preHeadCs = getChangeSet(preHeadIndex);
					preHeadCs.reverseApplyTo((OWLMutableOntology) getWorkingSetData());
					preHeadCs.clear();
					// delete cur head, making prev cur.
					HGHandle pairHandle = revisionAndChangeSetPairs.remove(curHeadIndex);
					boolean clearChangeSet = curHeadIndex < workingSetChangesIndex;
					removePair(pairHandle, clearChangeSet); // will graph.update
															// this
				}
				if (keepWorkingSet)
				{
					// Here might be a problem on remove changes in the
					// WorkinsetChanges.
					// Something that existed might not be in the changesetData
					// anymore
					// after the reverseApplication.
					SortedSet<Integer> newWorkingSetConflicts = workingSetChanges.applyTo(getWorkingSetData());
					// Replace Changeset
					HGHandle preHeadCsHandle = graph.getHandle(preHeadCs);
					if (!graph.replace(preHeadCsHandle, workingSetChanges))
					{
						throw new IllegalStateException("Could not replace old pre head changeset with previous workingSet");
					}
					setWorkingSetConflicts(newWorkingSetConflicts);
				}
				return null;
			}
		});
	}

	/**
	 * Undoes all changes from head to the given revision, deleting all
	 * changesets. The head changeset must be empty when calling this method.
	 * 
	 * This method ensures a HGTransaction.
	 * 
	 * @param rId
	 * @throws IllegalStateException
	 *             if working changeset has changes or rId not found.
	 */
	public void revertHeadTo(final RevisionID rId)
	{
		revertHeadTo(rId, false);
	}

	// /**
	// * Reverts head to the previous revision.
	// * Changes from previous to head will be applied inversely.
	// * WorkingChangeSet must be empty.
	// *
	// * This method ensures a HGTransaction.
	// */
	// public void revertHeadOneRevision() {
	// graph.getTransactionManager().ensureTransaction(new Callable<Object>() {
	// public Object call() {
	// rollbackHeadToPreviousRevision();
	// return null;
	// }});
	// }

	/**
	 * Adds one change to the current workingset. The change will be persisted
	 * instantly. If the change conflicts with
	 * 
	 * Should be called within HGTransaction.
	 * 
	 * @param vc
	 */
	void addAppliedChange(OWLOntologyChange owlChange)
	{
		// Changes are guaranteed to have been successfully applied to the
		// ontology.
		// see OWLOntologyChangeFilter
		VOWLChange vc = VOWLChangeFactory.create(owlChange, graph);
		getWorkingSetChanges().addChange(vc);
		// Not necessary:
		// //See, if new change is a conflict
		// if (owlChange.accept(conflictDetector)) {
		// addWorkingSetConflict(getWorkingSetChanges().size() - 1);
		// }
	}

	// private void addWorkingSetConflict(int index){
	// if (!workingSetConflicts.add(index)) {
	// System.err.println("" + this + " already contained ws conflict at " +
	// index);
	// } else {
	// graph.update(this);
	// }
	// }

	/**
	 * Adds a delta to the versionedOntology and applies it to headRevisionData.
	 * (Delta is typically received by a push or pull operation.)
	 * 
	 * Changesets, changes should be added to the graph before. Revisions must
	 * not have been added to the graph. Preconditions: 1) this must be stored
	 * in the graph 2) revisions must not, changesets and dependents must be in
	 * the graph. 3) the first revision in the list must match the current head
	 * revision 4) the existing current workingset must be empty, unless
	 * mergeWithUncommitted is set.
	 * 
	 * Postconditions: 1) the versionedontology will have all given revisions,
	 * except the matching first and all changesets without exceptions added.
	 * The first changeset will replace the current 2) the workingset will be a
	 * new empty changeset, unless mergeWithUncommitted is set where it will be
	 * the original workingset changeset. 3) all revisions/changesets will be
	 * stored in the graph as pairs and the versionedontology will be updated.
	 * 
	 * @param revisions
	 *            a list of revisions, where the first matches the existing head
	 *            revision.
	 * @param changeSets
	 *            a list of changesets, where the last empty workingset is not
	 *            provided.
	 * @param mergeWithUncommitted
	 *            if true, the method allows the workingset changeset to have
	 *            changes and merges them with the applied delta.
	 */
	public void addApplyDelta(List<Revision> revisions, List<ChangeSet> changeSets, boolean mergeWithUncommitted)
	{
		if (revisions.size() != changeSets.size() + 1)
			throw new IllegalArgumentException("There must be one more revision than changesets.");
		if (!getHeadRevision().equals(revisions.get(0)))
			throw new IllegalArgumentException("The first revision must match the current head.");
		if (graph.getHandle(this) == null)
			throw new IllegalStateException("The versioned ontology must be stored in the graph.");
		if (!getWorkingSetChanges().isEmpty() && !mergeWithUncommitted)
			throw new IllegalStateException("The workingset must be empty for changes to be applied without merge parameter set.");
		List<HGHandle> changeSetHandles = new LinkedList<HGHandle>();
		for (ChangeSet c : changeSets)
		{
			HGHandle csHandle = graph.getHandle(c);
			if (csHandle == null)
				throw new IllegalArgumentException("Changeset number " + changeSets.indexOf(c) + " not in graph.");
			changeSetHandles.add(csHandle);
		}
		// TRANSACTION:
		// 1) Apply changes
		// 2) Add revision and changeset pair, except first.
		HGDBOntology headData = getWorkingSetData();
		ChangeSet workingsetChanges = getWorkingSetChanges();
		// ROLLBACK FOR MERGE
		if (mergeWithUncommitted)
		{
			workingsetChanges.reverseApplyTo(headData);
		}
		for (int i = 0; i < revisions.size(); i++)
		{
			// TRANSACTION START
			Revision curR = revisions.get(i);
			ChangeSet curCS;
			HGHandle curCSHandle;
			SortedSet<Integer> conflicts;
			if (i < changeSets.size())
			{
				curCS = changeSets.get(i);
				curCSHandle = changeSetHandles.get(i);
			}
			else if (i == changeSets.size())
			{
				if (!mergeWithUncommitted)
				{
					curCS = new ChangeSet();
					curCS.setCreatedDate(new Date());
					curCSHandle = graph.add(curCS);
				}
				else
				{
					// REAPPLY after earlier rollback
					curCS = workingsetChanges;
					curCSHandle = graph.getHandle(workingsetChanges);
					if (curCSHandle == null)
						throw new IllegalArgumentException("We are reusing, it has to be there.");
				}
			}
			else
			{
				throw new IllegalStateException("More revisions than changesets + 1. This should be unreachable code.");
			}
			if (!curCS.isEmpty())
			{
				// apply the current changeset to our ontology (head revision
				// data)
				// TODO: we might want to hash the current onto to have content
				// adressible
				conflicts = curCS.applyTo(headData);
			}
			else
			{
				conflicts = new TreeSet<Integer>();
			}
			if (i == changeSets.size())
			{
				// Set working set conflicts after reapplication of workingset
				// changes or ensure empty for new empty ws changeset.
				setWorkingSetConflicts(conflicts);
			}
			if (i == 0)
			{
				if (!getHeadRevision().equals(curR))
					throw new IllegalStateException("headrevision does not match first revision (anymore)");
				replaceWorkingChangeSet(curCSHandle, !mergeWithUncommitted);
			}
			else
			{
				addPair(curR, curCSHandle);
			}
			// TRANSACTION END
		}
	}

	/**
	 * Removes all revisions and changesets without modifying head revision
	 * data. The versioned ontology may be removed after this operation.
	 * 
	 * Should be called within HGTransaction.
	 * 
	 */
	void clear()
	{
		List<HGHandle> revisionAndChangeSetPairsCopy = new ArrayList<HGHandle>(revisionAndChangeSetPairs);
		for (int i = 0; i < revisionAndChangeSetPairsCopy.size(); i++)
		{
			HGHandle pairHandle = revisionAndChangeSetPairsCopy.get(i);
			removePair(pairHandle, true);
		}
		// assert revisionAndChangeSetPairs.isEmpty()
		if (!revisionAndChangeSetPairs.isEmpty())
			throw new IllegalStateException("List expected to be empty.");
		// Will be empty revisionAndChangeSetPairs.clear();
		// graph.update(this);
	}

	//
	// HELPERS
	//

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.hypergraphdb.HGGraphHolder#setHyperGraph(org.hypergraphdb.HyperGraph)
	 */
	@Override
	@HGIgnore
	public void setHyperGraph(HyperGraph graph)
	{
		this.graph = graph;
	}

	@HGIgnore
	public HyperGraph getHyperGraph()
	{
		return graph;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.hypergraphdb.HGLink#getArity()
	 */
	@Override
	public int getArity()
	{
		return revisionAndChangeSetPairs.size();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.hypergraphdb.HGLink#getTargetAt(int)
	 */
	@Override
	public HGHandle getTargetAt(int i)
	{
		return revisionAndChangeSetPairs.get(i);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.hypergraphdb.HGLink#notifyTargetHandleUpdate(int,
	 * org.hypergraphdb.HGHandle)
	 */
	@Override
	public void notifyTargetHandleUpdate(int i, HGHandle handle)
	{
		revisionAndChangeSetPairs.set(i, handle);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.hypergraphdb.HGLink#notifyTargetRemoved(int)
	 */
	@Override
	public void notifyTargetRemoved(int i)
	{
		revisionAndChangeSetPairs.remove(i);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.hypergraphdb.app.owl.versioning.VersioningObject#accept(org.hypergraphdb
	 * .app.owl.versioning.VOWLObjectVisitor)
	 */
	@Override
	public void accept(VOWLObjectVisitor visitor)
	{
		visitor.visit(this);
	}

	public String toString()
	{
		String OID = getWorkingSetData().getOntologyID().toString();
		String headRevision = "" + getHeadRevision().toString();
		return OID + " Head: " + headRevision + "(Versioned Revs: " + getNrOfRevisions() + ")";
	}
}