package org.endeavourhealth.transform.ui.helpers;

import org.endeavourhealth.transform.ui.models.types.UIQuantity;
import org.hl7.fhir.instance.model.Quantity;

public class QuantityHelper {

    public static UIQuantity convert(Quantity quantity) {
        String comparator = null;

        if (quantity.hasComparator())
            comparator = quantity.getComparator().toCode();

        return new UIQuantity()
                .setValue(quantity.getValue().doubleValue())
                .setUnits(quantity.getUnit())
                .setComparator(comparator);
    }
}
