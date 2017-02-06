package org.endeavourhealth.transform.hl7v2.parser;

import org.apache.commons.lang3.Validate;

import java.util.ArrayList;
import java.util.List;

public class GenericDatatype {
    private static final int FIRST = 0;

    private String datatype;
    private Seperators seperators;
    protected List<String> components = new ArrayList<>();

    //////////////////  Constructors  //////////////////

    private GenericDatatype() {
    }

    public GenericDatatype(String datatype, Seperators seperators) {
        Validate.notNull(datatype);
        Validate.notNull(seperators);

        this.datatype = datatype;
        this.seperators = seperators;

        this.parse();
    }

    //////////////////  Accessors  //////////////////

    public String getAsString() {
        return this.datatype;
    }

    public String getComponent(int componentNumber) {
        int componentIndex = componentNumber - 1;
        return Helpers.getSafely(this.components, componentIndex);
    }

    //////////////////  Parsers  //////////////////

    private void parse() {
        if (this.datatype.equals(this.seperators.getMsh2Field())) {
            this.components.add(this.datatype);
            return;
        }

        this.components = Helpers.split(this.datatype, seperators.getComponentSeperator());
    }
}
