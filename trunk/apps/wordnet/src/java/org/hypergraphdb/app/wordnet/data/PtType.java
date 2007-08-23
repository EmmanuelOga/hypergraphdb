package org.hypergraphdb.app.wordnet.data;

import java.io.Serializable;
import java.util.*;

import org.hypergraphdb.app.wordnet.data.pointers.AlsoSee;
import org.hypergraphdb.app.wordnet.data.pointers.Antonym;
import org.hypergraphdb.app.wordnet.data.pointers.Attribute;
import org.hypergraphdb.app.wordnet.data.pointers.Category;
import org.hypergraphdb.app.wordnet.data.pointers.CategoryMember;
import org.hypergraphdb.app.wordnet.data.pointers.Cause;
import org.hypergraphdb.app.wordnet.data.pointers.Derived;
import org.hypergraphdb.app.wordnet.data.pointers.EntailedBy;
import org.hypergraphdb.app.wordnet.data.pointers.Entailment;
import org.hypergraphdb.app.wordnet.data.pointers.Hypernym;
import org.hypergraphdb.app.wordnet.data.pointers.Hyponim;
import org.hypergraphdb.app.wordnet.data.pointers.InstanceHypernym;
import org.hypergraphdb.app.wordnet.data.pointers.InstanceHyponym;
import org.hypergraphdb.app.wordnet.data.pointers.MemberHolonym;
import org.hypergraphdb.app.wordnet.data.pointers.MemberMeronym;
import org.hypergraphdb.app.wordnet.data.pointers.Nominalization;
import org.hypergraphdb.app.wordnet.data.pointers.PartHolonym;
import org.hypergraphdb.app.wordnet.data.pointers.PartMeronym;
import org.hypergraphdb.app.wordnet.data.pointers.ParticipleOf;
import org.hypergraphdb.app.wordnet.data.pointers.Pertainym;
import org.hypergraphdb.app.wordnet.data.pointers.Region;
import org.hypergraphdb.app.wordnet.data.pointers.RegionMember;
import org.hypergraphdb.app.wordnet.data.pointers.Similar;
import org.hypergraphdb.app.wordnet.data.pointers.SubstanceHolonym;
import org.hypergraphdb.app.wordnet.data.pointers.SubstanceMeronym;
import org.hypergraphdb.app.wordnet.data.pointers.Usage;
import org.hypergraphdb.app.wordnet.data.pointers.UsageMember;
import org.hypergraphdb.app.wordnet.data.pointers.VerbGroup;

/**
 * Instances of this class enumerate the possible WordNet pointer types,
 * and are used to label <code>PtType</code>s. Each <code>PtType</code>
 * carries additional information: a human-readable label, an optional reflexive
 * type that labels links pointing the opposite direction, an encoding of
 * parts-of-speech that it applies to, and a short string that represents it in
 * the dictionary files.
 */
public final class PtType implements Serializable {
	static final long serialVersionUID = 220886251671304256L;
	// Flags for tagging a pointer type with the POS types it apples to.
	private static final int N = 1;
	private static final int V = 2;
	private static final int ADJ = 4;
	private static final int ADV = 8;
	private static final int LEXICAL = 16;

	// All categories
	public static final PtType ANTONYM = 
		new PtType(Antonym.class, "ANTONYM", "ANTONYM_KEY", N | V | ADJ | ADV | LEXICAL);
    public static final PtType CATEGORY = 
    	new PtType(Category.class, "CATEGORY_DOMAIN", "CATEGORY_DOMAIN_KEY", N | V | ADJ | ADV | LEXICAL);
    public static final PtType REGION = 
    	new PtType(Region.class, "REGION_DOMAIN", "REGION_DOMAIN_KEY", N | V | ADJ | ADV | LEXICAL);
    public static final PtType USAGE = 
    	new PtType(Usage.class, "USAGE_DOMAIN", "USAGE_DOMAIN_KEY", N | V | ADJ | ADV | LEXICAL);

	// Nouns and Verbs

	public static final PtType HYPERNYM = 
		new PtType(Hypernym.class, "HYPERNYM", "HYPERNYM_KEY", N | V);
	public static final PtType HYPONYM = 
		new PtType(Hyponim.class, "HYPONYM", "HYPONYM_KEY", N | V);
    public static final PtType NOMINALIZATION = 
    	new PtType(Nominalization.class, "NOMINALIZATION", "NOMINALIZATION_KEY", N | V);

	// Nouns and Adjectives

	public static final PtType ATTRIBUTE = 
		new PtType(Attribute.class, "ATTRIBUTE", "ATTRIBUTE_KEY", N | ADJ);
	public static final PtType SEE_ALSO = 
		new PtType(AlsoSee.class, "ALSO_SEE", "ALSO_SEE_KEY", N | V | ADJ | LEXICAL);

    // Nouns

