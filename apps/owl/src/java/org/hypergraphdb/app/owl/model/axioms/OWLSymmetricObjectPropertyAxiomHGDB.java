package org.hypergraphdb.app.owl.model.axioms;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.hypergraphdb.HGHandle;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAxiomVisitor;
import org.semanticweb.owlapi.model.OWLAxiomVisitorEx;
import org.semanticweb.owlapi.model.OWLObjectVisitor;
import org.semanticweb.owlapi.model.OWLObjectVisitorEx;
import org.semanticweb.owlapi.model.OWLSubObjectPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSymmetricObjectPropertyAxiom;

/**
 * OWLSymmetricObjectPropertyAxiomHGDB.
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Nov 4, 2011
 */
public class OWLSymmetricObjectPropertyAxiomHGDB extends OWLObjectPropertyCharacteristicAxiomHGDB implements OWLSymmetricObjectPropertyAxiom {

    public OWLSymmetricObjectPropertyAxiomHGDB(HGHandle...args) {
    	this(args[0], Collections.<OWLAnnotation>emptySet());
    	if (args[0] == null) throw new IllegalArgumentException("args[0] was null");
    }

	public OWLSymmetricObjectPropertyAxiomHGDB(HGHandle property, Collection<? extends OWLAnnotation> annotations) {
    	//OWLObjectPropertyExpression property, Collection<? extends OWLAnnotation> annotations
        super(property, annotations);
    	if (property == null) throw new IllegalArgumentException("property was null");                
    }

    public Set<OWLSubObjectPropertyOfAxiom> asSubPropertyAxioms() {
        Set<OWLSubObjectPropertyOfAxiom> result = new HashSet<OWLSubObjectPropertyOfAxiom>(5);
        result.add(getOWLDataFactory().getOWLSubObjectPropertyOfAxiom(getProperty(), getProperty().getInverseProperty().getSimplified()));
        result.add(getOWLDataFactory().getOWLSubObjectPropertyOfAxiom(getProperty().getInverseProperty().getSimplified(), getProperty()));
        return result;
    }

    public OWLSymmetricObjectPropertyAxiom getAnnotatedAxiom(Set<OWLAnnotation> annotations) {
        return getOWLDataFactory().getOWLSymmetricObjectPropertyAxiom(getProperty(), mergeAnnos(annotations));
    }

    public OWLSymmetricObjectPropertyAxiom getAxiomWithoutAnnotations() {
        if (!isAnnotated()) {
            return this;
        }
        return getOWLDataFactory().getOWLSymmetricObjectPropertyAxiom(getProperty());
    }

    @Override
	public boolean equals(Object obj) {
        if (super.equals(obj)) {
            return obj instanceof OWLSymmetricObjectPropertyAxiom;
        }
        return false;
    }

    public void accept(OWLAxiomVisitor visitor) {
        visitor.visit(this);
    }

    public void accept(OWLObjectVisitor visitor) {
        visitor.visit(this);
    }

    public <O> O accept(OWLAxiomVisitorEx<O> visitor) {
        return visitor.visit(this);
    }

    public <O> O accept(OWLObjectVisitorEx<O> visitor) {
        return visitor.visit(this);
    }

    public AxiomType<?> getAxiomType() {
        return AxiomType.SYMMETRIC_OBJECT_PROPERTY;
    }
}