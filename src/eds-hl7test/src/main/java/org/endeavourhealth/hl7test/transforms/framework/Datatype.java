package org.endeavourhealth.hl7test.transforms.framework;

import org.apache.commons.lang3.Validate;

import java.lang.reflect.Constructor;

public class Datatype {
    protected GenericDatatype datatype;

    public Datatype(GenericDatatype datatype) {
        Validate.notNull(datatype);

        this.datatype = datatype;
    }

    public static <T extends Datatype> T instantiate(Class<T> dt, GenericDatatype datatype) throws ParseException {
        try {
            Constructor<T> constructor = dt.getConstructor(GenericDatatype.class);
            return constructor.newInstance(datatype);
        } catch (Exception e) {
            throw new ParseException("Could not instantiate " + dt.getName(), e);
        }
    }
}
