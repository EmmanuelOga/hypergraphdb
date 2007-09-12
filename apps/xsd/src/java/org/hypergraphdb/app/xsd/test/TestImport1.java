/*
 * This file is part of the XSD for HyperGraphDB source distribution. This is copyrighted
 * software. For permitted uses, licensing options and redistribution, please see
 * the LicensingInformation file at the root level of the distribution.
 *
 * Copyright (c) 2007
 * Kobrix Software, Inc.  All rights reserved.
 */
package org.hypergraphdb.app.xsd.test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGPersistentHandle;
import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.app.xsd.RestrictionViolationException;
import org.hypergraphdb.app.xsd.SchemaImporter;
import org.hypergraphdb.app.xsd.XSDPrimitiveTypeSystem;
import org.hypergraphdb.type.HGCompositeType;
import org.hypergraphdb.type.HGProjection;
import org.hypergraphdb.type.Record;
import org.hypergraphdb.type.Slot;

/**
 *
 */
public class TestImport1
{
    private static final String DATABASELOCATION = "xsdapp";

    /**
     *
     * @param args String[]
     */
    public static void main(String[] args)
    {
        System.out.println("Executing simple import unit tests...\n------\n\n");
//        TestImport1.exercizeImportSchema();
//        TestImport1.exercizeImportSchema2();
//        TestImport1.exercizeDecimalMinInclusive();
//        TestImport1.exercizeDecimalMaxInclusive();
//        TestImport1.exercizeDecimalMinExclusive();
//        TestImport1.exercizeDecimalMaxExclusive();
//        TestImport1.exercizeDecimalTotalDigits();
//        TestImport1.exercizeDecimalFractionDigits();
//        TestImport1.testComplexImport1();
        TestImport1.testComplexImport2();
    }

    /**
     *
     */
    private static void exercizeImportSchema()
    {
        HGPersistentHandle pHandle = null;
        HyperGraph hg = new HyperGraph();
        final String original = "1024";

        //phase1
        try
        {
            hg.open(TestImport1.DATABASELOCATION);
            XSDPrimitiveTypeSystem.getInstance().bootstrap(hg);

            SchemaImporter importer = new SchemaImporter(hg);
            importer.importSchema("/org/hypergraphdb/app/xsd/test/test1.xsd");

            //use of the imported types.
            HGHandle typeHandle = hg.getTypeSystem().getTypeHandle(
                "file:///c:/kobrix/test1.xsd#SKU");
            HGHandle handle = hg.add(original, typeHandle);
            pHandle = hg.getPersistentHandle(handle);
        } catch (Throwable t)
        {
            t.printStackTrace();
        } finally
        {
            hg.close();
        }

        //phase2
        try
        {
            hg.open(TestImport1.DATABASELOCATION);
            XSDPrimitiveTypeSystem.getInstance().bootstrap(hg);

            SchemaImporter importer = new SchemaImporter(hg);
            importer.importSchema("/org/hypergraphdb/app/xsd/test/test1.xsd");

            String retrieved = (String) hg.get(pHandle);
            hg.remove(pHandle);

            if (!original.equals(retrieved))
            {
                throw new RuntimeException(
                    "Initial and stored SKUs do not match!");
            }

            System.out.println("PASSED: exercizeImportSchema.");
        } catch (Throwable t)
        {
            t.printStackTrace();
        } finally
        {
            hg.close();
        }

    } //exercizeImportSchema.

    /**
     * Store and retrieve a file:///c:/kobrix/test1.xsd#myInteger.
     */
    private static void exercizeImportSchema2()
    {
        HGPersistentHandle pHandle = null;
        HyperGraph hg = new HyperGraph();
        /**@todo make the integer types work with ints, bytes, shorts.*/
        final BigDecimal original = new BigDecimal(14000);

        //phase1
        try
        {
            hg.open(TestImport1.DATABASELOCATION);
            XSDPrimitiveTypeSystem.getInstance().bootstrap(hg);

            SchemaImporter importer = new SchemaImporter(hg);
            importer.importSchema("/org/hypergraphdb/app/xsd/test/test1.xsd");

            //use of the imported types.
            HGHandle typeHandle = hg.getTypeSystem().getTypeHandle(
                "file:///c:/kobrix/test1.xsd#myInteger");
            HGHandle handle = hg.add(original, typeHandle);
            pHandle = hg.getPersistentHandle(handle);
        } finally
        {
            hg.close();
        }

        //phase2
        try
        {
            hg.open(TestImport1.DATABASELOCATION);
            XSDPrimitiveTypeSystem.getInstance().bootstrap(hg);

            SchemaImporter importer = new SchemaImporter(hg);
            importer.importSchema("/org/hypergraphdb/app/xsd/test/test1.xsd");

            BigDecimal retrieved = (BigDecimal) hg.get(pHandle);
            hg.remove(pHandle);

            if (!original.equals(retrieved))
            {
                throw new RuntimeException(
                    "Initial and stored myIntegers do not match!");
            }

            System.out.println("PASSED: exercizeImportSchema2.");
        } catch (Throwable t)
        {
            t.printStackTrace();
        } finally
        {
            hg.close();
        }

    } //exercizeImportSchema2.

