package org.hypergraphdb.app.wordnet;

import java.util.Locale;
import java.util.ResourceBundle;

public class StringFormatter {

	private static final String CORE_RESOURCE =	"org/hypergraphdb/app/wordnet/HGWN";
	private static ResourceBundle _bundle = 
		ResourceBundle.getBundle(CORE_RESOURCE, new Locale("en", ""));
            
	private StringFormatter(){}
	
	/**
	 * Resolve <var>msg</var> in one of the resource bundles used by the system
	 * @param params parameters to insert into the resolved message
	 */
	public static String resolveMessage(String msg, Object[] params) {
		return insertParams(_bundle.getString(msg), params);
	}
	
	public static String resolveMessage(String msg)
	{
		//System.out.println("Message: " + msg);
		return _bundle.getString(msg);
	}
	
	public static String resolveMessage(String msg, Object obj) {
		return resolveMessage(_bundle.getString(msg), new Object[]{obj});
	}

	private static String insertParams(String str, Object[] params) {
		StringBuffer buf = new StringBuffer();
		int startIndex = 0;
		for (int i = 0; i < params.length && startIndex <= str.length(); i++) {
			int endIndex = str.indexOf("{" + i, startIndex);
			if (endIndex != -1) {
				buf.append(str.substring(startIndex, endIndex));
				buf.append(params[i] == null ? null : params[i].toString());
				startIndex = endIndex + 3;
			}
		}
		buf.append(str.substring(startIndex, str.length()));
		return buf.toString();
	}
}
