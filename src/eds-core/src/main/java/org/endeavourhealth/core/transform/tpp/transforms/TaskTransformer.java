package org.endeavourhealth.core.transform.tpp.transforms;

import org.endeavourhealth.core.transform.fhir.Fhir;
import org.endeavourhealth.core.transform.fhir.FhirUris;
import org.endeavourhealth.core.transform.tpp.schema.Task;
import org.endeavourhealth.core.transform.tpp.schema.Visit;
import org.hl7.fhir.instance.model.*;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.List;

public class TaskTransformer {

    public static void transform(Task tppTask, List<Resource> fhirResources) {

        Order fhirTask = new Order();
        fhirTask.setMeta(new Meta().addProfile(FhirUris.PROFILE_URI_TASK));
        fhirTask.setId(tppTask.getTaskUID());
        fhirResources.add(fhirTask);

        String taskType = tppTask.getTaskType();
        fhirTask.addExtension(new Extension()
                .setUrl(FhirUris.EXTENSION_URI_TASKTYPE)
                .setValue(new CodeType().setValue(taskType)));


        XMLGregorianCalendar dueDate = tppTask.getDue();
        if (dueDate != null) {
            Timing fhirTiming = new Timing().addEvent(dueDate.toGregorianCalendar().getTime());
            Order.OrderWhenComponent fhirWhen = new Order.OrderWhenComponent().setSchedule(fhirTiming);
            fhirTask.setWhen(fhirWhen);
        }

        String content = tppTask.getContent();
        //TODO - need somewhere in FHIR to store the task content

        //who the task is assigned to isn't relevant to a third party
        //String userNameAssigned = tppTask.getUserNameAssigned();
        //String userGroupAssigned = tppTask.getGroupNameAssigned();

        //link back to the patient
        String patientId = Fhir.findPatientId(fhirResources);
        fhirTask.setSubject(Fhir.createReference(ResourceType.Patient, patientId));

        //TODO - not sure if we can set the required Status and Priority fields on the FHIR resource
    }
}
