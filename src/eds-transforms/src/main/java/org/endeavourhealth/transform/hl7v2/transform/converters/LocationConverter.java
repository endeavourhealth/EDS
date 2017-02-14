package org.endeavourhealth.transform.hl7v2.transform.converters;

import org.apache.commons.lang3.StringUtils;
import org.endeavourhealth.transform.fhir.FhirExtensionUri;
import org.endeavourhealth.transform.hl7v2.parser.datatypes.Pl;
import org.endeavourhealth.transform.hl7v2.transform.TransformException;
import org.hl7.fhir.instance.model.*;

import java.util.*;

public class LocationConverter {

    private static String bed;
    private static String room;
    private static String ward;
    private static String facility;
    private static String building;
    private static LinkedHashMap<String, String> locationHeirarchy = new LinkedHashMap<>();


    public static List<Location> convert(Pl source, String locationType) throws TransformException {
        List<Location> locationList = new ArrayList<Location>();
        bed = source.getBed();
        room = source.getRoom();
        ward = source.getPointOfCare();
        facility = source.getFacility();
        building = source.getBuilding();

        locationHeirarchy.put("bu", source.getBuilding());
        locationHeirarchy.put("wi", source.getPointOfCare());
        locationHeirarchy.put("ro", source.getRoom());
        locationHeirarchy.put("bd", source.getBed());

        return createLocations();
    }

    private static List<Location> createLocations() throws TransformException {
        //UUID uuid = UUID.nameUUIDFromBytes((sourceString + locationType).getBytes());
        List<Location> locations = new ArrayList<Location>();

        Map<String, String> locationMap = new HashMap<>();
        locationMap.put("bu", " Building");
        locationMap.put("wi", " Department");
        locationMap.put("ro", " Room");
        locationMap.put("bd", " Bed");

        Reference linkedLocation = new Reference();
        String locationDescription = "";

        for (Map.Entry<String, String> entry : locationHeirarchy.entrySet()) {
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

    private static Encounter.EncounterLocationComponent getLocation(String name, String type, String locationType) throws TransformException {

        Encounter.EncounterLocationComponent locationComponent = new Encounter.EncounterLocationComponent();
        Location location = new Location();
        location.setMode(Location.LocationMode.INSTANCE);
        location.setName(name);
        locationComponent.setLocationTarget(location);
        Reference reference = new Reference();
        reference.setReference(type);
        reference.setDisplay(name);
        locationComponent.setLocation(reference);
        locationComponent.addExtension(ExtensionHelper.createStringExtension(FhirExtensionUri.ENCOUNTER_LOCATION_TYPE, locationType));


        return locationComponent;
    }
}
