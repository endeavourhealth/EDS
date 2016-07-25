
package org.endeavourhealth.transform.tpp.xml.schema;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for RelationshipType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="RelationshipType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="Adopted Father"/>
 *     &lt;enumeration value="Adopted Mother"/>
 *     &lt;enumeration value="Advisor (Day)"/>
 *     &lt;enumeration value="Advisor (Night)"/>
 *     &lt;enumeration value="Audiologist"/>
 *     &lt;enumeration value="Aunt"/>
 *     &lt;enumeration value="Boyfriend"/>
 *     &lt;enumeration value="Brother"/>
 *     &lt;enumeration value="Brother-in-law"/>
 *     &lt;enumeration value="CAB Adviser"/>
 *     &lt;enumeration value="CAF Lead Professional"/>
 *     &lt;enumeration value="Care Co-ordinator"/>
 *     &lt;enumeration value="Care Lead"/>
 *     &lt;enumeration value="Care Provider"/>
 *     &lt;enumeration value="Carer"/>
 *     &lt;enumeration value="Case Manager"/>
 *     &lt;enumeration value="Chaplain"/>
 *     &lt;enumeration value="Child"/>
 *     &lt;enumeration value="Child Protection/CIN Supervisor"/>
 *     &lt;enumeration value="Chiropodist"/>
 *     &lt;enumeration value="Civil Partner"/>
 *     &lt;enumeration value="Clinical Coordinator"/>
 *     &lt;enumeration value="Clinical Nurse Specialist"/>
 *     &lt;enumeration value="Clinical Psychologist"/>
 *     &lt;enumeration value="Community Matron"/>
 *     &lt;enumeration value="Community Midwife"/>
 *     &lt;enumeration value="Community Nurse"/>
 *     &lt;enumeration value="Community Paediatrician"/>
 *     &lt;enumeration value="Community Psychiatric Nurse"/>
 *     &lt;enumeration value="Community Volunteer"/>
 *     &lt;enumeration value="Consultant"/>
 *     &lt;enumeration value="Continence Advisor"/>
 *     &lt;enumeration value="Corporate Appointee"/>
 *     &lt;enumeration value="Counsellor"/>
 *     &lt;enumeration value="Cousin"/>
 *     &lt;enumeration value="Daughter"/>
 *     &lt;enumeration value="Daughter-in-law"/>
 *     &lt;enumeration value="Dentist"/>
 *     &lt;enumeration value="Dependant"/>
 *     &lt;enumeration value="Detox Worker"/>
 *     &lt;enumeration value="Dietician"/>
 *     &lt;enumeration value="District Nurse"/>
 *     &lt;enumeration value="Educational Psychologist"/>
 *     &lt;enumeration value="Ex-husband"/>
 *     &lt;enumeration value="Ex-partner"/>
 *     &lt;enumeration value="Ex-wife"/>
 *     &lt;enumeration value="Family Member"/>
 *     &lt;enumeration value="Family Nurse"/>
 *     &lt;enumeration value="Family Support"/>
 *     &lt;enumeration value="Father"/>
 *     &lt;enumeration value="Father-in-law"/>
 *     &lt;enumeration value="Fiance"/>
 *     &lt;enumeration value="Flatmate"/>
 *     &lt;enumeration value="Foster Child"/>
 *     &lt;enumeration value="Foster Father"/>
 *     &lt;enumeration value="Foster Mother"/>
 *     &lt;enumeration value="Foster Parent"/>
 *     &lt;enumeration value="Fostering Social Worker"/>
 *     &lt;enumeration value="Friend"/>
 *     &lt;enumeration value="Funeral Director"/>
 *     &lt;enumeration value="Generic Worker"/>
 *     &lt;enumeration value="Girlfriend"/>
 *     &lt;enumeration value="GP"/>
 *     &lt;enumeration value="GP For Ante-natal Care"/>
 *     &lt;enumeration value="GP Practice"/>
 *     &lt;enumeration value="Grandchild"/>
 *     &lt;enumeration value="Grandfather"/>
 *     &lt;enumeration value="Grandmother"/>
 *     &lt;enumeration value="Group Worker"/>
 *     &lt;enumeration value="Guardian"/>
 *     &lt;enumeration value="Half Brother"/>
 *     &lt;enumeration value="Half Sister"/>
 *     &lt;enumeration value="Health Visitor"/>
 *     &lt;enumeration value="Heart Failure Specialist Nurse"/>
 *     &lt;enumeration value="Home Team Leader"/>
 *     &lt;enumeration value="Hospital Paediatrician"/>
 *     &lt;enumeration value="Husband"/>
 *     &lt;enumeration value="Independent Mental Capacity Act Advocate"/>
 *     &lt;enumeration value="Independent Reviewing Officer"/>
 *     &lt;enumeration value="Intermediate Care Team"/>
 *     &lt;enumeration value="Key Palliative Care Member"/>
 *     &lt;enumeration value="Key Worker"/>
 *     &lt;enumeration value="Landlord"/>
 *     &lt;enumeration value="Macmillan Nurse"/>
 *     &lt;enumeration value="Main Assessor"/>
 *     &lt;enumeration value="Main Carer"/>
 *     &lt;enumeration value="Maternity Support Worker"/>
 *     &lt;enumeration value="Mother"/>
 *     &lt;enumeration value="Mother-in-law"/>
 *     &lt;enumeration value="Named Nurse"/>
 *     &lt;enumeration value="Named Prescriber"/>
 *     &lt;enumeration value="Neighbour"/>
 *     &lt;enumeration value="Nephew"/>
 *     &lt;enumeration value="Niece"/>
 *     &lt;enumeration value="Non-dependant"/>
 *     &lt;enumeration value="None"/>
 *     &lt;enumeration value="Nursery Nurse"/>
 *     &lt;enumeration value="Obstetrician"/>
 *     &lt;enumeration value="Occupational Therapist"/>
 *     &lt;enumeration value="Oncologist"/>
 *     &lt;enumeration value="Optometrist"/>
 *     &lt;enumeration value="Other"/>
 *     &lt;enumeration value="Outreach Worker"/>
 *     &lt;enumeration value="Paediatrician"/>
 *     &lt;enumeration value="Parent"/>
 *     &lt;enumeration value="Partner"/>
 *     &lt;enumeration value="Person of Religion"/>
 *     &lt;enumeration value="Pharmacist"/>
 *     &lt;enumeration value="Physiotherapist"/>
 *     &lt;enumeration value="Play Specialist"/>
 *     &lt;enumeration value="Polygamous Partner"/>
 *     &lt;enumeration value="Post Discharge Worker"/>
 *     &lt;enumeration value="Power of Attorney"/>
 *     &lt;enumeration value="Practice Nurse"/>
 *     &lt;enumeration value="Proxy - Communication"/>
 *     &lt;enumeration value="Proxy - Contact"/>
 *     &lt;enumeration value="Proxy - Contact and Communication"/>
 *     &lt;enumeration value="PSI Worker"/>
 *     &lt;enumeration value="Psychiatrist"/>
 *     &lt;enumeration value="Residential Carer"/>
 *     &lt;enumeration value="Safeguarding Lead Professional"/>
 *     &lt;enumeration value="School Health Assistant"/>
 *     &lt;enumeration value="School Nurse"/>
 *     &lt;enumeration value="School Teacher"/>
 *     &lt;enumeration value="SENCO"/>
 *     &lt;enumeration value="Sibling"/>
 *     &lt;enumeration value="Sister"/>
 *     &lt;enumeration value="Sister-in-law"/>
 *     &lt;enumeration value="Social Care Provider"/>
 *     &lt;enumeration value="Social Worker"/>
 *     &lt;enumeration value="Solicitor"/>
 *     &lt;enumeration value="Son"/>
 *     &lt;enumeration value="Son-in-law"/>
 *     &lt;enumeration value="Specialist Health Visitor"/>
 *     &lt;enumeration value="Specialist Midwife"/>
 *     &lt;enumeration value="Specialist Nurse"/>
 *     &lt;enumeration value="Speech Therapist"/>
 *     &lt;enumeration value="Spouse/Partner"/>
 *     &lt;enumeration value="Step-brother"/>
 *     &lt;enumeration value="Step-daughter"/>
 *     &lt;enumeration value="Step-father"/>
 *     &lt;enumeration value="Step-mother"/>
 *     &lt;enumeration value="Step-parent"/>
 *     &lt;enumeration value="Step-sister"/>
 *     &lt;enumeration value="Step-son"/>
 *     &lt;enumeration value="Uncle"/>
 *     &lt;enumeration value="Unknown"/>
 *     &lt;enumeration value="Ward Nurse"/>
 *     &lt;enumeration value="Warden"/>
 *     &lt;enumeration value="Wife"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "RelationshipType")
