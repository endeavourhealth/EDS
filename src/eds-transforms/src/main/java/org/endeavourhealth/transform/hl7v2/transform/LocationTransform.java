package org.endeavourhealth.transform.hl7v2.transform;

import org.apache.commons.lang3.StringUtils;
import org.endeavourhealth.transform.hl7v2.parser.datatypes.Pl;
import org.hl7.fhir.instance.model.*;

import java.util.*;

public class LocationTransform {

    private static final Map<String, String> locationMap = Collections.unmodifiableMap(new HashMap<String, String>() {{
        put("bu", " Building");
        put("wi", " Department");
        put("ro", " Room");
        put("bd", " Bed");
    }});

    public static List<Location> convert(Pl source) throws TransformException {
        List<Location> locationHierarchy = new ArrayList<>();

        if (StringUtils.isNotBlank(source.getBuilding()))
            processLocation(source.getBuilding(), "bu", locationHierarchy, source.getAsString());

        if (StringUtils.isNotBlank(source.getPointOfCare()))
            processLocation(source.getPointOfCare(), "wi", locationHierarchy, source.getAsString());

        if (StringUtils.isNotBlank(source.getRoom()))
            processLocation(source.getRoom(), "ro", locationHierarchy, source.getAsString());

        if (StringUtils.isNotBlank(source.getBed()))
            processLocation(source.getBed(), "bd", locationHierarchy, source.getAsString());

        return locationHierarchy;
    }

    //need to process the locations to work out the references when processing them in PV1 segment and beyond
    private static void processLocation(String locationName, String locationType, List<Location> locationHierarchy, String identifierString) throws TransformException {
        Reference parentRef = new Reference();
        String parentDescription = "";

        if (!locationHierarchy.isEmpty()) {
            Location parentLocation = locationHierarchy.get(locationHierarchy.size() - 1);
            createReferenceFromLocation(parentLocation);
            parentDescription = " in " + parentLocation.getDescription();
        }

        Location newLocation = new Location();
        newLocation.addIdentifier().setValue(generateId(locationType, identifierString));
        newLocation.setPhysicalType(getCodeableConcept(locationType));
        newLocation.setDescription(locationName + locationMap.get(locationType) + parentDescription);
        newLocation.setName(locationName);

        if (StringUtils.isNotBlank(parentRef.getDisplay()))
            newLocation.setPartOf(parentRef.copy());

        locationHierarchy.add(newLocation);
    }

    public static Reference createReferenceFromLocation(Location location) {
        Reference reference = new Reference();
        List<Identifier> identifiers = location.getIdentifier();
        reference.setDisplay(location.getDescription()).setReference(identifiers.get(0).getValue());
        return reference;
    }

    private static String generateId(String uniqueString, String identifierString) {
        return UUID.nameUUIDFromBytes((identifierString + uniqueString).getBytes()).toString();
    }

    private static CodeableConcept getCodeableConcept(String code) throws TransformException {
        CodeableConcept codeableConcept = new CodeableConcept();
        codeableConcept.addCoding();
        codeableConcept.setText(code);

        return codeableConcept;
    }
}
