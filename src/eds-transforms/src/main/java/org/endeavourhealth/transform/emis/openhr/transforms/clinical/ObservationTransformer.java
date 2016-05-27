package org.endeavourhealth.transform.emis.openhr.transforms.clinical;

import org.apache.commons.lang3.StringUtils;
import org.endeavourhealth.transform.fhir.ReferenceHelper;
import org.endeavourhealth.transform.common.TransformException;
import org.endeavourhealth.transform.emis.openhr.schema.*;
import org.endeavourhealth.transform.emis.openhr.transforms.common.CodeConverter;
import org.endeavourhealth.transform.emis.openhr.transforms.common.DateConverter;
import org.endeavourhealth.transform.emis.openhr.transforms.common.EventEncounterMap;
import org.endeavourhealth.transform.fhir.FhirUri;
import org.hl7.fhir.instance.model.*;

import java.math.BigDecimal;
import java.util.List;

public class ObservationTransformer implements ClinicalResourceTransformer
{
    private final static String CERTAINTY_QUALIFIER_NAME = "Certainty";
    private final static String UNCERTAIN_QUALIFIER_VALUE = "Uncertain";

    private final static String EPISODICITY_EXTENSION = "urn:fhir.nhs.uk:extension/Episodicity";
    private final static String EPISODICITY_SYSTEM = "urn:fhir.nhs.uk:vs/Episodicity";


    public Resource transform(OpenHR001HealthDomain.Event source, OpenHR001HealthDomain healthDomain, EventEncounterMap eventEncounterMap) throws TransformException
    {
        Observation target = new Observation();
        target.setId(source.getId());
        target.setMeta(new Meta().addProfile(FhirUri.PROFILE_URI_OBSERVATION));

        target.setCode(CodeConverter.convertCode(source.getCode(), source.getDisplayTerm()));
        target.setEffective(DateConverter.convertPartialDateTimeToDateTimeType(source.getEffectiveTime()));
        target.setIssued(DateConverter.toDate(source.getAvailabilityTimeStamp()));
        target.setStatus(Observation.ObservationStatus.FINAL);
        target.setSubject(ReferenceHelper.createReference(ResourceType.Patient, source.getPatient()));

        if (!StringUtils.isBlank(source.getAuthorisingUserInRole()))
            target.addPerformer(ReferenceHelper.createReference(ResourceType.Practitioner, source.getAuthorisingUserInRole()));

        target.setEncounter(eventEncounterMap.getEncounterReference(source.getId()));

        convertAssociatedText(source, target);

        // process observation event specific data
        OpenHR001Observation sourceObservation = source.getObservation();
        if (sourceObservation != null) {
            target.setInterpretation(convertAbnormality(sourceObservation));
            convertValue(sourceObservation.getValue(), target);
            addRelatedObservations(sourceObservation.getComplexObservation(), source.getCode(), target);

            addEpisodicity(sourceObservation.getEpisodicity(), target);
        }

        return target;
    }

    private void convertAssociatedText(OpenHR001Event source, Observation target) throws TransformException
    {
        List<OpenHR001Event.AssociatedText> associatedTextList = source.getAssociatedText();

        if (associatedTextList == null)
            return;

        long distinctCount = associatedTextList.stream()
                .map(a -> a.getAssociatedTextType())
                .distinct()
                .count();

        if (distinctCount != associatedTextList.size())
            throw new TransformException("AssociatedText contains duplicate types");

        for (OpenHR001Event.AssociatedText associatedText: source.getAssociatedText()) {

            VocAssociatedTextType type = associatedText.getAssociatedTextType();
            String value = StringUtils.trimToNull(associatedText.getValue());

            if (value == null)
                continue;

            if (type == VocAssociatedTextType.POST) {
                target.setComments(value);
            } else {
                //TODO: Add Extension for non post text associated text
            }
        }
    }

    private CodeableConcept convertAbnormality(OpenHR001Observation source)
    {
        if (source == null || source.getAbnormal() == null || !source.getAbnormal().isValue())
            return null;

        CodeableConcept result = new CodeableConcept()
                .setText(source.getAbnormal().getDescription());

        switch (source.getAbnormal().getDescription().toUpperCase())
        {
            //High
            case "HIGH":
            case "HI":
            case "SH":
            case "H":
            case "+":
                result.addCoding(new Coding()
                        .setSystem("http://hl7.org/fhir/v2/0078")
                        .setCode("H")
                        .setDisplay("Above high normal"));
                break;
            //Low
            case "LOW":
            case "LO":
            case "SL":
            case "L":
            case "-":
                result.addCoding(new Coding()
                        .setSystem("http://hl7.org/fhir/v2/0078")
                        .setCode("L")
                        .setDisplay("Below low normal"));
                break;
            //Outside Reference Range
            case "OR":
            //Probably Abnormal
            case "PA":
            default:
                result.addCoding(new Coding()
                        .setSystem("http://hl7.org/fhir/v2/0078")
                        .setCode("A")
                        .setDisplay("Abnormal"));
                break;
        }

        return result;
    }