    /**
     * Work with a file:///c:/kobrix/test1.xsd#myInteger's that are at the ends of the range of
     * allowed values.
     */
    private static void exercizeDecimalMinInclusive()
    {
        HyperGraph hg = null;
        BigDecimal original = new BigDecimal(9999);

        //expected to fail the store operation.
        try
        {
            hg = TestImport1.setup();

            //use of the imported types.
            HGHandle typeHandle = hg.getTypeSystem().getTypeHandle(
                "file:///c:/kobrix/test1.xsd#myInteger");
            hg.add(original, typeHandle);
        } catch (RuntimeException t)
        {
            if (!(t instanceof RestrictionViolationException))
            {
                throw t;
            }
        } finally
        {
            hg.close();
        }

        original = new BigDecimal(10001);
        //should pass.
        try
        {
            hg = TestImport1.setup();

            //use of the imported types.
            HGHandle typeHandle = hg.getTypeSystem().getTypeHandle(
                "file:///c:/kobrix/test1.xsd#myInteger");
            HGHandle handle = hg.add(original, typeHandle);
            hg.remove(handle);
        } finally
        {
            hg.close();
        }

        original = new BigDecimal(10000);
        //should pass.
        try
        {
            hg = TestImport1.setup();

            //use of the imported types.
            HGHandle typeHandle = hg.getTypeSystem().getTypeHandle(
                "file:///c:/kobrix/test1.xsd#myInteger");
            HGHandle handle = hg.add(original, typeHandle);
            hg.remove(handle);

            System.out.println("PASSED: exercizeDecimalMinInclusive.");
        } finally
        {
            hg.close();
        }

    } //exercizeDecimalMinInclusive.

    /**
     * Work with a file:///c:/kobrix/test1.xsd#myInteger's that are at the ends of the range of
     * allowed values.
     */
    private static void exercizeDecimalMaxInclusive()
    {
        HyperGraph hg = null;
        BigDecimal original = new BigDecimal(100000);

        //expected to fail the store operation.
        try
        {
            hg = TestImport1.setup();

            //use of the imported types.
            HGHandle typeHandle = hg.getTypeSystem().getTypeHandle(
                "file:///c:/kobrix/test1.xsd#myInteger");
            hg.add(original, typeHandle);
        } catch (RuntimeException t)
        {
            if (!(t instanceof RestrictionViolationException))
            {
                throw t;
            }
        } finally
        {
            hg.close();
        }

        original = new BigDecimal(99999);
        //should pass.
        try
        {
            hg = TestImport1.setup();

            //use of the imported types.
            HGHandle typeHandle = hg.getTypeSystem().getTypeHandle(
                "file:///c:/kobrix/test1.xsd#myInteger");
            HGHandle handle = hg.add(original, typeHandle);
            hg.remove(handle);
        } finally
        {
            hg.close();
        }

        original = new BigDecimal(100001);
        //expected to fail the store operation.
        try
        {
            hg = TestImport1.setup();

            //use of the imported types.
            HGHandle typeHandle = hg.getTypeSystem().getTypeHandle(
                "file:///c:/kobrix/test1.xsd#myInteger");
            hg.add(original, typeHandle);

        } catch (RuntimeException t)
        {
            if (!(t instanceof RestrictionViolationException))
            {
                throw t;
            } else
            {
                System.out.println("PASSED: exercizeDecimalMaxInclusive.");
            }
        } finally
        {
            hg.close();
        }

    } //exercizeDecimalMaxInclusive.

