package org.hypergraphdb.app.wordnet.data;

import java.io.Serializable;

/** Implements lazy resolving for a resource key */
public class Resolvable implements Serializable {
	static final long serialVersionUID = 4753740475813500883L;

	private String unresolved = null;
	private transient String _resolved = null;

	public Resolvable() {}
	
	public Resolvable(String msg) {
		unresolved = msg;
	}

	public String toString() {
		if (_resolved == null)
			_resolved = StringFormatter.resolveMessage(unresolved);
		return _resolved;
	}

	public String getUnresolved() {
		return unresolved;
	}

	public void setUnresolved(String unresolved) {
		this.unresolved = unresolved;
	}
}
