package org.endeavourhealth.transform.hl7v2.transform;

import org.apache.commons.lang3.StringUtils;
import org.endeavourhealth.transform.hl7v2.parser.datatypes.Pl;
import org.endeavourhealth.transform.hl7v2.transform.TransformException;
import org.hl7.fhir.instance.model.*;

import java.util.*;

public class LocationTransform {

    private static LinkedHashMap<String, String> locationHierarchy = new LinkedHashMap<>();

    public static List<Location> convert(Pl source) throws TransformException {
        locationHierarchy.put("bu", source.getBuilding());
        locationHierarchy.put("wi", source.getPointOfCare());
        locationHierarchy.put("ro", source.getRoom());
        locationHierarchy.put("bd", source.getBed());

        return createLocations();
    }

    private static List<Location> createLocations() throws TransformException {
        List<Location> locations = new ArrayList<>();

        Map<String, String> locationMap = new HashMap<>();
        locationMap.put("bu", " Building");
        locationMap.put("wi", " Department");
        locationMap.put("ro", " Room");
        locationMap.put("bd", " Bed");

        Reference linkedLocation = new Reference();
        String locationDescription = "";

        for (Map.Entry<String, String> entry : locationHierarchy.entrySet()) {
            Location location = new Location();
            location.addIdentifier().setValue(entry.getValue());
            location.setPhysicalType(getCodeableConcept(entry.getKey()));
            location.setDescription(entry.getValue() + locationMap.get(entry.getKey()) + " " + locationDescription);

            locationDescription = "in " + location.getDescription();
            if (StringUtils.isNotBlank(linkedLocation.getDisplay()))
                location.setPartOf(linkedLocation.copy());

            locations.add(location);
            linkedLocation.setDisplay(entry.getValue()).setReference(entry.getValue());
        }
        return locations;
    }

    private static CodeableConcept getCodeableConcept(String code) throws TransformException {
        CodeableConcept codeableConcept = new CodeableConcept();
        codeableConcept.addCoding();
        codeableConcept.setText(code);

        return codeableConcept;
    }
}