    /**
     * Work with a file:///c:/kobrix/test1.xsd#myInteger1's that are at the ends of the range of
     * allowed values.
     */
    private static void exercizeDecimalMinExclusive()
    {
        HyperGraph hg = null;
        BigDecimal original = new BigDecimal(999);

        //expected to fail the store operation.
        try
        {
            hg = TestImport1.setup();

            //use of the imported types.
            HGHandle typeHandle = hg.getTypeSystem().getTypeHandle(
                "file:///c:/kobrix/test1.xsd#myInteger1");
            hg.add(original, typeHandle);
        } catch (RuntimeException t)
        {
            if (!(t instanceof RestrictionViolationException))
            {
                throw t;
            }
        } finally
        {
            hg.close();
        }

        original = new BigDecimal(1001);
        //should pass.
        try
        {
            hg = TestImport1.setup();

            //use of the imported types.
            HGHandle typeHandle = hg.getTypeSystem().getTypeHandle(
                "file:///c:/kobrix/test1.xsd#myInteger1");
            HGHandle handle = hg.add(original, typeHandle);
            hg.remove(handle);
        } finally
        {
            hg.close();
        }

        original = new BigDecimal(1000);
        //should fail.
        try
        {
            hg = TestImport1.setup();

            //use of the imported types.
            HGHandle typeHandle = hg.getTypeSystem().getTypeHandle(
                "file:///c:/kobrix/test1.xsd#myInteger1");
            hg.add(original, typeHandle);

            System.out.println("PASSED: exercizeDecimalMinExclusive.");
        } catch (RuntimeException t)
        {
            if (!(t instanceof RestrictionViolationException))
            {
                throw t;
            }
        } finally
        {
            hg.close();
        }
    } //exercizeDecimalMinExclusive.

    /**
     * Work with a file:///c:/kobrix/test1.xsd#myInteger1's that are at the ends of the range of
     * allowed values.
     */
    private static void exercizeDecimalMaxExclusive()
    {
        HyperGraph hg = null;
        BigDecimal original = new BigDecimal(10000);

        //expected to fail the store operation.
        try
        {
            hg = TestImport1.setup();

            //use of the imported types.
            HGHandle typeHandle = hg.getTypeSystem().getTypeHandle(
                "file:///c:/kobrix/test1.xsd#myInteger1");
            hg.add(original, typeHandle);
        } catch (RuntimeException t)
        {
            if (!(t instanceof RestrictionViolationException))
            {
                throw t;
            }
        } finally
        {
            hg.close();
        }

        original = new BigDecimal(9998);
        //should pass.
        try
        {
            hg = TestImport1.setup();

            //use of the imported types.
            HGHandle typeHandle = hg.getTypeSystem().getTypeHandle(
                "file:///c:/kobrix/test1.xsd#myInteger1");
            HGHandle handle = hg.add(original, typeHandle);
            hg.remove(handle);
        } finally
        {
            hg.close();
        }

        original = new BigDecimal(9999);
        //expected to fail the store operation.
        try
        {
            hg = TestImport1.setup();

            //use of the imported types.
            HGHandle typeHandle = hg.getTypeSystem().getTypeHandle(
                "file:///c:/kobrix/test1.xsd#myInteger1");
            hg.add(original, typeHandle);

        } catch (RuntimeException t)
        {
            if (!(t instanceof RestrictionViolationException))
            {
                throw t;
            } else
            {
                System.out.println("PASSED: exercizeDecimalMaxExclusive.");
            }
        } finally
        {
            hg.close();
        }

    } //exercizeDecimalMaxExclusive.

    /**
     * Works with a file:///c:/kobrix/test1.xsd#money's that are at the ends of the range of
     * allowed values.
     */
    private static void exercizeDecimalTotalDigits()
    {
        HyperGraph hg = null;

        //should pass.
        BigDecimal original = new BigDecimal("922337.5807");
        try
        {
            hg = TestImport1.setup();

            //use of the imported types.
            HGHandle typeHandle = hg.getTypeSystem().getTypeHandle(
                "file:///c:/kobrix/test1.xsd#money");
            HGHandle handle = hg.add(original, typeHandle);
            hg.remove(handle);
        } finally
        {
            hg.close();
        }

        //should pass.
        original = new BigDecimal("-922337.5808");
        try
        {
            hg = TestImport1.setup();

            //use of the imported types.
            HGHandle typeHandle = hg.getTypeSystem().getTypeHandle(
                "file:///c:/kobrix/test1.xsd#money");
            HGHandle handle = hg.add(original, typeHandle);
            hg.remove(handle);
        } finally
        {
            hg.close();
        }

        //should fail.
        original = new BigDecimal("99999999999");
        try
        {
            hg = TestImport1.setup();

            HGHandle typeHandle = hg.getTypeSystem().getTypeHandle(
                "file:///c:/kobrix/test1.xsd#money");
            HGHandle handle = hg.add(original, typeHandle);
            hg.remove(handle);
        } catch (RuntimeException t)
        {
            if (!(t instanceof RestrictionViolationException))
            {
                throw t;
            } else
            {
                System.out.println("PASSED: exercizeDecimalTotalDigits.");
            }
        } finally
        {
            hg.close();
        }

    } //exercizeDecimalTotalDigits.

