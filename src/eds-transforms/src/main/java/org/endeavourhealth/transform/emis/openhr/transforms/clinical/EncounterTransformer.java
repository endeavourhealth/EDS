package org.endeavourhealth.transform.emis.openhr.transforms.clinical;

import org.apache.commons.lang3.StringUtils;
import org.endeavourhealth.core.utility.StreamExtension;
import org.endeavourhealth.transform.fhir.ReferenceHelper;
import org.endeavourhealth.transform.common.exceptions.TransformException;
import org.endeavourhealth.transform.emis.openhr.schema.*;
import org.endeavourhealth.transform.emis.openhr.transforms.common.DateConverter;
import org.endeavourhealth.transform.emis.openhr.transforms.common.OpenHRHelper;
import org.endeavourhealth.transform.fhir.FhirUri;
import org.hl7.fhir.instance.model.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class EncounterTransformer
{
    private final static String PROBLEM_HEADING_TERM = "Problem";

    //TODO: This concept has been deprecated. There is no alternative to this code
    private final static String DEFAULT_TOPIC_DISPLAY = "Unspecified conditions";
    private final static String DEFAULT_TOPIC_CODE = "315645005";

    static class EncounterSection
    {
        private DtCode heading;
        private List<String> events;

        public DtCode getHeading() {
            return heading;
        }

        public void setHeading(DtCode heading) {
            this.heading = heading;
        }

        public List<String> getEvents() {
            if (events == null) events = new ArrayList<>();
            return events;
        }

        public void setEvents(List<String> events) {
            this.events = events;
        }
    }

    static class EncounterPage {
        private short pageNumber;
        private List<EncounterSection> sections;

        public short getPageNumber() {
            return pageNumber;
        }

        public void setPageNumber(short pageNumber) {
            this.pageNumber = pageNumber;
        }

        public List<EncounterSection> getSections() {
            if (sections == null) sections = new ArrayList<>();
            return sections;
        }

        public void setSections(List<EncounterSection> sections) {
            this.sections = sections;
        }
    }

    public static List<Resource> transform(OpenHR001HealthDomain healthDomain, List<Resource> resources) throws TransformException
    {
        List<Resource> result = new ArrayList<>();

        for (OpenHR001Encounter source: healthDomain.getEncounter())
            result.add(createEncounter(healthDomain, resources, source));

        return result;
    }

    private static Encounter createEncounter(OpenHR001HealthDomain healthDomain, List<Resource> resources, OpenHR001Encounter source) throws TransformException
    {
        OpenHRHelper.ensureDboNotDelete(source);

        Encounter target = new Encounter();
        target.setId(source.getId());
        target.setMeta(new Meta().addProfile(FhirUri.PROFILE_URI_ENCOUNTER));

        target.setStatus(convertStatus(source.isComplete()));
        target.setClass_(Encounter.EncounterClass.AMBULATORY);
        target.addType(convertType(source.getLocationType()));
        target.setPatient(ReferenceHelper.createReference(ResourceType.Patient, source.getPatient()));
        target.addParticipant(createParticipantFromAuthorisingUser(source.getAuthorisingUserInRole()));
        target.setPeriod(new Period().setEndElement(DateConverter.convertPartialDateTimeToDateTimeType(source.getEffectiveTime())));
        target.setServiceProvider(createOrganisationReference(source.getOrganisation()));
        target.addLocation(convertLocation(source.getLocation()));
        target.setLength(convertDuration(source.getDuration()));

        addAccompanyingHCPsAsAttenderParticipants(source.getAccompanyingHCP(), target);

        addComposition(source, target, resources);

        return target;
    }

    private static Encounter.EncounterState convertStatus(boolean isComplete) {
        return (isComplete)
                ? Encounter.EncounterState.FINISHED
                : Encounter.EncounterState.INPROGRESS;
    }

    private static CodeableConcept convertType(OpenHR001LocationType sourceLocationType) {
        return new CodeableConcept()
                .addCoding(new Coding()
                                .setSystem(FhirUri.CODE_SYSTEM_SNOMED_CT)
                                .setCode(sourceLocationType.getCode())
                                .setDisplay(sourceLocationType.getDisplayName())
                );
    }

    private static Reference createOrganisationReference(List<String> sourceOrganisations) throws TransformException
    {
        if (sourceOrganisations == null)
            return null;

        String organisationId = sourceOrganisations.stream()
                .collect(StreamExtension.singleOrNullCollector());

        //if multiple organisations exists, then it will just throw a general exception.

        if (StringUtils.isBlank(organisationId))
            throw new TransformException("Organisation not found");

        return ReferenceHelper.createReference(ResourceType.Organization, organisationId);
    }

    private static Duration convertDuration(DtDuration sourceDuration) {
        if (sourceDuration == null)
            return null;

        Duration target = new Duration();
        target.setSystem(FhirUri.CODE_SYSTEM_SNOMED_CT);
        target.setUnit("minutes");
        target.setCode("258701004");
        target.setValue(new BigDecimal(sourceDuration.getValue()));
        return target;
    }

    private static Encounter.EncounterParticipantComponent createParticipantFromAuthorisingUser(String userInRoleId) throws TransformException
    {
        if (StringUtils.isBlank(userInRoleId))
            throw new TransformException("UserInRoleId not found");

        return new Encounter.EncounterParticipantComponent()
        .addType(new CodeableConcept()
                .addCoding(new Coding()
                        .setSystem("http://hl7.org/fhir/v3/ParticipationType")
                        .setDisplay("consultant")
                        .setCode("CON")))
                .setIndividual(ReferenceHelper.createReference(ResourceType.Practitioner, userInRoleId));
    }

    private static Encounter.EncounterLocationComponent convertLocation(String locationId) throws TransformException
    {
        if (StringUtils.isBlank(locationId))
            return null;

        return new Encounter.EncounterLocationComponent()
            .setLocation(ReferenceHelper.createReference(ResourceType.Location, locationId));
    }

    private static void addAccompanyingHCPsAsAttenderParticipants(List<String> userInRoleIds, Encounter target) throws TransformException {
        if (userInRoleIds == null)
            return;

        for (String userInRoleId: userInRoleIds) {
            target.addParticipant(new Encounter.EncounterParticipantComponent()
                    .addType(new CodeableConcept()
                            .addCoding(new Coding()
                                    .setSystem("http://hl7.org/fhir/v3/ParticipationType")
                                    .setDisplay("attender")
                                    .setCode("ATND")))
                    .setIndividual(ReferenceHelper.createReference(ResourceType.Practitioner, userInRoleId)));
        }
    }

    private static void addComposition(OpenHR001Encounter source, Encounter target, List<Resource> resources) throws TransformException
    {
        Composition composition = createComposition(source, resources);

        if (composition != null)
            target.getContained().add(composition);
    }

    private static Composition createComposition(OpenHR001Encounter source, List<Resource> resources) throws TransformException
    {
        if (source.getComponent() == null || source.getComponent().isEmpty())
            return null;

        Composition composition = new Composition();
        composition.setId("composition");
        composition.setDate(DateConverter.toDate(source.getAvailabilityTimeStamp()));
        composition.setType(new CodeableConcept()
                .setText(source.getLocationType().getDisplayName())
                .addCoding(convertCompositionType(source.getLocationType())));
        composition.setStatus(Composition.CompositionStatus.FINAL);
        composition.setSubject(ReferenceHelper.createReference(ResourceType.Patient, source.getPatient()));
        composition.addAuthor(ReferenceHelper.createReference(ResourceType.Practitioner, source.getAuthorisingUserInRole()));
        composition.setEncounter(ReferenceHelper.createReference(ResourceType.Encounter, source.getId()));

        List<EncounterPage> pages = convertFlatComponentListToPageAndSectionHierarchy(source.getComponent());

        for (EncounterPage page: pages) {
            Composition.SectionComponent topicSection = new Composition.SectionComponent()
                    .setCode(getTopicCodeFromEncounterPage(page, resources));

            for (EncounterSection sourceSection: page.getSections()) {
                Composition.SectionComponent categorySection = new Composition.SectionComponent()
                        .setCode(new CodeableConcept()
                                .addCoding(new Coding()
                                        .setSystem(FhirUri.CODE_SYSTEM_SNOMED_CT)
                                        .setDisplay(sourceSection.getHeading().getDisplayName())
                                        .setCode(sourceSection.getHeading().getCode())));

                categorySection.setOrderedBy(new CodeableConcept()
                        .addCoding(new Coding()
                                .setSystem("http://hl7.org/fhir/list-order")
                                .setDisplay("Sorted by User")
                                .setCode("user")));

                for (String eventId: sourceSection.getEvents()) {
                    categorySection.getEntry().add(createResourceReferenceFromEvent(resources, eventId));
                }

                topicSection.addSection(categorySection);
            }

            composition.addSection(topicSection);
        }

        return composition;
    }

    private static List<EncounterPage> convertFlatComponentListToPageAndSectionHierarchy(List<OpenHR001Component> components) {
        List<EncounterPage> pages = new ArrayList<>();

        EncounterPage currentPage = null;
        EncounterSection currentSection = null;
        for (OpenHR001Component component: components) {
            if (currentPage == null || currentPage.getPageNumber() != component.getProblemPage()) {
                currentPage = new EncounterPage();
                currentPage.setPageNumber(component.getProblemPage());
                pages.add(currentPage);
                currentSection = null;
            }

            if (currentSection == null || !currentSection.getHeading().getDisplayName().equals(component.getHeading().getDisplayName())) {
                currentSection = new EncounterSection();
                currentSection.setHeading(component.getHeading());
                currentPage.getSections().add(currentSection);
            }

            currentSection.getEvents().add(component.getEvent());
        }

        return pages;
    }

    private static CodeableConcept getTopicCodeFromEncounterPage(EncounterPage page, List<Resource> resources)
    {
        CodeableConcept topicCode = null;

        for (EncounterSection section: page.getSections())
        {
            if (section.getHeading().getDisplayName().equals(PROBLEM_HEADING_TERM))
            {
                // Problem sections should only have a single event
                String eventId = section.events.stream()
                        .collect(StreamExtension.singleOrNullCollector());

                if (StringUtils.isNotBlank(eventId))
                {
                    // find event resource in container
                    Resource resource = resources
                            .stream()
                            .filter(t -> eventId.equals(t.getId()))
                            .collect(StreamExtension.singleCollector());

                    topicCode = getCodeFromResource(resource);
                }

                break;
            }
        }

        // if topic (problem) code not found add default
        if (topicCode == null)
        {
            topicCode = new CodeableConcept()
                    .addCoding(new Coding()
                            .setSystem(FhirUri.CODE_SYSTEM_SNOMED_CT)
                            .setDisplay(DEFAULT_TOPIC_DISPLAY)
                            .setCode(DEFAULT_TOPIC_CODE));
        }

        return topicCode;
    }

    private static CodeableConcept getCodeFromResource(Resource resource) {
        if (resource == null)
            return null;

        CodeableConcept result = null;
        if (resource.getResourceType() == ResourceType.Condition) {
            Condition condition = (Condition)resource;
            if (condition.hasCode())
                result = condition.getCode().copy();
        } else if (resource.getResourceType() == ResourceType.Observation) {
            Observation observation = (Observation)resource;
            if (observation.hasCode())
                result = observation.getCode().copy();
        }
        return result;
    }

    private static Coding convertCompositionType(OpenHR001LocationType locationType) {
        switch (locationType.getCode()) {
            // GP2GP EhrComposition Vocabulary Codes
            case "24561000000109": // A+E report
            case "37361000000105": // Additional Note
            case "37351000000107": // Administration Note
            case "37341000000109": // Alert Note
            case "37331000000100": // Comment Note
            case "15611000000104": // Diagnosis
            case "37321000000102": // Community Nursing Note
            case "24571000000102": // Community Nursing Report
            case "25561000000105": // Data transferred from other system
            case "24581000000100": // Day Case Report
            case "25571000000103": // Discharge Report
            case "24591000000103": // Other Report
            case "25581000000101": // Discharge Summary Report
            case "37311000000108": // Out of Hours, Non Practice Note
            case "37301000000106": // Emergency Consultation Note
            case "37281000000105": // Out of Hours, Practice Note
            case "37291000000107": // Externally Entered Note
            case "24601000000109": // Radiology Request
            case "25591000000104": // Follow-up/Routine Visit Note
            case "25601000000105": // G.O.S. 18 Report
            case "24611000000106": // Radiology Result
            case "25611000000107": // Referral Letter
            case "24621000000100": // Health Authority Entry
            case "25631000000104": // Health Visitor Note
            case "25621000000101": // Repeat Issue Note
            case "25641000000108": // Health Visitor Report
            case "24631000000103": // Residential Home Visit Note
            case "25651000000106": // Home Visit Note
            case "24641000000107": // Investigation Result
            case "25661000000109": // Social Services Report
            case "24651000000105": // Hospital Admission Note
            case "24661000000108": // Hospital Inpatient Report
            case "25671000000102": // Surgery Consultation Note
            case "25681000000100": // Hospital Outpatient Report
            case "25691000000103": // Telephone call from a patient
            case "25701000000103": // Hotel Visit Note
            case "24671000000101": // Telephone call to a patient
            case "25711000000101": // Initial post discharge review
            case "24681000000104": // Telephone Consultation
            case "24691000000102": // Laboratory Request
            case "24701000000102": // Laboratory Result
            case "25741000000100": // Third Party Consultation
            case "25731000000109": // Mail from patient
            case "37271000000108": // Twilight Visit Note
            case "24711000000100": // Mail to patient
            case "24721000000106": // Acute Visit Note
            case "24881000000103": // NHS Direct Report
            case "25751000000102": // Children's Home Visit Note
            case "25761000000104": // Night Visit Note
            case "24731000000108": // Clinic Note
            case "25771000000106": // Night visit practice note
            case "25791000000105": // Community Clinic Note
            case "25781000000108": // OOH Report
            case "24741000000104": // Night visit, deputising service note
            case "25801000000109": // Night visit local rota note
            case "109341000000100": // GP to GP communication transaction
            case "24751000000101": // Nursing Home Visit Note
            case "25811000000106": // OOH Attendance Note
                return new Coding().setSystem(FhirUri.CODE_SYSTEM_SNOMED_CT).setDisplay(locationType.getDisplayName()).setCode(locationType.getCode());
            // LocationTypes mapped to valid GP2GP EhrComposition Vocabulary Code
            case "1503371000006105": //	GP Surgery
                return new Coding().setSystem(FhirUri.CODE_SYSTEM_SNOMED_CT).setDisplay("Surgery Consultation Note").setCode("25671000000102");
            case "1809181000006108": //	Emergency consultation
                return new Coding().setSystem(FhirUri.CODE_SYSTEM_SNOMED_CT).setDisplay("Emergency Consultation Note").setCode("37301000000106");
            case "185221008": // Seen in gynaecology clinic
            case "185229005": // Seen in diabetic clinic
            case "185242005": // Seen in asthma clinic
            case "313103006": // Seen in baby clinic
            case "1503121000006107": //	Walk-in clinic
            case "1854731000006104": //	Seen in drug misuse clinic
            case "1839331000006101": //	Seen in chronic obstructive pulmonary disease clinic
                return new Coding().setSystem(FhirUri.CODE_SYSTEM_SNOMED_CT).setDisplay("Clinic Note").setCode("24731000000108");
            case "185317003": // Telephone encounter
            case "1849981000006103": //	Follow up telephone consultation
            case "1849991000006100": //	First telephone consultation
            case "386473003": // Telephone follow-up
            case "401267002": // Telephone triage encounter
                return new Coding().setSystem(FhirUri.CODE_SYSTEM_SNOMED_CT).setDisplay("Telephone Consultation").setCode("24681000000104");
            default:
                //A composition with a name that is not the same as any specified composition name. The originalText element specifies a title for the composition.
                return new Coding().setSystem(FhirUri.CODE_SYSTEM_SNOMED_CT).setDisplay("Other Report").setCode("24591000000103");
        }
    }

    private static Reference createResourceReferenceFromEvent(List<Resource> resources, String eventId) throws TransformException
    {
        // find event resource in container
        // find event resource in container
        Resource resource = resources
                .stream()
                .filter(t -> eventId.equals(t.getId()))
                .collect(StreamExtension.singleOrNullCollector());

        if (resource == null)
            throw new TransformException("Encounter component event resource not found in container. EventId:" + eventId);

        return ReferenceHelper.createReference(resource.getResourceType(), eventId);
    }
}