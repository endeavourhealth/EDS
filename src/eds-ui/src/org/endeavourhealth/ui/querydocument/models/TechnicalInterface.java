package org.endeavourhealth.ui.querydocument.models;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

/**
 * Created by darren on 19/05/16.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "technicalInterface", propOrder = {
        "uuid",
        "name",
        "messageType",
        "messageFormat",
        "messageFormatVersion"
})
public class TechnicalInterface {

    protected String uuid;
    protected String name;
    protected String messageType;
    protected String messageFormat;
    protected String messageFormatVersion;

    public String getUuid() {
        return uuid;
    }
    public void setUuid(String value) {
        this.uuid = value;
    }

    public String getName() {
        return name;
    }
    public void setName(String value) {
        this.name = value;
    }

    public String getMessageType() {
        return messageType;
    }
    public void setMessageType(String value) {
        this.messageType = value;
    }

    public String getMessageFormat() {
        return messageFormat;
    }
    public void setMessageFormat(String value) {
        this.messageFormat = value;
    }

    public String getMessageFormatVersion() {
        return messageFormatVersion;
    }
    public void setMessageFormatVersion(String value) {
        this.messageFormatVersion = value;
    }

}




