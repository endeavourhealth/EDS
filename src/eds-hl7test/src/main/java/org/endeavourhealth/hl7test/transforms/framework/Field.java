package org.endeavourhealth.hl7test.transforms.framework;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Field {
    private String field;
    private Seperators seperators;
    protected List<String> components;

    public Field(String field, Seperators seperators) {
        this.field = field;
        this.seperators = seperators;
    }

    public String getAsString() {
        return field;
    }

    public String getComponent(int componentNumber) {
        int componentIndex = componentNumber - 1;

        if ((componentIndex >= 0) && (componentIndex < (components.size() - 1)))
            return components.get(componentIndex);

        return null;
    }

    private void parse() {
        this.components = new ArrayList<>();

        components = Arrays.stream(StringUtils.split(this.field, seperators.getComponentSeperator()))
                .collect(Collectors.toList());
    }
}
