package org.endeavourhealth.transform.emis.openhr.transforms.common;

import org.apache.commons.lang3.StringUtils;
import org.endeavourhealth.transform.common.TransformException;
import org.endeavourhealth.transform.emis.openhr.schema.DtPatientIdentifier;
import org.endeavourhealth.transform.emis.openhr.schema.VocPatientIdentifierType;
import org.endeavourhealth.transform.fhir.FhirUri;
import org.hl7.fhir.instance.model.Identifier;

import java.util.ArrayList;
import java.util.List;

public class IdentifierConverter
{
    public static List<Identifier> convert(List<DtPatientIdentifier> sourceIdentifiers) throws TransformException
    {
        if (sourceIdentifiers == null || sourceIdentifiers.isEmpty())
            return null;

        List<Identifier> targetIdentifiers = new ArrayList<>();

        for (DtPatientIdentifier source: sourceIdentifiers)
            if ((source.getIdentifierType() == VocPatientIdentifierType.NHS) || (source.getIdentifierType() == VocPatientIdentifierType.CHI))
                if (!StringUtils.isBlank(source.getValue()))
                    targetIdentifiers.add(new Identifier().setSystem(convertIdentifierType(source.getIdentifierType())).setValue(source.getValue()));

        return targetIdentifiers;
    }

    private static String convertIdentifierType(VocPatientIdentifierType openHRType) throws TransformException
    {
        switch (openHRType)
        {
            case NHS: return FhirUri.IDENTIFIER_SYSTEM_NHSNUMBER;
            case CHI: return FhirUri.IDENTIFIER_SYSTEM_CHINUMBER;
            default: throw new TransformException("VocPatientIdentifierType not supported: " + openHRType.toString());
        }
    }
}
