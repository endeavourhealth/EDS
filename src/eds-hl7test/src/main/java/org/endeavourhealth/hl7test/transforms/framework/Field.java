package org.endeavourhealth.hl7test.transforms.framework;

import org.apache.commons.lang3.Validate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Field {
    private static final int FIRST = 0;

    private String field;
    private Seperators seperators;
    protected List<List<String>> repetitionsAndComponents = new ArrayList<>();

    private Field() {
    }

    public Field(String field, Seperators seperators) {
        Validate.notNull(field);
        Validate.notNull(seperators);

        this.field = field;
        this.seperators = seperators;

        this.parse();
    }

    public String getAsString() {
        return field;
    }

    public String getComponent(int componentNumber) {
        int componentIndex = componentNumber - 1;
        return Helpers.getSafely(Helpers.getSafely(this.repetitionsAndComponents, FIRST), componentIndex);
    }

    private void parse() {
        if (this.field.equals(this.seperators.getMsh2Field())) {
            this.repetitionsAndComponents = Arrays.asList(new List[]{(Arrays.asList(new String[]{this.field}))});
            return;
        }

        List<String> fieldRepetitions = Helpers.split(this.field, seperators.getRepetitionSeperator());

        for (String fieldRepetition : fieldRepetitions)
            this.repetitionsAndComponents.add(Helpers.split(fieldRepetition, seperators.getComponentSeperator()));
    }
}
