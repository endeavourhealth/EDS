package org.endeavourhealth.transform.ui.helpers;

import org.apache.commons.lang3.StringUtils;
import org.endeavourhealth.core.utility.StreamExtension;
import org.endeavourhealth.transform.fhir.ReferenceHelper;
import org.endeavourhealth.transform.ui.models.resources.admin.UILocation;
import org.endeavourhealth.transform.ui.models.resources.admin.UIOrganisation;
import org.endeavourhealth.transform.ui.models.resources.admin.UIPractitioner;
import org.endeavourhealth.transform.ui.models.resources.clinicial.UIMedication;
import org.endeavourhealth.transform.ui.models.resources.clinicial.UIObservation;
import org.endeavourhealth.transform.ui.models.types.UICodeableConcept;
import org.endeavourhealth.transform.ui.transforms.admin.UILocationTransform;
import org.endeavourhealth.transform.ui.transforms.admin.UIOrganisationTransform;
import org.endeavourhealth.transform.ui.transforms.admin.UIPractitionerTransform;
import org.endeavourhealth.transform.ui.transforms.clinical.UIMedicationTransform;
import org.endeavourhealth.transform.ui.transforms.clinical.UIObservationTransform;
import org.hl7.fhir.instance.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ReferencedResources {
    private List<Practitioner> practitioners = new ArrayList<>();
    private List<UIPractitioner> uiPractitioners = new ArrayList<>();
    private List<Organization> organisations = new ArrayList<>();
    private List<UIOrganisation> uiOrganisations = new ArrayList<>();
    private List<Location> locations = new ArrayList<>();
    private List<UILocation> uiLocations = new ArrayList<>();
    private List<Medication> medications = new ArrayList<>();
    private List<UIMedication> uiMedications = new ArrayList<>();
    private List<Observation> observations = new ArrayList<>();
    private List<UIObservation> uiObservations = new ArrayList<>();

    public void setPractitioners(List<Practitioner> practitioners) {
        this.practitioners = practitioners;

        this.uiPractitioners = practitioners
                .stream()
                .map(t -> UIPractitionerTransform.transform(t))
                .collect(Collectors.toList());
    }

    public UIPractitioner getUIPractitioner(Reference reference) {
        String referenceId = ReferenceHelper.getReferenceId(reference, ResourceType.Practitioner);

        if (StringUtils.isEmpty(referenceId))
            return null;

        return this
                .uiPractitioners
                .stream()
                .filter(t -> referenceId.equals(t.getId()))
                .collect(StreamExtension.firstOrNullCollector());
    }

    public UIOrganisation getUIOrganisation(Reference reference) {
        String referenceId = ReferenceHelper.getReferenceId(reference, ResourceType.Organization);

        if (StringUtils.isEmpty(referenceId))
            return null;

        return this
                .uiOrganisations
                .stream()
                .filter(t -> t.getId().equals(referenceId))
                .collect(StreamExtension.firstOrNullCollector());
    }

    public void setOrganisations(List<Organization> organisations) {
        this.organisations = organisations;

        this.uiOrganisations = organisations
                .stream()
                .map(t -> UIOrganisationTransform.transform(t))
                .collect(Collectors.toList());
    }

    public void setLocations(List<Location> locations) {
        this.locations = locations;

        this.uiLocations = locations
                .stream()
                .map(t -> UILocationTransform.transform(t))
                .collect(Collectors.toList());
    }

    public void setMedications(List<Medication> medications) {
        this.medications = medications;

        this.uiMedications = medications
            .stream()
            .map(t -> UIMedicationTransform.transform(t))
            .collect(Collectors.toList());
    }

    public UIMedication getUIMedication(Reference reference) {
        String referenceId = ReferenceHelper.getReferenceId(reference, ResourceType.Medication);

        if (StringUtils.isEmpty(referenceId))
            return null;

        return this
            .uiMedications
            .stream()
            .filter(t -> t.getId().equals(referenceId))
            .collect(StreamExtension.firstOrNullCollector());
    }

    public void setObservations(List<Observation> observations, ReferencedResources referencedResources) {
        this.observations = observations;
        this.uiObservations = observations
            .stream()
            .map(t -> UIObservationTransform.transform(t, referencedResources))
            .collect(Collectors.toList());
    }

    public UIObservation getUIObservation(Reference reference) {
        String referenceId = ReferenceHelper.getReferenceId(reference, ResourceType.Observation);

        if (StringUtils.isEmpty(referenceId))
            return null;

        return this
            .uiObservations
            .stream()
            .filter(t -> t.getId().equals(referenceId))
            .collect(StreamExtension.firstOrNullCollector());
    }
}