    /**
     * Works with a file:///c:/kobrix/test1.xsd#money's that are at the ends of the range of
     * allowed values.
     */
    private static void exercizeDecimalFractionDigits()
    {
        HyperGraph hg = null;

        //should pass.
        BigDecimal original = new BigDecimal("922337.5807");
        try
        {
            hg = TestImport1.setup();

            //use of the imported types.
            HGHandle typeHandle = hg.getTypeSystem().getTypeHandle(
                "file:///c:/kobrix/test1.xsd#money");
            HGHandle handle = hg.add(original, typeHandle);
            hg.remove(handle);
        } finally
        {
            hg.close();
        }

        //should pass.
        original = new BigDecimal("-922337.5808");
        try
        {
            hg = TestImport1.setup();

            //use of the imported types.
            HGHandle typeHandle = hg.getTypeSystem().getTypeHandle(
                "file:///c:/kobrix/test1.xsd#money");
            HGHandle handle = hg.add(original, typeHandle);
            hg.remove(handle);
        } finally
        {
            hg.close();
        }

        //should fail.
        original = new BigDecimal("999999.99999");
        try
        {
            hg = TestImport1.setup();

            HGHandle typeHandle = hg.getTypeSystem().getTypeHandle(
                "file:///c:/kobrix/test1.xsd#money");
            HGHandle handle = hg.add(original, typeHandle);
            hg.remove(handle);
        } catch (RuntimeException t)
        {
            if (!(t instanceof RestrictionViolationException))
            {
                throw t;
            } else
            {
                System.out.println("PASSED: exercizeDecimalFractionDigits.");
            }
        } finally
        {
            hg.close();
        }

    } //exercizeDecimalFractionDigits.

    /**
     *
     */
    private static void testComplexImport1()
    {
        HGPersistentHandle pHandle = null;
        HyperGraph hg = new HyperGraph();

        //phase1
        try
        {
            hg.open(TestImport1.DATABASELOCATION);

            XSDPrimitiveTypeSystem.getInstance().bootstrap(hg);

            SchemaImporter importer = new SchemaImporter(hg);
            importer.importSchema("/org/hypergraphdb/app/xsd/test/testcomplex1.xsd");

            //use of the imported types.
            HGHandle typeHandle = hg.getTypeSystem().getTypeHandle("USAddress");
            
            Map usAddress = new HashMap();
            usAddress.put("name", "tx");
            usAddress.put("street", "royal");
            usAddress.put("city", "irving");
                        
            HGHandle handle = hg.add(usAddress, typeHandle);

            pHandle = hg.getPersistentHandle(handle);
        } finally
        {
            hg.close();
        }

        //phase2
        try
        {
            hg.open(TestImport1.DATABASELOCATION);

            XSDPrimitiveTypeSystem.getInstance().bootstrap(hg);
            Object o = hg.get(pHandle);
            System.out.println("Object: "+o);
        } finally
        {
            hg.close();
        }

    } //testComplexImport1.

   /**
    *
    */
   private static void testComplexImport2()
   {
       HGPersistentHandle pHandle = null;
       HyperGraph hg = new HyperGraph();

       //phase1
       try
       {
           hg.open(TestImport1.DATABASELOCATION);

           XSDPrimitiveTypeSystem.getInstance().bootstrap(hg);

           SchemaImporter importer = new SchemaImporter(hg);
           importer.importSchema("/org/hypergraphdb/app/xsd/test/testcomplex1.xsd");

           //use of the imported types.
           HGHandle typeHandle = hg.getTypeSystem().getTypeHandle("length3");
           
           Map<String,Object> usAddress = new HashMap<String,Object>();
           usAddress.put("size", new BigDecimal("1024"));
           usAddress.put("unit", "miles");
                       
           HGHandle handle = hg.add(usAddress, typeHandle);

           pHandle = hg.getPersistentHandle(handle);
       } finally
       {
           hg.close();
       }

       //phase2
       try
       {
           hg.open(TestImport1.DATABASELOCATION);

           XSDPrimitiveTypeSystem.getInstance().bootstrap(hg);
           Object o = hg.get(pHandle);
           System.out.println("Object: "+o);
       } finally
       {
           hg.close();
       }

   } //testComplexImport2.

   ////////////////////////////
    // Private Helper Methods //
    ////////////////////////////

    /**
     *
     */
    private static HyperGraph setup()
    {
        HyperGraph result = new HyperGraph();

        result.open(TestImport1.DATABASELOCATION);
        XSDPrimitiveTypeSystem.getInstance().bootstrap(result);

        SchemaImporter importer = new SchemaImporter(result);
        importer.importSchema("/org/hypergraphdb/app/xsd/test/test1.xsd");

        return result;
    }

} //TestImport1.
