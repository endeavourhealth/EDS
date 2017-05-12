package org.endeavourhealth.transform.ui.transforms;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;
import org.endeavourhealth.core.data.admin.models.Service;
import org.endeavourhealth.transform.common.exceptions.TransformException;
import org.endeavourhealth.transform.ui.models.resources.UIResource;
import org.endeavourhealth.transform.ui.models.resources.admin.UIPatient;
import org.endeavourhealth.transform.ui.models.resources.clinicial.*;
import org.endeavourhealth.transform.ui.models.types.UIService;
import org.endeavourhealth.transform.ui.transforms.admin.UIPatientTransform;
import org.endeavourhealth.transform.ui.transforms.clinical.*;
import org.hl7.fhir.instance.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class UITransform {
    public static UIPatient transformPatient(Patient patient) {
        return UIPatientTransform.transform(patient);
    }

    public static <T extends UIResource> UIClinicalTransform getClinicalTransformer(Class<T> resourceType) {

        if (resourceType == UICondition.class)
            return new UIConditionTransform();
        else if (resourceType == UIProblem.class)
            return new UIProblemTransform();
        else if (resourceType == UIEncounter.class)
            return new UIEncounterTransform();
        else if (resourceType == UIObservation.class)
            return new UIObservationTransform();
        else if (resourceType == UIAllergyIntolerance.class)
            return new UIAllergyIntoleranceTransform();
        else if (resourceType == UIImmunisation.class)
            return new UIImmunisationTransform();
        else if (resourceType == UIProcedure.class)
            return new UIProcedureTransform();
        else if (resourceType == UIDiary.class)
            return new UIDiaryTransform();
        else if (resourceType == UIMedicationStatement.class)
            return new UIMedicationStatementTransform();
        else if (resourceType == UIMedicationOrder.class)
        		return new UIMedicationOrderTransform();
        else if (resourceType == UIFamilyMemberHistory.class)
            return new UIFamilyMemberHistoryTransform();
        else if (resourceType == UIEpisodeOfCare.class)
            return new UIEpisodeOfCareTransform();

        throw new NotImplementedException(resourceType.getSimpleName());
    }

    public static List<UIService> transformServices(List<Service> services) throws TransformException {
        List<UIService> result = new ArrayList<>();

        for (Service service : services) {
            UIService uiService = transformService(service);

            if (uiService != null)
                result.add(uiService);
        }

        return result;
    }

    public static UIService transformService(Service service) throws TransformException {
        try {
            if (service == null)
                return null;

            if (StringUtils.isBlank(service.getEndpoints()))
                return null;

            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readValue(service.getEndpoints(), JsonNode.class);

            String systemUuidString = null;
            UUID systemUuid = null;

            if (rootNode.has(0)) {
                if (rootNode.get(0).has("systemUuid")) {
                    systemUuidString = rootNode.get(0).get("systemUuid").asText().replace("\"", "");

                    if (StringUtils.isNotBlank(systemUuidString))
                        systemUuid = UUID.fromString(systemUuidString);
                }
            }

            if (systemUuid == null)
                return null;

            return new UIService()
                    .setName(service.getName())
                    .setLocalIdentifier(service.getLocalIdentifier())
                    .setServiceId(service.getId())
                    .setSystemId(systemUuid);

        } catch (Exception e) {
            throw new TransformException("Transform exception occurred - please see inner exception", e);
        }
    }

}
