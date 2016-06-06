package org.endeavourhealth.transform.fhir;

import org.hl7.fhir.instance.model.Quantity;
import org.hl7.fhir.instance.model.SimpleQuantity;

import java.math.BigDecimal;

public class QuantityHelper {

    public static Quantity createQuantity(Double value, String unit) {
        if (value == null) {
            return null;
        }

        return new Quantity().setValue(BigDecimal.valueOf(value)).setUnit(unit);
    }

    public static SimpleQuantity createSimpleQuantity(Double value, String unit, Quantity.QuantityComparator comparator) {
        if (value == null) {
            return null;
        }

        SimpleQuantity q = new SimpleQuantity();
        q.setValue(BigDecimal.valueOf(value)).setUnit(unit).setComparator(comparator);
        return q;
    }
}