    private void convertValue(OpenHR001ObservationValue sourceValue, Observation target) throws TransformException
    {
        if (sourceValue == null)
            return;

        if (sourceValue.getNumeric() != null) {
            target.setValue(new Quantity()
                    .setValue(sourceValue.getNumeric().getValue())
                    .setUnit(sourceValue.getNumeric().getUnits())
                    .setComparator(convertQuantityComparator(sourceValue.getNumeric().getOperator())));
            target.addReferenceRange(convertRange(sourceValue.getNumeric().getRange()));
        }
        else if (sourceValue.getText() != null) {
            target.setValue(new StringType()
                    .setValue(sourceValue.getText().getValue()));
            target.addReferenceRange(convertRange(sourceValue.getText().getRange()));
        }
        else
            throw new TransformException("Invalid Observation Value: Missing Numeric and Text Value");
    }

    private Quantity.QuantityComparator convertQuantityComparator(String operator) throws TransformException {
        if (StringUtils.isBlank(operator))
            return null;

        switch (operator) {
            case ">":
                return Quantity.QuantityComparator.GREATER_THAN;
            case ">=":
                return Quantity.QuantityComparator.GREATER_OR_EQUAL;
            case "<":
                return Quantity.QuantityComparator.LESS_THAN;
            case "<=":
                return Quantity.QuantityComparator.GREATER_OR_EQUAL;
            default:
                throw new TransformException("Invalid Numeric Operator: " + operator);
        }
    }

    private Observation.ObservationReferenceRangeComponent convertRange(OpenHR001Range sourceRange) throws TransformException
    {
        if (sourceRange == null)
            return null;

        Observation.ObservationReferenceRangeComponent rangeComponent = new Observation.ObservationReferenceRangeComponent();

        if (sourceRange.getNumericRange() != null) {
            rangeComponent.setLow(convertNumericRange(sourceRange.getNumericRange().getLow(), sourceRange.getUnits()));
            rangeComponent.setHigh(convertNumericRange(sourceRange.getNumericRange().getHigh(), sourceRange.getUnits()));
        }
        else if (sourceRange.getTextRange() != null) {
            //TODO: Text Range - Text range High and Low values can not be mapped, consider extension
        }
        else {
            rangeComponent.setText(sourceRange.getDescription());
        }

        rangeComponent.setAge(convertAgeRange(sourceRange.getAgeRange()));

        return rangeComponent;
    }

    private SimpleQuantity convertNumericRange(OpenHR001RangeValue rangeValue, String units) throws TransformException
    {
        if (rangeValue == null)
            return null;

        SimpleQuantity quantity = new SimpleQuantity();
        quantity.setValue(rangeValue.getValue());
        quantity.setUnit(units);

        //TODO: Check rangeValue.getOperator() - SimpleQuantity does not allow Comparator
        //quantity.setComparator(convertQuantityComparator(rangeValue.getOperator()));

        return quantity;
    }

    private void addRelatedObservations(OpenHR001ComplexObservation complexObservation, DtCodeQualified sourceCode, Observation target) throws TransformException
    {
        if (complexObservation == null)
            return;

        //TODO: Blood Pressures (Change from DSTU2 Ballot to DSTU2 QA Preview) - Child Observation should be added as components

        for (String relatedObservationId: complexObservation.getChildLink()) {
            target.addRelated(new Observation.ObservationRelatedComponent()
                    .setTarget(ReferenceHelper.createReference(ResourceType.Observation, relatedObservationId))
                    .setType(Observation.ObservationRelationshipType.HASMEMBER));
        }
    }

    private void addEpisodicity(VocEpisodicity episodicity, Observation target) {
        if (episodicity == null || episodicity == VocEpisodicity.NONE)
            return;

        target.addExtension(new Extension()
                .setUrl(EPISODICITY_EXTENSION)
                .setValue(new CodeableConcept()
                    .addCoding(new Coding()
                        .setSystem(EPISODICITY_SYSTEM)
                        .setCode(episodicity.toString()))));
    }

    public static Range convertAgeRange(DtAgeRange source) throws TransformException
    {
        if (source == null)
            return null;

        Range target = new Range();

        //TODO: Age Unit - Consider coding the unit

        if (source.getLow() != null)
        {
            SimpleQuantity lowQuantity = new SimpleQuantity();
            lowQuantity.setValue(new BigDecimal(source.getLow().getValue()));
            lowQuantity.setUnit(convertAgeUnit(source.getLow().getUnit()));
            target.setLow(lowQuantity);
        }

        if (source.getHigh() != null)
        {
            SimpleQuantity highQuantity = new SimpleQuantity();
            highQuantity.setValue(new BigDecimal(source.getHigh().getValue()));
            highQuantity.setUnit(convertAgeUnit(source.getHigh().getUnit()));
            target.setHigh(highQuantity);
        }

        return target;
    }

    private static String convertAgeUnit(VocAgeUnit ageUnit) throws TransformException
    {
        switch (ageUnit)
        {
            case D: return "day";
            case W: return "week";
            case M: return "month";
            case Y: return "year";
            default: throw new TransformException("Age Unit not supported: " + ageUnit.toString());
        }
    }
}