    public static final PtType MEMBER_HOLONYM = 
    	new PtType(MemberHolonym.class, "MEMBER_HOLONYM", "MEMBER_HOLONYM_KEY", N);
    public static final PtType SUBSTANCE_HOLONYM = 
    	new PtType(SubstanceHolonym.class, "SUBSTANCE_HOLONYM", "SUBSTANCE_HOLONYM_KEY", N);
    public static final PtType PART_HOLONYM = 
    	new PtType(PartHolonym.class, "PART_HOLONYM", "PART_HOLONYM_KEY", N);
    public static final PtType MEMBER_MERONYM = 
    	new PtType(MemberMeronym.class, "MEMBER_MERONYM", "MEMBER_MERONYM_KEY", N);
    public static final PtType SUBSTANCE_MERONYM = 
    	new PtType(SubstanceMeronym.class, "SUBSTANCE_MERONYM", "SUBSTANCE_MERONYM_KEY", N);
    public static final PtType PART_MERONYM = 
    	new PtType(PartMeronym.class, "PART_MERONYM", "PART_MERONYM_KEY", N);
    public static final PtType CATEGORY_MEMBER = 
    	new PtType(CategoryMember.class, "CATEGORY_MEMBER", "CATEGORY_MEMBER_KEY", N);
    public static final PtType REGION_MEMBER = 
    	new PtType(RegionMember.class, "REGION_MEMBER", "REGION_MEMBER_KEY", N);
    public static final PtType USAGE_MEMBER = 
    	new PtType(UsageMember.class, "USAGE_MEMBER", "USAGE_MEMBER_KEY", N);

    //since WordNet 2.1
    public static final PtType INSTANCE_HYPERNYM = 
    	new PtType(InstanceHypernym.class, "INSTANCE_HYPERNYM", "INSTANCE_HYPERNYM_KEY", N);
    public static final PtType INSTANCE_HYPONYM = 
    	new PtType(InstanceHyponym.class, "INSTANCE_HYPONYM", "INSTANCE_HYPONYM_KEY", N);
  
	// Verbs

	public static final PtType ENTAILMENT = 
		new PtType(Entailment.class, "ENTAILMENT", "ENTAILMENT_KEY", V);
	public static final PtType ENTAILED_BY = 
		new PtType(EntailedBy.class, "ENTAILED_BY", "ENTAILED_BY_KEY", V);
	public static final PtType CAUSE = 
		new PtType(Cause.class, "CAUSE", "CAUSE_KEY", V);
	public static final PtType VERB_GROUP = 
		new PtType(VerbGroup.class, "VERB_GROUP", "VERB_GROUP_KEY", V);

	// Adjectives
	public static final PtType SIMILAR_TO = 
		new PtType(Similar.class, "SIMILAR", "SIMILAR_KEY", ADJ);
	public static final PtType PARTICIPLE_OF = 
		new PtType(ParticipleOf.class, "PARTICIPLE_OF", "PARTICIPLE_OF_KEY", ADJ | LEXICAL);
	public static final PtType PERTAINYM = 
		new PtType(Pertainym.class, "PERTAINYM", "PERTAINYM_KEY", ADJ | LEXICAL);

	// Adverbs
	public static final PtType DERIVED = 
		new PtType(Derived.class, "DERIVED", "DERIVED_KEY", ADV);

	/** A list of all <code>PtType</code>s. */
	private static final List ALL_TYPES = Collections.unmodifiableList(Arrays.asList(new PtType[] {
        ANTONYM, HYPERNYM, HYPONYM, ATTRIBUTE, SEE_ALSO, ENTAILMENT, ENTAILED_BY, CAUSE, VERB_GROUP,
        MEMBER_MERONYM, SUBSTANCE_MERONYM, PART_MERONYM, MEMBER_HOLONYM, SUBSTANCE_HOLONYM, PART_HOLONYM,
        SIMILAR_TO, PARTICIPLE_OF, DERIVED, NOMINALIZATION, CATEGORY, REGION, USAGE, CATEGORY_MEMBER,
        REGION_MEMBER, USAGE_MEMBER,INSTANCE_HYPERNYM, INSTANCE_HYPONYM 
    }));

    private static final Map POS_TO_MASK_MAP = new HashMap();
    private static final Map KEY_TO_POINTER_TYPE_MAP = new HashMap();
    private static final Map CLASS_TO_POINTER_TYPE_MAP = new HashMap();

    private static boolean _initialized = false;

    public static void initialize() {
        if (!_initialized) {
            POS_TO_MASK_MAP.put(POS.NOUN, new Integer(N));
            POS_TO_MASK_MAP.put(POS.VERB, new Integer(V));
            POS_TO_MASK_MAP.put(POS.ADJECTIVE, new Integer(ADJ));
            POS_TO_MASK_MAP.put(POS.ADVERB, new Integer(ADV));

            for (Iterator itr = ALL_TYPES.iterator(); itr.hasNext();) {
                PtType pt = (PtType) itr.next();
                KEY_TO_POINTER_TYPE_MAP.put(pt.getKey(), pt);
                CLASS_TO_POINTER_TYPE_MAP.put(pt.getClazz(), pt);
            }

            _initialized = true;
        }
    }

