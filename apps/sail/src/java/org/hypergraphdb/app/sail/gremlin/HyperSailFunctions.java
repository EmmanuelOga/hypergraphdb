package org.hypergraphdb.app.sail.gremlin;

import com.tinkerpop.gremlin.functions.FunctionHelper;
import com.tinkerpop.gremlin.models.ggm.Graph;
import com.tinkerpop.gremlin.models.ggm.impls.sail.SailGraph;
import com.tinkerpop.gremlin.statements.EvaluationException;
import org.apache.commons.jxpath.ExpressionContext;
import org.apache.commons.jxpath.Function;
import org.apache.commons.jxpath.Functions;
import org.hypergraphdb.app.sail.HyperGraphStore;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


/**
 * created Feb 5, 2010  - 9:37:19 AM
 *
 * @author IanHolsman
 *         Copyright (C) 2010 by Aol. All Rights Reserved.
 */
public class HyperSailFunctions implements Functions {
  public static final String NAMESPACE_PREFIX = "hyperSail";

  private static Set<String> namespaces = new HashSet<String>();
  private static Map<String, Function> functionMap = new HashMap<String, Function>();

  static {
    namespaces.add(NAMESPACE_PREFIX);
    functionMap.put("open", new Open());
  }

  public Function getFunction(final String namespace, final String name, final Object[] parameters) {
    return functionMap.get(name);
  }

  public Set getUsedNamespaces() {
    return namespaces;
  }

  private static class Open implements Function {
    public static final String FUNCTION_NAME = "open";

    public Graph invoke(final ExpressionContext context, final Object[] parameters) {

      if (null != parameters && parameters.length == 1) {
        Object object = FunctionHelper.nodeSetConversion(parameters[0]);
        if (object instanceof String) {
          return new SailGraph(new HyperGraphStore((String) object));
        }
      }
      throw EvaluationException.createException(FunctionHelper.makeFunctionName(NAMESPACE_PREFIX, FUNCTION_NAME),
          EvaluationException.EvaluationErrorType.UNSUPPORTED_PARAMETERS);

    }
  }


}
