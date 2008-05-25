package org.hypergraphdb.peer.jxta;

import java.net.URI;
import java.net.URISyntaxException;

import net.jxta.document.AdvertisementFactory;
import net.jxta.id.IDFactory;
import net.jxta.pipe.PipeID;
import net.jxta.pipe.PipeService;
import net.jxta.protocol.PipeAdvertisement;

public class HGAdvertisementsFactory {
   
    public static PipeAdvertisement newAdvertisement(String peerId) {
        PipeID pipeID = null;

        try {
            pipeID = (PipeID) IDFactory.fromURI(new URI(peerId));
        } catch (URISyntaxException ex) {
            ex.printStackTrace();
        }
        
        PipeAdvertisement advertisement = (PipeAdvertisement)
                AdvertisementFactory.newAdvertisement(PipeAdvertisement.getAdvertisementType());

        advertisement.setPipeID(pipeID);
        advertisement.setType(PipeService.UnicastType);
        advertisement.setName("HGDB Server");
        return advertisement;
    }
}
