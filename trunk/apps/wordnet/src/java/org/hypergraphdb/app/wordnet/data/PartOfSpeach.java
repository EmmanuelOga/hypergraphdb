package org.hypergraphdb.app.wordnet.data;/**
 * Instances of this class enumerate the possible major syntactic categories, or
 * <b>P</b>art's <b>O</b>f <b>S</b>peech. Each <code>POS</code> has a human-readable
 * label that can be used to print it.
 */
public enum PartOfSpeach 
{
	noun("NOUN"),
	verb("VERB"),
	adjective("ADJECTIVE"),
	adverb("ADVERB");
	
	private String label;
	
	private PartOfSpeach(String label)
	{
		this.label = label;
	}
	
	public static PartOfSpeach get(String label)
	{
		if (noun.label.equals(label))
			return noun;
		else if (verb.label.equals(label))
			return verb;
		else if (adjective.label.equals(label))
			return adjective;
		else if (adverb.label.equals(label))
			return adverb;
		else 
			return null;
	}
}