    static {
		setSymmetric(ANTONYM, ANTONYM);
		setSymmetric(HYPERNYM, HYPONYM);
		setSymmetric(MEMBER_MERONYM, MEMBER_HOLONYM);
		setSymmetric(SUBSTANCE_MERONYM, SUBSTANCE_HOLONYM);
		setSymmetric(PART_MERONYM, PART_HOLONYM);
		setSymmetric(SIMILAR_TO, SIMILAR_TO);
        setSymmetric(ATTRIBUTE, ATTRIBUTE);
        setSymmetric(VERB_GROUP, VERB_GROUP);
		setSymmetric(ENTAILMENT, ENTAILED_BY);
        setSymmetric(CATEGORY, CATEGORY_MEMBER);
        setSymmetric(REGION, REGION_MEMBER);
        setSymmetric(USAGE,  USAGE_MEMBER);
        setSymmetric(NOMINALIZATION, NOMINALIZATION);
	}

    /**
     * Returns true if <var>type</var> is a symmetric pointer type
     * (it is its own symmetric type).
     */
    public static boolean isSymmetric(PtType type) {
        return type.symmetricTo(type);
    }

    /**
	 * Return the <code>PtType</code> whose key matches <var>key</var>.
	 */
    public static PtType getPointerTypeForKey(String key) {
		return (PtType) KEY_TO_POINTER_TYPE_MAP.get(key);
	}
    
	public static PtType getPointerTypeForClass(Class key) {
		return (PtType) CLASS_TO_POINTER_TYPE_MAP.get(key);
	}

    public static List getAllPointerTypes() {
        return ALL_TYPES;
    }

    public static List getAllPointerTypesForPOS(POS pos) {
        List types = new ArrayList();
        for (Iterator itr = ALL_TYPES.iterator(); itr.hasNext();) {
            PtType pt = (PtType) itr.next();
            if (pt.appliesTo(pos)) {
                types.add(pt);
            }
        }
        return Collections.unmodifiableList(types);
    }

	/** Set <var>a</var> as <var>b</var>'s symmetric type, and vice versa. */
	private static void setSymmetric(PtType a, PtType b) {
		a._symmetricType = b;
		b._symmetricType = a;
	}

	private static int getPOSMask(POS pos) {
		return ((Integer) POS_TO_MASK_MAP.get(pos)).intValue();
	}

	private Resolvable _label;
	private Resolvable _key;
	private int _flags;
	/** The PtType that is the revers of this PtType */
	private PtType _symmetricType;
	private Class clazz;

	private PtType(Class clazz, String label, String key, int flags) {
		_label = new Resolvable(label);
		_key = new Resolvable(key);
		_flags = flags;
		this.setClazz(clazz);
	}

	private transient String _cachedToString = null;

	public String toString() {
		if (_cachedToString == null) {
            _cachedToString = StringFormatter.resolveMessage(
            		"DATA_TOSTRING_011", new Object[]{getLabel(), getKey(), getFlagsAsString()});
        }
		return _cachedToString;
	}

	public String getKey() {
		return _key.toString();
	}

	public String getLabel() {
		return _label.toString();
	}

	private void setClazz(Class clazz) {
		this.clazz = clazz;
	}

	public Class getClazz() {
		return clazz;
	}

	/** Whether or not this PtType can be associated with <code>pos</code> */
	public boolean appliesTo(POS pos) {
		return (_flags & getPOSMask(pos)) != 0;
	}

	public boolean isSymmetric() {
		return symmetricTo(this);
	}

	/** Returns true if <var>type</var> is symmetric to this pointer type. */
	public boolean symmetricTo(PtType type) {
		return getSymmetricType() != null && getSymmetricType().equals(type);
	}

	/** Returns the pointer type that is symmetric to this type. */
	public PtType getSymmetricType() {
		return _symmetricType;
	}

	public int hashCode() {
		return getLabel().hashCode();
	}

	private String _flagStringCache = null;

	private String getFlagsAsString() {
		if (_flagStringCache == null) {
			String str = "";
			if ((_flags & N) != 0) str += StringFormatter.resolveMessage("NOUN") + ", ";
			if ((_flags & V) != 0) str += StringFormatter.resolveMessage("VERB") + ", ";
			if ((_flags & ADJ) != 0) str += StringFormatter.resolveMessage("ADJECTIVE") + ", ";
			if ((_flags & ADV) != 0) str += StringFormatter.resolveMessage("ADVERB") + ", ";
			if ((_flags & LEXICAL) != 0) str += StringFormatter.resolveMessage("LEXICAL") + ", ";
			_flagStringCache = str.substring(0, str.length() - 2);
		}
		return _flagStringCache;
	}
}