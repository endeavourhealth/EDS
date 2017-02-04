package org.endeavourhealth.hl7test.transforms.framework;

import com.mchange.v2.cfg.PropertiesConfigSource;
import org.apache.commons.lang3.Validate;

import java.util.ArrayList;
import java.util.List;

public class Field {
    private static final int FIRST = 0;

    private String field;
    private Seperators seperators;
    protected List<GenericDatatype> genericDatatypes = new ArrayList<>();

    //////////////////  Constructors  //////////////////

    private Field() {
    }

    public Field(String field, Seperators seperators) {
        Validate.notNull(field);
        Validate.notNull(seperators);

        this.field = field;
        this.seperators = seperators;

        this.parse();
    }

    //////////////////  Accessors  //////////////////

    public String getAsString() {
        return field;
    }

    public String getComponent(int componentNumber) {
        GenericDatatype genericDatatype = getFirstGenericDatatype();

        if (genericDatatype == null)
            return null;

        return genericDatatype.getComponent(componentNumber);
    }

    private GenericDatatype getFirstGenericDatatype() {
        return Helpers.getSafely(this.genericDatatypes, FIRST);
    }

    public Datatype getDatatype() {
        return new Datatype(getFirstGenericDatatype());
    }

    public <T extends Datatype> T getDatatype(Class<T> datatype) throws ParseException {
        Validate.notNull(datatype);

        return Datatype.instantiate(datatype, getFirstGenericDatatype());
    }

    public List<Datatype> getDatatypes() throws ParseException {
        List<Datatype> result = new ArrayList<>();

        for (GenericDatatype genericDatatype : this.genericDatatypes)
            result.add(new Datatype(genericDatatype));

        return result;
    }

    public <T extends Datatype> List<T> getDatatypes(Class<T> dt) throws ParseException {
        Validate.notNull(dt);

        List<T> result = new ArrayList<>();

        for (GenericDatatype genericDatatype : this.genericDatatypes)
            result.add(Datatype.instantiate(dt, genericDatatype));

        return result;
    }

    //////////////////  Parsers  //////////////////

    private void parse() {
        if (this.field.equals(this.seperators.getMsh2Field())) {
            this.genericDatatypes.add(new GenericDatatype(this.field, this.seperators));
            return;
        }

        List<String> fieldRepetitions = Helpers.split(this.field, seperators.getRepetitionSeperator());

        for (String fieldRepetition : fieldRepetitions)
            this.genericDatatypes.add(new GenericDatatype(fieldRepetition, this.seperators));
    }
}
