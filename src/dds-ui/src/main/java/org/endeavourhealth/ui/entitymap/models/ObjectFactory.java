
package org.endeavourhealth.ui.entitymap.models;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.endeavourhealth.ui.entitymap.models package.
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _EntityMap_QNAME = new QName("", "entityMap");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.endeavourhealth.ui.entitymap.models
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link EntityMap }
     * 
     */
    public EntityMap createEntityMap() {
        return new EntityMap();
    }

    /**
     * Create an instance of {@link DataValueType }
     * 
     */
    public DataValueType createDataValueType() {
        return new DataValueType();
    }

    /**
     * Create an instance of {@link Field }
     * 
     */
    public Field createField() {
        return new Field();
    }

    /**
     * Create an instance of {@link Entity }
     * 
     */
    public Entity createEntity() {
        return new Entity();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link EntityMap }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "entityMap")
    public JAXBElement<EntityMap> createEntityMap(EntityMap value) {
        return new JAXBElement<EntityMap>(_EntityMap_QNAME, EntityMap.class, null, value);
    }

}
