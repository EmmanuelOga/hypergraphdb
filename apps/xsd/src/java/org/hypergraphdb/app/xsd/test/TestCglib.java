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
//        TestCglib.testAsm();
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

    /**
     *
     */
    private static void testAsm()
    {
//        Class clazz = XSDDecimalPrimitive.class;
//        String clazzName = clazz.getName();
//        clazzName = clazzName.replace('.', '/');
//        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
//
//        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
//        cw.visit(Constants.V1_5, Constants.ACC_PUBLIC, "GeneratedClass", null,
//                 clazzName, null);
//
//        // creates a MethodWriter for the (implicit) constructor
//        MethodVisitor mw = cw.visitMethod(Constants.ACC_PUBLIC,
//                                          "<init>",
//                                          "()V",
//                                          null,
//                                          null);
//        // pushes the 'this' variable
//        mw.visitVarInsn(Constants.ALOAD, 0);
//        // invokes the super class constructor
//        mw.visitMethodInsn(Constants.INVOKESPECIAL,
//                           clazzName,
//                           "<init>", "()V");
//        mw.visitInsn(Constants.RETURN);
//        // this code uses a maximum of one stack element and one local variable
//        mw.visitMaxs(1, 1);
//        mw.visitEnd();
//
//        // creates a MethodWriter for the 'main' method
//        mw = cw.visitMethod(Constants.ACC_PUBLIC,
//                            "evaluateRestrictions",
//                            "(Ljava/math/BigDecimal;)Z",
//                            null,
//                            null);
//
//        /**todo go through the facets*/
//
//        //make a big decimal instance.
//        mw.visitVarInsn(Constants.ALOAD, 0);
//        mw.visitTypeInsn(Opcodes.NEW, "java/math/BigDecimal");
//        mw.visitInsn(Opcodes.DUP);
//        mw.visitLdcInsn("1000");
//        mw.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/math/BigDecimal",
//                           "<init>", "(Ljava/lang/String;)V");
//
//        mw.visitVarInsn(Opcodes.ASTORE, 2);
//
//        mw.visitInsn(Opcodes.ICONST_0);
//        mw.visitVarInsn(Opcodes.ALOAD, 1);
//        mw.visitVarInsn(Opcodes.ALOAD, 2);
//        mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/math/BigDecimal",
//                           "compareTo", "(Ljava/math/BigDecimal;)I");
//
//        Label l0 = new Label();
//
//        mw.visitJumpInsn(Opcodes.IF_ICMPGE, l0);
//        mw.visitInsn(Opcodes.ICONST_0);
//        mw.visitInsn(Opcodes.IRETURN);
//
//        mw.visitLabel(l0);
//        mw.visitInsn(Opcodes.ICONST_1);
//        mw.visitInsn(Opcodes.IRETURN);
//        mw.visitMaxs(1, 1);
//        mw.visitEnd();
//
//        // gets the bytecode of the Example class, and loads it dynamically
//        byte[] code = cw.toByteArray();
//
////        TestClassLoader loader = new TestClassLoader();
////        Class<XSDDecimalPrimitive> exampleClass = loader.defineClass("GeneratedClass", code);
////
////        try
////        {
////            XSDDecimalPrimitive o = exampleClass.newInstance();
////            int i = o.evaluateRestrictions(new BigDecimal("1000"));
////            System.out.println("i:"+i);
////        } catch (Exception ex)
////        {
////            ex.printStackTrace();
////        }
//
//
//        TestClassLoader loader = new TestClassLoader();
//        Class<XSDDecimalPrimitive> exampleClass = loader.defineClass("GeneratedClass", code);
//
//        try
//        {
//            XSDDecimalPrimitive o = exampleClass.newInstance();
//            boolean b = o.evaluateRestrictions(new BigDecimal("1000"));
//        } catch (Exception ex)
//        {
//            ex.printStackTrace();
//        }
//
//        //cw.visitEnd();
    }

}


