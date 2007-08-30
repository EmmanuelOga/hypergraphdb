/*
 * This file is part of the HyperGraphDB source distribution. This is copyrighted
 * software. For permitted uses, licensing options and redistribution, please see
 * the LicensingInformation file at the root level of the distribution.
 *
 * Copyright (c) 2007
 * Kobrix Software, Inc.  All rights reserved.
 */
package org.hypergraphdb.app.xsd.test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;

//import net.sf.cglib.core.Constants;
import org.hypergraphdb.app.xsd.ClassGenerator;
import org.hypergraphdb.app.xsd.facet.ConstrainingFacet;
import org.hypergraphdb.app.xsd.facet.TotalDigitsFacet;
import org.hypergraphdb.app.xsd.primitive.XSDDecimalPrimitive;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 *
 */
public class TestCglib
{

   protected static class TestClassLoader extends ClassLoader
    {
        public TestClassLoader()
        {
        }

        public Class defineClass(final String name, final byte[] b)
        {
            return defineClass(name, b, 0, b.length);
        }
    }


    public static void main(String[] args)
    {
        try
        {
            TestCglib.testTotalDigits();
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    /**
     *
     */
    private static void testTotalDigits() throws InstantiationException, IllegalAccessException
    {
        Class newClass = XSDDecimalPrimitive.class;

        Collection<ConstrainingFacet> facets = new ArrayList<ConstrainingFacet>();
        TotalDigitsFacet facet = new TotalDigitsFacet();
        facet.setValue(5);
        facets.add(facet);
        ClassGenerator cg = new ClassGenerator();
        newClass = cg.implementEvaluate(newClass, facets);

        XSDDecimalPrimitive result = null;
        result = (XSDDecimalPrimitive)newClass.newInstance();

        boolean passes = result.evaluateRestrictions(new BigDecimal(99));
        System.out.println("EOT - "+passes+'.');

        passes = result.evaluateRestrictions(new BigDecimal(98989898));
        System.out.println("EOT - "+passes+'.');

        passes = result.evaluateRestrictions(new BigDecimal("11.11"));
        System.out.println("EOT - "+passes+'.');
        passes = result.evaluateRestrictions(new BigDecimal("1111."));
        System.out.println("EOT - "+passes+'.');
        passes = result.evaluateRestrictions(new BigDecimal("1111.11"));
        System.out.println("EOT - "+passes+'.');
        passes = result.evaluateRestrictions(new BigDecimal("111.11"));
        System.out.println("EOT - "+passes+'.');
        passes = result.evaluateRestrictions(new BigDecimal("1111.1"));
        System.out.println("EOT - "+passes+'.');
    }

}


