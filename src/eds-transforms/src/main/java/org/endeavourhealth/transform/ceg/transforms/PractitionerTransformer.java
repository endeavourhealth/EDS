package org.endeavourhealth.transform.ceg.transforms;

import org.endeavourhealth.transform.ceg.models.AbstractModel;
import org.endeavourhealth.transform.ceg.models.Staff;
import org.hl7.fhir.instance.model.Coding;
import org.hl7.fhir.instance.model.HumanName;
import org.hl7.fhir.instance.model.Practitioner;

import java.util.List;

public class PractitionerTransformer extends AbstractTransformer {

    public static void transform(Practitioner fhir, List<AbstractModel> models) throws Exception {

        Staff model = new Staff();

        model.setStaffId(transformStaffId(fhir.getId()));

        HumanName name = fhir.getName();
        model.setAuthorisingUser(name.getText());

        if (fhir.hasPractitionerRole()) {
            Practitioner.PractitionerPractitionerRoleComponent role = fhir.getPractitionerRole().get(0);
            for (Coding coding: role.getRole().getCoding()) {
                String roleDesc = coding.getDisplay();
                model.setAuthorisingRole(roleDesc);
            }
        }

        models.add(model);
    }
}
