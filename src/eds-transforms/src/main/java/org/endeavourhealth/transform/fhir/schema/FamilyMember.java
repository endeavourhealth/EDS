package org.endeavourhealth.transform.fhir.schema;

import org.endeavourhealth.transform.fhir.FhirValueSetUri;

public enum FamilyMember {

    //defined at http://hl7.org/fhir/v3/FamilyMember/index.html
    FAMILY_MEMBER("FAMMEMB", "family member"),
    CHILD("CHILD", "child"),
    ADOPTED_CHILD("HLDADOPT", "adopted child"),
    ADOPTED_DAUGHTER("DAUADOPT", "adopted daughter"),
    ADOPTED_SON("SONADOPT", "adopted son"),
    FOSTER_CHILD("CHLDFOST", "foster child"),
    FOSTER_DAUGHTER("DAUFOST", "foster daughter"),
    FOSTER_SON("SONFOST", "foster son"),
    DAUGHTER("DAUC", "daughter"),
    NATURAL_DAUGHTER("DAU", "natural daughter"),
    STEP_DAUGHTER("STPDAU", "stepdaughter"),
    NATURAL_CHILD("NCHILD", "natural child"),
    NATURAL_SON("SON", "natural son"),
    SON("SONC", "son"),
    STEP_SON("STPSON", "stepson"),
    STEP_CHILD("STPCHLD", "step child"),
    EXTENDED_FAMILY_MEMBER("EXT", "extended family member"),
    AUNT("AUNT", "aunt"),
    MATERNAL_AUNT("MAUNT", "maternal aunt"),
    PATERNAL_AUNT("PAUNT", "paternal aunt"),
    COUSIN("COUSN", "cousin"),
    MATERNAL_COUSIN("MCOUSN", "maternal cousin"),
    PATERNAL_COUSIN("PCOUSN", "paternal cousin"),
    GREAT_GRANDPARENT("GGRPRN", "great grandparent"),
    GREAT_GRANDFATER("GGRFTH", "great grandfather"),
    MATERNAL_GREAT_GRANDFATHER("MGGRFTH", "maternal great-grandfather"),
    PATERNAL_GREAT_GRANDFATHER("PGGRFTH", "paternal great-grandfather"),
    GREAT_GRANDMOTHER("GGRMTH", "great grandmother"),
    MATERNAL_GREAT_GRANDMOTHER("MGGRMTH", "maternal great-grandmother"),
    PATERNAL_GREAT_GRANDMOTHER("PGGRMTH", "paternal great-grandmother"),
    MATERNAL_GREAT_GRANDPARENT("MGGRPRN", "maternal great-grandparent"),
    PATERNAL_GREAT_GRANDPARENT("PGGRPRN", "paternal great-grandparent"),
    GRANDCHILD("GRNDCHILD", "grandchild"),
    GRANDDAUGHTER("GRNDDAU", "granddaughter"),
    GRANDSON("GRNDSON", "grandson"),
    GRANDPARENT("GRPRN", "grandparent"),
    GRANDFATHER("GRFTH", "grandfather"),
    MATERNAL_GRANDFATHER("MGRFTH", "maternal grandfather"),
    PATERNAL_GRANDFATHER("PGRFTH", "paternal grandfather"),
    GRANDMOTHER("GRMTH", "grandmother"),
    MATERNAL_GRANDMOTHER("MGRMTH", "maternal grandmother"),
    PATERNAL_GRANDMOTHER("PGRMTH", "paternal grandmother"),
    MATERNAL_GRANDPARENT("MGRPRN", "maternal grandparent"),
    PATERNAL_GRANDPARENT("PGRPRN", "paternal grandparent"),
    INLAW("INLAW", "inlaw"),
    CHILD_IN_LAW("CHLDINLAW", "child-in-law"),
    DAUGHTER_IN_LAW("DAUINLAW", "daughter in-law"),
    SON_IN_LAW("SONINLAW", "son in-law"),
    PARENT_IN_LAW("PRNINLAW", "parent in-law"),
    FATHER_IN_LAW("FTHINLAW", "father-in-law"),
    MOTHER_IN_LAW("MTHINLAW", "mother-in-law"),
    SIBLING_IN_LAW("SIBINLAW", "sibling in-law"),
    BROTHER_IN_LAW("BROINLAW", "brother-in-law"),
    SISTER_IN_LAW("SISINLAW", "sister-in-law"),
    NEICE_OR_NEPHEW("NIENEPH", "niece/nephew"),
    NEPHEW("NEPHEW", "nephew"),
    NIECE("NIECE", "niece"),
    UNCLE("UNCLE", "uncle"),
    MATERNAL_UNCLE("MUNCLE", "maternal uncle"),
    PATERNAL_UNCLE("PUNCLE", "paternal uncle"),
    PARENT("PRN", "parent"),
    ADOPTIVE_PARENT("ADOPTP", "adoptive parent"),
    ADOPTIVE_FATHER("ADOPTF", "adoptive father"),
    ADOPTIVE_MOTHER("ADOPTM", "adoptive mother"),
    FATHER("FTH", "father"),
    FOSTER_FATHER("FTHFOST", "foster father"),
    NATURAL_FATHER("NFTH", "natural father"),
    NATURAL_FATHER_OF_FETUS("NFTHF", "natural father of fetus"),
    STEP_FATHER("STPFTH", "stepfather"),
    MOTHER("MTH", "mother"),
    GESTATIONAL_MOTHER("GESTM", "gestational mother"),
    FOSTER_MOTHER("MTHFOST", "foster mother"),
    NATURAL_MOTHER("NMTH", "natural mother"),
    NATURAL_MOTHER_OF_FETUS("NMTHF", "natural mother of fetus"),
    STEP_MOTHER("STPMTH", "stepmother"),
    NATURAL_PARENT("NPRN", "natural parent"),
    FOSTER_PARENT("PRNFOST", "foster parent"),
    STEP_PARENT("STPPRN", "step parent"),
    SIBLING("SIB", "sibling"),
    BROTHER("BRO", "brother"),
    HALF_BROTHER("HBRO", "half-brother"),
    NATURAL_BROTHER("NBRO", "natural brother"),
    TWIN_BROTHER("TWINBRO", "twin brother"),
    FRATERNAL_TWIN_BROTHER("FTWINBRO", "fraternal twin brother"),
    IDENTICAL_TWIN_BROTHER("ITWINBRO", "identical twin brother"),
    STEP_BROTHER("STPBRO", "stepbrother"),
    HALF_SIBLING("HSIB", "half-sibling"),
    HALF_SISTER("HSIS", "half-sister"),
    NATURAL_SIBLING("NSIB", "natural sibling"),
    NATURAL_SISTER("NSIS", "natural sister"),
    TWIN_SISTER("TWINSIS", "twin sister"),
    FRATERNAL_TWIN_SISTER("FTWINSIS", "fraternal twin sister"),
    IDENTICAL_TWIN_SISTER("ITWINSIS", "identical twin sister"),
    TWIN("TWIN", "twin"),
    FRATERNAL_TWIN("FTWIN", "fraternal twin"),
    IDENTICAL_TWIN("ITWIN", "identical twin"),
    SISTER("SIS", "sister"),
    STEP_SISTER("STPSIS", "stepsister"),
    STEP_SIBLING("STPSIB", "step sibling"),
    SIGNIFICANT_OTHER("SIGOTHR", "significant other"),
    DOMESTIC_PARTNER("DOMPART", "domestic partner"),
    FORMER_SPOUSE("FMRSPS", "former spouse"),
    SPOUSE("SPS", "spouse"),
    HUSBAND("HUSB", "husband"),
    WIFE("WIFE", "wife");

    private String code = null;
    private String description = null;

    public String getCode() {
        return code;
    }
    public String getDescription() {
        return description;
    }
    public String getSystem() {
        return FhirValueSetUri.VALUE_SET_FAMILY_MEMBER;
    }

    FamilyMember(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public static FamilyMember fromCode(String v) {
        for (FamilyMember c: FamilyMember.values()) {
            if (c.code.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
