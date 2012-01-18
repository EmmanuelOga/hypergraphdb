package org.hypergraphdb.app.owl.versioning.change;

import org.hypergraphdb.HGHandle;

/**
 * ImportChange.
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Jan 13, 2012
 */
public abstract class ImportChange extends VOWLChange {
	
	private HGHandle importDeclarationHandle;
	
	public ImportChange(HGHandle...args) {
		importDeclarationHandle = args[0];
    }
	
	HGHandle getImportDeclaration() {
		return importDeclarationHandle;
	}

	/* (non-Javadoc)
	 * @see org.hypergraphdb.HGLink#getArity()
	 */
	@Override
	public int getArity() {		
		return (importDeclarationHandle == null)? 0:1;
	}

	/* (non-Javadoc)
	 * @see org.hypergraphdb.HGLink#getTargetAt(int)
	 */
	@Override
	public HGHandle getTargetAt(int i) {
		if (!(i >= 0 && i < getArity())) throw new IllegalArgumentException("Index has to be >= 0 and less than " + getArity());
		return importDeclarationHandle;
	}

	/* (non-Javadoc)
	 * @see org.hypergraphdb.HGLink#notifyTargetHandleUpdate(int, org.hypergraphdb.HGHandle)
	 */
	@Override
	public void notifyTargetHandleUpdate(int i, HGHandle handle) {
		if (!(i >= 0 && i < getArity())) throw new IllegalArgumentException("Index has to be >= 0 and less than " + getArity());
		importDeclarationHandle = handle;
	}

	/* (non-Javadoc)
	 * @see org.hypergraphdb.HGLink#notifyTargetRemoved(int)
	 */
	@Override
	public void notifyTargetRemoved(int i) {
		if (!(i >= 0 && i < getArity())) throw new IllegalArgumentException("Index has to be >= 0 and less than " + getArity());
		importDeclarationHandle = null;
	}

}
