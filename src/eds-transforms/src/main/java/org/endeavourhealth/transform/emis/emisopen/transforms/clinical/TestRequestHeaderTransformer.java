package org.endeavourhealth.transform.emis.emisopen.transforms.clinical;

import com.google.common.base.Strings;
import org.endeavourhealth.transform.common.exceptions.TransformException;
import org.endeavourhealth.transform.emis.csv.EmisCsvHelper;
import org.endeavourhealth.transform.emis.emisopen.EmisOpenHelper;
import org.endeavourhealth.transform.emis.emisopen.schema.eommedicalrecord38.*;
import org.endeavourhealth.transform.emis.emisopen.transforms.common.CodeConverter;
import org.endeavourhealth.transform.emis.emisopen.transforms.common.DateConverter;
import org.endeavourhealth.common.fhir.FhirUri;
import org.endeavourhealth.common.fhir.ReferenceHelper;
import org.hl7.fhir.instance.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TestRequestHeaderTransformer extends ClinicalTransformerBase {

    private static final Logger LOG = LoggerFactory.getLogger(TestRequestHeaderTransformer.class);

    public static void transform(MedicalRecordType medicalRecord, List<Resource> resources, String patientGuid) throws TransformException {

        MedicalRecordType.TestRequestHeaderList testRequestList = medicalRecord.getTestRequestHeaderList();
        if (testRequestList == null) {
            return;
        }

        for (TestRequestHeaderType testRequest : testRequestList.getTestRequestHeader()) {
            transform(testRequest, resources, patientGuid);
        }
    }

    public static void transform(TestRequestHeaderType testRequest, List<Resource> resources, String patientGuid) throws TransformException {

        DiagnosticOrder fhirOrder = new DiagnosticOrder();
        fhirOrder.setMeta(new Meta().addProfile(FhirUri.PROFILE_URI_DIAGNOSTIC_ORDER));

        String requestGuid = testRequest.getGUID();
        EmisCsvHelper.setUniqueId(fhirOrder, patientGuid, requestGuid);

        fhirOrder.setSubject(EmisOpenHelper.createPatientReference(patientGuid));

        IdentType requestor = testRequest.getRequestor();
        if (requestor != null) {
            fhirOrder.setOrderer(EmisOpenHelper.createPractitionerReference(requestor.getGUID()));
        }

        IdentType consultationRef = testRequest.getConsultationID();
        if (consultationRef != null) {
            fhirOrder.setEncounter(EmisOpenHelper.createEncounterReference(consultationRef.getGUID(), patientGuid));
        }

        Date requestedDate = DateConverter.getDate(testRequest.getDateCreated());
        if (requestedDate != null) {
            DiagnosticOrder.DiagnosticOrderEventComponent diagnosticOrderEvent = fhirOrder.addEvent();
            diagnosticOrderEvent.setDateTime(requestedDate);

            //set the initial requested status on both the an event and the order itself
            diagnosticOrderEvent.setStatus(DiagnosticOrder.DiagnosticOrderStatus.REQUESTED);
            fhirOrder.setStatus(DiagnosticOrder.DiagnosticOrderStatus.REQUESTED);
        }

        Object statusObj = testRequest.getStatus();
        if (statusObj != null) {
            String status = statusObj.toString(); //status is an OBJECT, but just treat as a String
            if (!Strings.isNullOrEmpty(status)) {

                DiagnosticOrder.DiagnosticOrderEventComponent diagnosticOrderEvent = fhirOrder.addEvent();

                DiagnosticOrder.DiagnosticOrderStatus fhirStatus = convertStatus(status);
                if (fhirStatus != null) {
                    diagnosticOrderEvent.setStatus(fhirStatus);
                    fhirOrder.setStatus(fhirStatus);
                }

                Date statusDate = DateConverter.getDate(testRequest.getLastStatusDate());
                if (statusDate != null) {
                    diagnosticOrderEvent.setDateTime(statusDate);
                }
            }
        }

        /**
         protected String dateForTest;
         protected String dateLastXRay;
         protected String copyTo;
         protected BigInteger nhs;
         protected BigInteger priority;
         protected BigInteger innoculationRisk;
         protected BigInteger fasted;
         protected String lastMenstualPeriod;
         protected String clinicalInformation;
         protected BigInteger pregnant;
         */

        List<Specimen> specimens = new ArrayList<>();

        TestRequestHeaderType.EDIOrderList orderList = testRequest.getEDIOrderList();
        if (orderList != null) {

            for (EDIOrderType order: orderList.getEDIOrder()) {
                EDIOrderType.TestRequestList requestList = order.getTestRequestList();
                if (requestList != null) {

                    DiagnosticOrder.DiagnosticOrderStatus orderStatus = convertStatus(order.getStatus());
                    Date orderStatusDate = DateConverter.getDate(order.getLastStatusDate());

                    for (TestRequestType request: requestList.getTestRequest()) {

                        DiagnosticOrder.DiagnosticOrderItemComponent orderItem = fhirOrder.addItem();
                        orderItem.setCode(CodeConverter.convert(request.getCode(), request.getDisplayTerm()));

                        DiagnosticOrder.DiagnosticOrderStatus requestStatus = convertStatus(request.getStatus());
                        Date requestStatusDate = DateConverter.getDate(request.getLastStatusDate());

                        addStatusToOrderItem(orderItem, orderStatus, orderStatusDate, requestStatus, requestStatusDate);

                        //and create a specimen resource if necessary
                        transformSpecimen(request, specimens, patientGuid);
                    }
                }
            }
        }

        for (Specimen specimen: specimens) {
            Reference reference = createReferenceExternal(specimen);
            fhirOrder.addSpecimen(reference);
        }

        resources.add(fhirOrder);
        resources.addAll(specimens);
    }

    private static void transformSpecimen(TestRequestType request, List<Specimen> specimens, String patientGuid) throws TransformException {

        //if there are no specimen details, then don't create a specimen
        if (request.getSpecimenTypeCode() == null
                && Strings.isNullOrEmpty(request.getSpecimenType())) {
            return;
        }

        Specimen fhirSpecimen = new Specimen();
        fhirSpecimen.setMeta(new Meta().addProfile(FhirUri.PROFILE_URI_SPECIMIN));

        String requestGuid = request.getGUID();
        EmisCsvHelper.setUniqueId(fhirSpecimen, patientGuid, requestGuid);

        fhirSpecimen.setSubject(EmisOpenHelper.createPatientReference(patientGuid));

        fhirSpecimen.setType(CodeConverter.convert(request.getSpecimenTypeCode(), request.getSpecimenType()));

        Specimen.SpecimenCollectionComponent collection = new Specimen.SpecimenCollectionComponent();
        fhirSpecimen.setCollection(collection);

        IdentType collector = request.getAuthorID();
        if (collector != null) {
            collection.setCollector(EmisOpenHelper.createPractitionerReference(collector.getGUID()));
        }

        Date collectionDate = DateConverter.getDate(request.getCollectionDate());
        if (collectionDate != null) {
            collection.setCollected(new DateTimeType(collectionDate));
        }

        specimens.add(fhirSpecimen);
    }

    private static DiagnosticOrder.DiagnosticOrderStatus convertStatus(String status) {
        if (Strings.isNullOrEmpty(status)) {
            return null;
        }

        try {
            return DiagnosticOrder.DiagnosticOrderStatus.fromCode(status);

        } catch (Exception ex) {
            LOG.warn("Unmapped test request status " + status);
            return null;
        }
    }

    private static void addStatusToOrderItem(DiagnosticOrder.DiagnosticOrderItemComponent orderItem,
                                             DiagnosticOrder.DiagnosticOrderStatus orderStatus, Date orderStatusDate,
                                             DiagnosticOrder.DiagnosticOrderStatus requestStatus, Date requestStatusDate) {
        if (requestStatus != null && orderStatus != null) {

            //if we have a status on the order and the request, use the most recent one
            if (requestStatusDate != null && orderStatusDate != null) {
                if (requestStatusDate.after(orderStatusDate)) {
                    addStatusToOrderItem(orderItem, requestStatus, requestStatusDate);

                } else {
                    addStatusToOrderItem(orderItem, orderStatus, orderStatusDate);
                }

            } else if (requestStatusDate != null) {
                addStatusToOrderItem(orderItem, requestStatus, requestStatusDate);

            } else {
                addStatusToOrderItem(orderItem, orderStatus, orderStatusDate);
            }

        } else if (requestStatus != null) {
            addStatusToOrderItem(orderItem, requestStatus, requestStatusDate);

        } else if (orderStatus != null) {
            addStatusToOrderItem(orderItem, orderStatus, orderStatusDate);

        } else {
            //we have no status for this item
        }
    }

    private static void addStatusToOrderItem(DiagnosticOrder.DiagnosticOrderItemComponent orderItem,
                                             DiagnosticOrder.DiagnosticOrderStatus status,
                                             Date statusDate) {
        orderItem.setStatus(status);

        if (statusDate != null) {
            DiagnosticOrder.DiagnosticOrderEventComponent orderItemEvent = orderItem.addEvent();
            orderItemEvent.setStatus(status);
            orderItemEvent.setDateTime(statusDate);
        }
    }

    private static Reference createReferenceExternal(Resource resource) throws TransformException {
        try {
            return ReferenceHelper.createReferenceExternal(resource);
        } catch (org.endeavourhealth.common.exceptions.TransformException e) {
            throw new TransformException("Error creating reference, see cause", e);
        }
    }
}
