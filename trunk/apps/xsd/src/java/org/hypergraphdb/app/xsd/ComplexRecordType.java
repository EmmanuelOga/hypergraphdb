/*
 * This file is part of the HyperGraphDB source distribution. This is copyrighted
 * software. For permitted uses, licensing options and redistribution, please see 
 * the LicensingInformation file at the root level of the distribution. 
 *
 * Copyright (c) 2005-2006
 *  Kobrix Software, Inc.  All rights reserved.
 */
package org.hypergraphdb.app.xsd;

import java.util.Map;

import org.hypergraphdb.HGPersistentHandle;
import org.hypergraphdb.type.RecordType;

/**
 * 
 */
public class ComplexRecordType extends RecordType {

	/**
	 * 
	 */
    public HGPersistentHandle store(Object instance)
    {
    	Map map = (Map)instance;
    	
    	return super.store(instance);
    }
}
