package org.endeavourhealth.hl7test.transform.parser;

import org.apache.commons.lang3.Validate;

import java.lang.reflect.Constructor;

public class Datatype {
    protected GenericDatatype datatype;

    public Datatype(GenericDatatype datatype) {
        Validate.notNull(datatype);

        this.datatype = datatype;
    }

    public String getComponent(int componentNumber) {
        return this.datatype.getComponent(componentNumber);
    }

    public String getAsString() {
        return this.datatype.getAsString();
    }

    public static <T extends Datatype> T instantiate(Class<T> dt, GenericDatatype datatype) {
        try {
            Constructor<T> constructor = dt.getConstructor(GenericDatatype.class);
            return constructor.newInstance(datatype);
        } catch (Exception e) {
            throw new RuntimeException("Could not instantiate " + dt.getName(), e);
        }
    }
}
