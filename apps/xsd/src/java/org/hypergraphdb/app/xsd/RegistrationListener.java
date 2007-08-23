package org.hypergraphdb.app.xsd;

import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.event.HGEvent;
import org.hypergraphdb.event.HGListener;
import org.hypergraphdb.event.HGListener.Result;
import org.hypergraphdb.event.HGOpenedEvent;


/**
 *
 */
public class RegistrationListener implements HGListener<HGOpenedEvent>
{

    /**
     * Registers the XSD predefined types.
     */
    public Result handle(HyperGraph hg, HGOpenedEvent event)
    {
        hg.getTypeSystem().addPredefinedType(SimpleTypeConstructor.HANDLE,
                                             SimpleTypeConstructor.INSTANCE, null);
        SimpleTypeConstructor.INSTANCE.setHyperGraph(hg);
        hg.getTypeSystem().addPredefinedType(ComplexTypeConstructor.HANDLE,
                                             ComplexTypeConstructor.INSTANCE, null);
        ComplexTypeConstructor.INSTANCE.setHyperGraph(hg);

        XSDPrimitiveTypeSystem.getInstance().importPrimitives(hg, "/org/hypergraphdb/app/xsd/XMLSchema.xsd");

        return Result.ok;
    }

}
