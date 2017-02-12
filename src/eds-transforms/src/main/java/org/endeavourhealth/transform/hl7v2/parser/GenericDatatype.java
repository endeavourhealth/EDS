package org.endeavourhealth.transform.hl7v2.parser;

import org.apache.commons.lang3.Validate;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class GenericDatatype {
    private static final int FIRST = 0;

    private String originalDatatypeText;    // originalDatatypeText may not reflect the current state of the datatype
    private Seperators seperators;
    protected List<String> components = new ArrayList<>();

    //////////////////  Constructors  //////////////////

    private GenericDatatype() {
    }

    public GenericDatatype(String datatypeText, Seperators seperators) {
        Validate.notNull(datatypeText);
        Validate.notNull(seperators);

        this.originalDatatypeText = datatypeText;
        this.seperators = seperators;

        this.parse();
    }

    //////////////////  Accessors  //////////////////

    public String getAsString() {
        return this.compose();
    }

    public String getComponent(int componentNumber) {
        int componentIndex = componentNumber - 1;
        return Helpers.getSafely(this.components, componentIndex);
    }

    public List<String> getComponents() {
        return this.components;
    }

    //////////////////  Parsers  //////////////////

    private void parse() {
        if (this.originalDatatypeText.equals(this.seperators.getMsh2Field())) {
            this.components.add(this.originalDatatypeText);
            return;
        }

        this.components = Helpers.split(this.originalDatatypeText, seperators.getComponentSeperator());
    }

    //////////////////  Composers  //////////////////

    public String compose() {
        return String.join(this.seperators.getComponentSeperator(),
                this
                        .getComponents()
                        .stream()
                        .map(t -> t)
                        .collect(Collectors.toList()));
    }
}
