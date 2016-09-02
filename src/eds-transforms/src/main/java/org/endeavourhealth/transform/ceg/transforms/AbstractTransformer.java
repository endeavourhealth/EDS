package org.endeavourhealth.transform.ceg.transforms;

import org.endeavourhealth.core.data.ehr.ResourceRepository;
import org.endeavourhealth.transform.common.exceptions.TransformException;
import org.endeavourhealth.transform.fhir.FhirUri;
import org.endeavourhealth.transform.fhir.ReferenceComponents;
import org.endeavourhealth.transform.fhir.ReferenceHelper;
import org.hl7.fhir.instance.model.*;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

public class AbstractTransformer {

    protected static void findConsultationDetailsForEncounter(Reference encounterReference,
                                                              Map<String, Resource> hsAllResources,
                                                              org.endeavourhealth.transform.ceg.models.Encounter model) throws Exception {

        if (encounterReference == null) {
            return;
        }

        Encounter fhirEncounter = (Encounter)findResource(encounterReference, hsAllResources);
        if (fhirEncounter == null) {
            return;
        }

        if (!fhirEncounter.hasAppointment()) {
            return;
        }

        Reference appointmentReference = fhirEncounter.getAppointment();
        Appointment fhirAppointment = (Appointment)findResource(appointmentReference, hsAllResources);
        if (fhirAppointment == null) {
            return;
        }

        if (fhirAppointment.hasMinutesDuration()) {
            int duration = fhirAppointment.getMinutesDuration();
            model.setConsultationDuration(new Integer(duration));
        }

        if (!fhirAppointment.hasSlot()) {
            return;
        }

        Reference slotReference = fhirAppointment.getSlot().get(0);
        Slot fhirSlot = (Slot)findResource(slotReference, hsAllResources);
        if (fhirSlot == null) {
            return;
        }

        if (!fhirSlot.hasSchedule()) {
            return;
        }

        Reference scheduleReference = fhirSlot.getSchedule();
        Schedule fhirSchedule = (Schedule)findResource(scheduleReference, hsAllResources);
        if (fhirSchedule == null) {
            return;
        }

        if (!fhirSchedule.hasType()) {
            return;
        }

        CodeableConcept type = fhirSchedule.getType().get(0);
        String typeDesc = type.getText();
        model.setConsultationType(typeDesc);
    }

    protected static void findClinicalCodesForEncounter(CodeableConcept codeableConcept,
                                                        org.endeavourhealth.transform.ceg.models.Encounter model) {
        for (Coding coding: codeableConcept.getCoding()) {
            if (coding.getSystem().equals(FhirUri.CODE_SYSTEM_SNOMED_CT)) {
                String value = coding.getCode();
                model.setSnomedConceptCode(Long.parseLong(value));
            } else {
                String value = coding.getCode();
                model.setNativeClinicalCode(value);
            }
        }

    }

    protected static Resource findResource(Reference reference, Map<String, Resource> hsAllResources) throws Exception {
        String referenceStr = reference.getReference();

        //look in our resources map first
        Resource ret = hsAllResources.get(referenceStr);
        if (ret == null) {

            //if not in our map, then hit the DB
            ReferenceComponents comps = ReferenceHelper.getReferenceComponents(reference);
            if (comps != null) {
                ret = new ResourceRepository().getCurrentVersionAsResource(comps.getResourceType(), comps.getId());
            }
        }
        return ret;
    }

    protected static Type findExtension(DomainResource resource, String url) {

        if (resource.hasExtension()) {
            for (Extension extension: resource.getExtension()) {
                if (extension.getUrl().equals(url)) {
                    return extension.getValue();
                }
            }
        }

        return null;
    }

    public static BigInteger transformId(String id) {
        UUID uuid = UUID.fromString(id);
        ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());
        return new BigInteger(1, bb.array());
    }

    protected static BigInteger transformPatientId(Reference reference) {
        ReferenceComponents comps = ReferenceHelper.getReferenceComponents(reference);
        if (comps.getResourceType() == ResourceType.Patient) {
            String id = comps.getId();
            return transformPatientId(id);
        } else {
            return null;
        }
    }
    protected static BigInteger transformPatientId(String id) {
        return transformId(id);
    }

    protected static BigInteger transformStaffId(Reference reference) {
        ReferenceComponents comps = ReferenceHelper.getReferenceComponents(reference);
        if (comps == null) {
            return null; //todo - investigate why a Condition has a null practitioner
        }
        if (comps.getResourceType() == ResourceType.Practitioner) {
            String id = comps.getId();
            return transformStaffId(id);
        } else {
            return null;
        }
    }
    protected static BigInteger transformStaffId(String id) {
        return transformId(id);
    }

    protected static Date transformDate(Type type) throws Exception {

        if (type == null) {
            return null;
        }

        if (type instanceof Age) {
            throw new TransformException("Cannot transform Age to Date");

        } else if (type instanceof DateTimeType) {
            DateTimeType dt = (DateTimeType)type;
            return dt.getValue();

        } else if (type instanceof Period) {
            Period period = (Period)type;
            if (period.hasStart()) {
                return period.getStart();
            } else {
                return null;
            }

        } else if (type instanceof Range) {
            throw new TransformException("Cannot transform Range to Date");

        } else if (type instanceof StringType) {
            throw new TransformException("Cannot transform StringType to Date");

        } else {
            throw new TransformException("Unsupported type to convert to date " + type.getClass());
        }

    }
}
