/*
 * This file is part of the XSD for HyperGraphDB source distribution. This is copyrighted
 * software. For permitted uses, licensing options and redistribution, please see
 * the LicensingInformation file at the root level of the distribution.
 *
 * Copyright (c) 2007
 * Kobrix Software, Inc.  All rights reserved.
 */
package org.hypergraphdb.app.xsd;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGValueLink;
import org.hypergraphdb.type.RecordType;
import org.hypergraphdb.type.Slot;
import org.xml.sax.Attributes;

/**
 * Imports XSD complexType's into the HGDB's type system.
 */
public class ComplexTypeImporter
{
   private SchemaImporter importer;
   private String name;
   private RecordType recordType;

   /**
    * 
    * @param importer
    *           SchemaImporter
    */
   /* package */ComplexTypeImporter(SchemaImporter importer)
   {
      this.importer = importer;
   }

   /**
    * 
    * @param attributes
    *           Attributes
    */
   public void startDefinition(
      Attributes attributes)
   {
      /** @todo form the full name. */
      name = attributes.getValue("name");

      recordType = new RecordType();

      /**@todo what to do with the type name ?*/
      /*Slot slot = new Slot();
      slot.setLabel(name);
      slot.setValueType(importer.hg.getTypeSystem().getTypeHandle(String.class));
      HGHandle slotHandle = importer.hg.add(slot);
      recordType.addSlot(slotHandle);*/
   }

   /**
    * 
    */
   public void endDefinition()
   {
      HGHandle theHandle = importer.hg.add(null, ComplexTypeConstructor.HANDLE);

      HGValueLink link = new HGValueLink(recordType, new HGHandle[]
      { theHandle });
      importer.hg.add(link);

      importer.hg.getTypeSystem().addAlias(theHandle, name);
   }

   /**
    * 
    * @param attributes
    *           Attributes
    */
   public void startSequence(
      Attributes attributes)
   {
   }

   /**
    * 
    * @param attributes
    *           Attributes
    */
   public void startElement(
      Attributes attributes)
   {
      String name = attributes.getValue("name");
      String type = attributes.getValue("type");

      /**@todo resolve the type name to full. */
      Slot slot = new Slot(name, importer.hg.getTypeSystem().getTypeHandle(type));
      HGHandle slotHandle = importer.hg.add(slot);
      recordType.addSlot(slotHandle);
   }

   /**
    * 
    * @param attributes
    *           Attributes
    */
   public void doAttribute(
      Attributes attributes)
   {
      String name = attributes.getValue("name");
      //String type = attributes.getValue("type");

      /** @todo hcoded to String temporarily. */
      Slot slot = new Slot(name, importer.hg.getTypeSystem().getTypeHandle(String.class));
      HGHandle slotHandle = importer.hg.add(slot);
      recordType.addSlot(slotHandle);
   }

} // ComplexTypeImporter.
