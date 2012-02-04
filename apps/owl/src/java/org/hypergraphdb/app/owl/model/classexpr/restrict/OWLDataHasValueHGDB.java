package org.hypergraphdb.app.owl.model.classexpr.restrict;

import org.hypergraphdb.HGHandle;
import org.semanticweb.owlapi.model.ClassExpressionType;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLClassExpressionVisitor;
import org.semanticweb.owlapi.model.OWLClassExpressionVisitorEx;
import org.semanticweb.owlapi.model.OWLDataHasValue;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLDataRange;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLObjectVisitor;
import org.semanticweb.owlapi.model.OWLObjectVisitorEx;

/**
 * OWLDataHasValueHGDB.
 * 
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Oct 19, 2011
 */
public class OWLDataHasValueHGDB extends OWLValueRestrictionHGDB<OWLDataRange, OWLDataPropertyExpression, OWLLiteral>
		implements OWLDataHasValue {

	
	/**
	 * @param args [0]...property, [1]...filler
	 */
    public OWLDataHasValueHGDB(HGHandle... args) {
    	super(args[0], args[1]);
    	if (args.length != 2) throw new IllegalArgumentException("Must be exactly 2 handles.");
    }
	
    public OWLDataHasValueHGDB(HGHandle property, HGHandle value) {
    	//TODO check types: OWLDataPropertyExpression property, OWLLiteral value
        super(property, value);
    }

	/**
	 * Gets the class expression type for this class expression
	 * 
	 * @return The class expression type
	 */
	public ClassExpressionType getClassExpressionType() {
		return ClassExpressionType.DATA_HAS_VALUE;
	}

	public boolean isObjectRestriction() {
		return false;
	}

	public boolean isDataRestriction() {
		return true;
	}

	@Override
	public boolean equals(Object obj) {
		if (super.equals(obj)) {
			return obj instanceof OWLDataHasValue;
		}
		return false;
	}

	public OWLClassExpression asSomeValuesFrom() {
		return getOWLDataFactory().getOWLDataSomeValuesFrom(getProperty(),
				getOWLDataFactory().getOWLDataOneOf(getValue()));
	}

	public void accept(OWLClassExpressionVisitor visitor) {
		visitor.visit(this);
	}

	public void accept(OWLObjectVisitor visitor) {
		visitor.visit(this);
	}

	public <O> O accept(OWLClassExpressionVisitorEx<O> visitor) {
		return visitor.visit(this);
	}

	public <O> O accept(OWLObjectVisitorEx<O> visitor) {
		return visitor.visit(this);
	}
}
