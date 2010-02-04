package org.hypergraphdb.app.sail.model;

import org.openrdf.model.Resource;

/**
 * created Jan 29, 2010  - 3:34:19 PM
 *
 * @author IanHolsman
 *         Copyright (C) 2010 by Aol. All Rights Reserved.
 */
public class HyperContext implements Resource {
  String context;

  public HyperContext() {
  }

  public HyperContext(String val) {
    this.context = val;
  }

  public HyperContext(Resource r) {
    if (r == null) {
      this.context = null;
    } else {
      this.context = r.stringValue();
    }
  }

  public String getVal() {

    return context;
  }

  public String getContext() {

    return context;
  }

  public void setVal(String val) {
    this.context = val;
  }

  public void setContext(String val) {
    this.context = val;
  }

  public String stringValue() {
    return context;
  }

  @Override
  public String toString() {
    return this.stringValue();
  }
  

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof HyperContext)) return false;

    HyperContext that = (HyperContext) o;
    if ( context == null && that.context == null) return true;
    if (!context.equals(that.context)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    if ( context == null) {
      return 0;
    }
    return context.hashCode();
  }
}
