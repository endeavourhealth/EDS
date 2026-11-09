package org.endeavourhealth.ui.entitymap;

import org.endeavourhealth.common.utility.Resources;
import org.endeavourhealth.common.utility.XmlSerializer;
import org.endeavourhealth.ui.entitymap.models.EntityMap;

public class EntityMapHelper {

    private static final String XSD = "EntityMap.xsd";

    public static EntityMap loadEntityMap() throws Exception {

        String xml = Resources.getResourceAsString("EntityMap.xml");
        //"src/discovery-controller/src/main/resources/controller.config";

        return XmlSerializer.deserializeFromString(EntityMap.class, xml, "EntityMap.xsd");
    }
//
//    public static Entity getPrimaryEntity(EntityMap map) {
//        return map.getEntity().get(0);
//    }
}
