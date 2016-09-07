package org.endeavourhealth.transform.ceg.transforms;

import org.endeavourhealth.transform.ceg.models.AbstractModel;
import org.endeavourhealth.transform.ceg.models.Encounter;
import org.hl7.fhir.instance.model.Observation;
import org.hl7.fhir.instance.model.Quantity;
import org.hl7.fhir.instance.model.Reference;
import org.hl7.fhir.instance.model.Resource;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class ObservationTransformer extends AbstractTransformer {

    public static void transform(Observation fhir,
                                 List<AbstractModel> models,
                                 Map<String, Resource> hsAllResources) throws Exception {

        Encounter model = new Encounter();

        model.setPatientId(transformPatientId(fhir.getSubject()));
        model.setEventDate(transformDate(fhir.getEffective()));

        findClinicalCodesForEncounter(fhir.getCode(), model);
        findConsultationDetailsForEncounter(fhir.getEncounter(), hsAllResources, model);

        if (fhir.hasValue()) {
            Quantity value = fhir.getValueQuantity();
            BigDecimal num = value.getValue();
            String unit = value.getUnit();
            model.setValue(new Double(num.doubleValue()));
            model.setUnits(unit);
        }

        if (fhir.hasPerformer()) {
            for (Reference ref: fhir.getPerformer()) {
                model.setStaffId(transformStaffId(ref));
            }
        }

//TODO - finish observations
        /**
         private Integer problemId;
         */

        models.add(model);
    }
}
