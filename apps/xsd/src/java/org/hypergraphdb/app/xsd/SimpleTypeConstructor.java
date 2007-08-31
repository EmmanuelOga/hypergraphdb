/*
 * This file is part of the HyperGraphDB source distribution. This is copyrighted
 * software. For permitted uses, licensing options and redistribution, please see
 * the LicensingInformation file at the root level of the distribution.
 *
 * Copyright (c) 2007
 * Kobrix Software, Inc.  All rights reserved.
 */
package org.hypergraphdb.app.xsd;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGHandleFactory;
import org.hypergraphdb.HGPersistentHandle;
import org.hypergraphdb.HGQuery;
import org.hypergraphdb.HGSearchResult;
import org.hypergraphdb.HGValueLink;
import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.LazyRef;
import org.hypergraphdb.atom.HGSubsumes;
import org.hypergraphdb.query.And;
import org.hypergraphdb.query.AtomTypeCondition;
import org.hypergraphdb.type.HGAtomType;

/**
 * 
 */
public class SimpleTypeConstructor implements HGAtomType
{
   public static final HGPersistentHandle HANDLE = HGHandleFactory
         .makeHandle("bd99ff0c-5b32-11db-82a1-a78cc7527afb");
   public static final SimpleTypeConstructor INSTANCE = new SimpleTypeConstructor();

   private HyperGraph hg;

   /**
    * 
    */
   private SimpleTypeConstructor()
   {
   }

   /**
    * 
    * @param handle
    *           HGPersistentHandle
    * @param targetSet
    *           LazyRef
    * @param incidenceSet
    *           LazyRef
    * @return Object
    */
   public Object make(
      HGPersistentHandle handle, LazyRef<HGHandle[]> targetSet,
      LazyRef<HGHandle[]> incidenceSet)
   {
      HGHandle[] handles = incidenceSet.deref();

      XSDTypeImplementation nameType = null;
      HGSubsumes subsumes = null;
      HGValueLink valueLink = null;
      FacetsDescriptorBase facets = null;

      /** @todo explore the order of fetching of the links. */
      /*
       * for(int i=0; i<3; i++) { Object o = hg.get(handles[i]);
       * 
       * if(o instanceof HGValueLink) { valueLink = (HGValueLink)o; } else if(o
       * instanceof XSDTypeImplementation) { nameType =
       * (XSDTypeImplementation)o; } else if(o instanceof XSDTypeImplementation) {
       * subsumes = (HGSubsumes)o; } }
       */

      nameType = (XSDTypeImplementation) hg.get(handles[0]);
      subsumes = (HGSubsumes) hg.get(handles[1]);
      valueLink = (HGValueLink) hg.get(handles[2]);

      facets = (FacetsDescriptorBase) valueLink.getValue();
      HGHandle typeNameHandle = nameType.getXSDTypeName();
      XSDTypeName typeName = (XSDTypeName) hg.get(typeNameHandle);

      /**
       * @todo tries to find the most general XSD type but it might well be not
       *       the most efficient way to do it; review at some point.
       */
      HGHandle hGeneral = subsumes.getGeneral();
      boolean done = false;

      while (false == done)
      {
         And subsumesCondition = new And(HGQuery.hg.type(HGSubsumes.class), HGQuery.hg
               .link(hGeneral));

         HGSearchResult<HGHandle> rs = null;
         boolean matches = false;

         try
         {
            rs = hg.find(subsumesCondition);
            matches = rs.hasNext();

            done = true;
            while (rs.hasNext())
            {
               HGHandle aHandle = rs.next();

               HGSubsumes subsumesLink = (HGSubsumes) hg.get(aHandle);
               HGHandle specific = subsumesLink.getSpecific();

               if (specific.equals(hGeneral))
               {
                  hGeneral = subsumesLink.getGeneral();
                  done = false;
                  break;
               }
            }
         } finally
         {
            rs.close();
         }
      }

      Class newClass = null;

      /** @todo newClass needs to be better dealt with. */
      String className = (String) hg.get(hGeneral);
      try
      {
         newClass = Class.forName(className);
      } catch (ClassNotFoundException e)
      {
         e.printStackTrace();
      }

      ClassGenerator cg = new ClassGenerator();
      newClass = cg.implementEvaluate(newClass, facets.getFacets());

      Object result = null;
      try
      {
         result = newClass.newInstance();
      } catch (Exception ex)
      {
         ex.printStackTrace();
      }

      return result;
   } // make.

   /**
    * 
    * @param hg
    *           HyperGraph
    */
   public void setHyperGraph(
      HyperGraph hg)
   {
      this.hg = hg;
   }

   /**
    * 
    * @param instance
    *           Object
    * @return HGPersistentHandle
    */
   public HGPersistentHandle store(
      Object instance)
   {
      return HGHandleFactory.nullHandle();
   }

   /**
    * 
    * @param handle
    *           HGPersistentHandle
    */
   public void release(
      HGPersistentHandle handle)
   {
      /** @todo */
   }

   /**
    * 
    * @param general
    *           Object
    * @param specific
    *           Object
    * @return boolean
    */
   public boolean subsumes(
      Object general, Object specific)
   {
      return false;
   }

} // SimpleTypeConstructor.
