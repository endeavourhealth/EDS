package org.endeavourhealth.queuereader;

import org.endeavourhealth.core.configuration.QueueReaderConfiguration;
import org.endeavourhealth.common.utility.XmlSerializer;

public abstract class ConfigDeserialiser {

    private static final String CONFIG_XSD = "QueueReaderConfiguration.xsd";

    public static QueueReaderConfiguration deserialise(String xmlStr) throws Exception {
        return XmlSerializer.deserializeFromString(QueueReaderConfiguration.class, xmlStr, CONFIG_XSD);
    }


}