@XmlEnum
public enum RelationshipType {

    @XmlEnumValue("Adopted Father")
    ADOPTED_FATHER("Adopted Father"),
    @XmlEnumValue("Adopted Mother")
    ADOPTED_MOTHER("Adopted Mother"),
    @XmlEnumValue("Advisor (Day)")
    ADVISOR_DAY("Advisor (Day)"),
    @XmlEnumValue("Advisor (Night)")
    ADVISOR_NIGHT("Advisor (Night)"),
    @XmlEnumValue("Audiologist")
    AUDIOLOGIST("Audiologist"),
    @XmlEnumValue("Aunt")
    AUNT("Aunt"),
    @XmlEnumValue("Boyfriend")
    BOYFRIEND("Boyfriend"),
    @XmlEnumValue("Brother")
    BROTHER("Brother"),
    @XmlEnumValue("Brother-in-law")
    BROTHER_IN_LAW("Brother-in-law"),
    @XmlEnumValue("CAB Adviser")
    CAB_ADVISER("CAB Adviser"),
    @XmlEnumValue("CAF Lead Professional")
    CAF_LEAD_PROFESSIONAL("CAF Lead Professional"),
    @XmlEnumValue("Care Co-ordinator")
    CARE_CO_ORDINATOR("Care Co-ordinator"),
    @XmlEnumValue("Care Lead")
    CARE_LEAD("Care Lead"),
    @XmlEnumValue("Care Provider")
    CARE_PROVIDER("Care Provider"),
    @XmlEnumValue("Carer")
    CARER("Carer"),
    @XmlEnumValue("Case Manager")
    CASE_MANAGER("Case Manager"),
    @XmlEnumValue("Chaplain")
    CHAPLAIN("Chaplain"),
    @XmlEnumValue("Child")
    CHILD("Child"),
    @XmlEnumValue("Child Protection/CIN Supervisor")
    CHILD_PROTECTION_CIN_SUPERVISOR("Child Protection/CIN Supervisor"),
    @XmlEnumValue("Chiropodist")
    CHIROPODIST("Chiropodist"),
    @XmlEnumValue("Civil Partner")
    CIVIL_PARTNER("Civil Partner"),
    @XmlEnumValue("Clinical Coordinator")
    CLINICAL_COORDINATOR("Clinical Coordinator"),
    @XmlEnumValue("Clinical Nurse Specialist")
    CLINICAL_NURSE_SPECIALIST("Clinical Nurse Specialist"),
    @XmlEnumValue("Clinical Psychologist")
    CLINICAL_PSYCHOLOGIST("Clinical Psychologist"),
    @XmlEnumValue("Community Matron")
    COMMUNITY_MATRON("Community Matron"),
    @XmlEnumValue("Community Midwife")
    COMMUNITY_MIDWIFE("Community Midwife"),
    @XmlEnumValue("Community Nurse")
    COMMUNITY_NURSE("Community Nurse"),
    @XmlEnumValue("Community Paediatrician")
    COMMUNITY_PAEDIATRICIAN("Community Paediatrician"),
    @XmlEnumValue("Community Psychiatric Nurse")
    COMMUNITY_PSYCHIATRIC_NURSE("Community Psychiatric Nurse"),
    @XmlEnumValue("Community Volunteer")
    COMMUNITY_VOLUNTEER("Community Volunteer"),
    @XmlEnumValue("Consultant")
    CONSULTANT("Consultant"),
    @XmlEnumValue("Continence Advisor")
    CONTINENCE_ADVISOR("Continence Advisor"),
    @XmlEnumValue("Corporate Appointee")
    CORPORATE_APPOINTEE("Corporate Appointee"),
    @XmlEnumValue("Counsellor")
    COUNSELLOR("Counsellor"),
    @XmlEnumValue("Cousin")
    COUSIN("Cousin"),
    @XmlEnumValue("Daughter")
    DAUGHTER("Daughter"),
    @XmlEnumValue("Daughter-in-law")
    DAUGHTER_IN_LAW("Daughter-in-law"),
    @XmlEnumValue("Dentist")
    DENTIST("Dentist"),
    @XmlEnumValue("Dependant")
    DEPENDANT("Dependant"),
    @XmlEnumValue("Detox Worker")
    DETOX_WORKER("Detox Worker"),
    @XmlEnumValue("Dietician")
    DIETICIAN("Dietician"),
    @XmlEnumValue("District Nurse")
    DISTRICT_NURSE("District Nurse"),
    @XmlEnumValue("Educational Psychologist")
    EDUCATIONAL_PSYCHOLOGIST("Educational Psychologist"),
    @XmlEnumValue("Ex-husband")
    EX_HUSBAND("Ex-husband"),
    @XmlEnumValue("Ex-partner")
    EX_PARTNER("Ex-partner"),
    @XmlEnumValue("Ex-wife")
    EX_WIFE("Ex-wife"),
    @XmlEnumValue("Family Member")
    FAMILY_MEMBER("Family Member"),
    @XmlEnumValue("Family Nurse")
    FAMILY_NURSE("Family Nurse"),
    @XmlEnumValue("Family Support")
    FAMILY_SUPPORT("Family Support"),
    @XmlEnumValue("Father")
    FATHER("Father"),
    @XmlEnumValue("Father-in-law")
    FATHER_IN_LAW("Father-in-law"),
    @XmlEnumValue("Fiance")
    FIANCE("Fiance"),
    @XmlEnumValue("Flatmate")
    FLATMATE("Flatmate"),
    @XmlEnumValue("Foster Child")
    FOSTER_CHILD("Foster Child"),
    @XmlEnumValue("Foster Father")
    FOSTER_FATHER("Foster Father"),
    @XmlEnumValue("Foster Mother")
    FOSTER_MOTHER("Foster Mother"),
    @XmlEnumValue("Foster Parent")
    FOSTER_PARENT("Foster Parent"),
    @XmlEnumValue("Fostering Social Worker")
    FOSTERING_SOCIAL_WORKER("Fostering Social Worker"),
    @XmlEnumValue("Friend")
    FRIEND("Friend"),
    @XmlEnumValue("Funeral Director")
    FUNERAL_DIRECTOR("Funeral Director"),
    @XmlEnumValue("Generic Worker")
    GENERIC_WORKER("Generic Worker"),
    @XmlEnumValue("Girlfriend")
    GIRLFRIEND("Girlfriend"),
    GP("GP"),
    @XmlEnumValue("GP For Ante-natal Care")
    GP_FOR_ANTE_NATAL_CARE("GP For Ante-natal Care"),
    @XmlEnumValue("GP Practice")
    GP_PRACTICE("GP Practice"),
    @XmlEnumValue("Grandchild")
    GRANDCHILD("Grandchild"),
    @XmlEnumValue("Grandfather")
    GRANDFATHER("Grandfather"),
    @XmlEnumValue("Grandmother")
    GRANDMOTHER("Grandmother"),
    @XmlEnumValue("Group Worker")
    GROUP_WORKER("Group Worker"),
    @XmlEnumValue("Guardian")
    GUARDIAN("Guardian"),
    @XmlEnumValue("Half Brother")
    HALF_BROTHER("Half Brother"),
    @XmlEnumValue("Half Sister")
    HALF_SISTER("Half Sister"),
    @XmlEnumValue("Health Visitor")
    HEALTH_VISITOR("Health Visitor"),
    @XmlEnumValue("Heart Failure Specialist Nurse")
    HEART_FAILURE_SPECIALIST_NURSE("Heart Failure Specialist Nurse"),
    @XmlEnumValue("Home Team Leader")
    HOME_TEAM_LEADER("Home Team Leader"),
    @XmlEnumValue("Hospital Paediatrician")
    HOSPITAL_PAEDIATRICIAN("Hospital Paediatrician"),
    @XmlEnumValue("Husband")
    HUSBAND("Husband"),
    @XmlEnumValue("Independent Mental Capacity Act Advocate")
    INDEPENDENT_MENTAL_CAPACITY_ACT_ADVOCATE("Independent Mental Capacity Act Advocate"),
    @XmlEnumValue("Independent Reviewing Officer")
    INDEPENDENT_REVIEWING_OFFICER("Independent Reviewing Officer"),
    @XmlEnumValue("Intermediate Care Team")
    INTERMEDIATE_CARE_TEAM("Intermediate Care Team"),
    @XmlEnumValue("Key Palliative Care Member")
    KEY_PALLIATIVE_CARE_MEMBER("Key Palliative Care Member"),
    @XmlEnumValue("Key Worker")
    KEY_WORKER("Key Worker"),
    @XmlEnumValue("Landlord")
    LANDLORD("Landlord"),
    @XmlEnumValue("Macmillan Nurse")
    MACMILLAN_NURSE("Macmillan Nurse"),
    @XmlEnumValue("Main Assessor")
    MAIN_ASSESSOR("Main Assessor"),
    @XmlEnumValue("Main Carer")
    MAIN_CARER("Main Carer"),
    @XmlEnumValue("Maternity Support Worker")
    MATERNITY_SUPPORT_WORKER("Maternity Support Worker"),
    @XmlEnumValue("Mother")
    MOTHER("Mother"),
    @XmlEnumValue("Mother-in-law")
    MOTHER_IN_LAW("Mother-in-law"),
    @XmlEnumValue("Named Nurse")
    NAMED_NURSE("Named Nurse"),
    @XmlEnumValue("Named Prescriber")
    NAMED_PRESCRIBER("Named Prescriber"),
    @XmlEnumValue("Neighbour")
    NEIGHBOUR("Neighbour"),
    @XmlEnumValue("Nephew")
    NEPHEW("Nephew"),
    @XmlEnumValue("Niece")
    NIECE("Niece"),
    @XmlEnumValue("Non-dependant")
    NON_DEPENDANT("Non-dependant"),
    @XmlEnumValue("None")
    NONE("None"),
    @XmlEnumValue("Nursery Nurse")
    NURSERY_NURSE("Nursery Nurse"),
    @XmlEnumValue("Obstetrician")
    OBSTETRICIAN("Obstetrician"),
    @XmlEnumValue("Occupational Therapist")
    OCCUPATIONAL_THERAPIST("Occupational Therapist"),
    @XmlEnumValue("Oncologist")
    ONCOLOGIST("Oncologist"),
    @XmlEnumValue("Optometrist")
    OPTOMETRIST("Optometrist"),
    @XmlEnumValue("Other")
    OTHER("Other"),
    @XmlEnumValue("Outreach Worker")
    OUTREACH_WORKER("Outreach Worker"),
    @XmlEnumValue("Paediatrician")
    PAEDIATRICIAN("Paediatrician"),
    @XmlEnumValue("Parent")
    PARENT("Parent"),
    @XmlEnumValue("Partner")
    PARTNER("Partner"),
    @XmlEnumValue("Person of Religion")
    PERSON_OF_RELIGION("Person of Religion"),
    @XmlEnumValue("Pharmacist")
    PHARMACIST("Pharmacist"),
    @XmlEnumValue("Physiotherapist")
    PHYSIOTHERAPIST("Physiotherapist"),
    @XmlEnumValue("Play Specialist")
    PLAY_SPECIALIST("Play Specialist"),
    @XmlEnumValue("Polygamous Partner")
    POLYGAMOUS_PARTNER("Polygamous Partner"),
    @XmlEnumValue("Post Discharge Worker")
    POST_DISCHARGE_WORKER("Post Discharge Worker"),
    @XmlEnumValue("Power of Attorney")
    POWER_OF_ATTORNEY("Power of Attorney"),
    @XmlEnumValue("Practice Nurse")
    PRACTICE_NURSE("Practice Nurse"),
    @XmlEnumValue("Proxy - Communication")
    PROXY_COMMUNICATION("Proxy - Communication"),
    @XmlEnumValue("Proxy - Contact")
    PROXY_CONTACT("Proxy - Contact"),
    @XmlEnumValue("Proxy - Contact and Communication")
    PROXY_CONTACT_AND_COMMUNICATION("Proxy - Contact and Communication"),
    @XmlEnumValue("PSI Worker")
    PSI_WORKER("PSI Worker"),
    @XmlEnumValue("Psychiatrist")
    PSYCHIATRIST("Psychiatrist"),
    @XmlEnumValue("Residential Carer")
    RESIDENTIAL_CARER("Residential Carer"),
    @XmlEnumValue("Safeguarding Lead Professional")
    SAFEGUARDING_LEAD_PROFESSIONAL("Safeguarding Lead Professional"),
    @XmlEnumValue("School Health Assistant")
    SCHOOL_HEALTH_ASSISTANT("School Health Assistant"),
    @XmlEnumValue("School Nurse")
    SCHOOL_NURSE("School Nurse"),
    @XmlEnumValue("School Teacher")
    SCHOOL_TEACHER("School Teacher"),
    SENCO("SENCO"),
    @XmlEnumValue("Sibling")
    SIBLING("Sibling"),
    @XmlEnumValue("Sister")
    SISTER("Sister"),
    @XmlEnumValue("Sister-in-law")
    SISTER_IN_LAW("Sister-in-law"),
    @XmlEnumValue("Social Care Provider")
    SOCIAL_CARE_PROVIDER("Social Care Provider"),
    @XmlEnumValue("Social Worker")
    SOCIAL_WORKER("Social Worker"),
    @XmlEnumValue("Solicitor")
    SOLICITOR("Solicitor"),
    @XmlEnumValue("Son")
    SON("Son"),
    @XmlEnumValue("Son-in-law")
    SON_IN_LAW("Son-in-law"),
    @XmlEnumValue("Specialist Health Visitor")
    SPECIALIST_HEALTH_VISITOR("Specialist Health Visitor"),
    @XmlEnumValue("Specialist Midwife")
    SPECIALIST_MIDWIFE("Specialist Midwife"),
    @XmlEnumValue("Specialist Nurse")
    SPECIALIST_NURSE("Specialist Nurse"),
    @XmlEnumValue("Speech Therapist")
    SPEECH_THERAPIST("Speech Therapist"),
    @XmlEnumValue("Spouse/Partner")
    SPOUSE_PARTNER("Spouse/Partner"),
    @XmlEnumValue("Step-brother")
    STEP_BROTHER("Step-brother"),
    @XmlEnumValue("Step-daughter")
    STEP_DAUGHTER("Step-daughter"),
    @XmlEnumValue("Step-father")
    STEP_FATHER("Step-father"),
    @XmlEnumValue("Step-mother")
    STEP_MOTHER("Step-mother"),
    @XmlEnumValue("Step-parent")
    STEP_PARENT("Step-parent"),
    @XmlEnumValue("Step-sister")
    STEP_SISTER("Step-sister"),
    @XmlEnumValue("Step-son")
    STEP_SON("Step-son"),
    @XmlEnumValue("Uncle")
    UNCLE("Uncle"),
    @XmlEnumValue("Unknown")
    UNKNOWN("Unknown"),
    @XmlEnumValue("Ward Nurse")
    WARD_NURSE("Ward Nurse"),
    @XmlEnumValue("Warden")
    WARDEN("Warden"),
    @XmlEnumValue("Wife")
    WIFE("Wife");
    private final String value;

    RelationshipType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static RelationshipType fromValue(String v) {
        for (RelationshipType c: RelationshipType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
