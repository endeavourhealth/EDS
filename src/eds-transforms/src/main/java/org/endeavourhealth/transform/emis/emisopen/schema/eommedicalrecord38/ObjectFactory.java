
package org.endeavourhealth.transform.emis.emisopen.schema.eommedicalrecord38;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.endeavourhealth.transform.emis.emisopen.schema.eommedicalrecord38 package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _MedicalRecord_QNAME = new QName("http://www.e-mis.com/emisopen/MedicalRecord", "MedicalRecord");
    private final static QName _Episodicity_QNAME = new QName("http://www.e-mis.com/emisopen/MedicalRecord", "Episodicity");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.endeavourhealth.transform.emis.emisopen.schema.eommedicalrecord38
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link LocationType }
     * 
     */
    public LocationType createLocationType() {
        return new LocationType();
    }

    /**
     * Create an instance of {@link LocationType.GPLinkCodeList }
     * 
     */
    public LocationType.GPLinkCodeList createLocationTypeGPLinkCodeList() {
        return new LocationType.GPLinkCodeList();
    }

    /**
     * Create an instance of {@link MixtureType }
     * 
     */
    public MixtureType createMixtureType() {
        return new MixtureType();
    }

    /**
     * Create an instance of {@link TestRequestHeaderType }
     * 
     */
    public TestRequestHeaderType createTestRequestHeaderType() {
        return new TestRequestHeaderType();
    }

    /**
     * Create an instance of {@link RegistrationStatusType }
     * 
     */
    public RegistrationStatusType createRegistrationStatusType() {
        return new RegistrationStatusType();
    }

    /**
     * Create an instance of {@link CodedItemBaseType }
     * 
     */
    public CodedItemBaseType createCodedItemBaseType() {
        return new CodedItemBaseType();
    }

    /**
     * Create an instance of {@link MedicationType }
     * 
     */
    public MedicationType createMedicationType() {
        return new MedicationType();
    }

    /**
     * Create an instance of {@link OriginatorType }
     * 
     */
    public OriginatorType createOriginatorType() {
        return new OriginatorType();
    }

    /**
     * Create an instance of {@link RegistrationType }
     * 
     */
    public RegistrationType createRegistrationType() {
        return new RegistrationType();
    }

    /**
     * Create an instance of {@link RegistrationType.CustomRegistrationFields }
     * 
     */
    public RegistrationType.CustomRegistrationFields createRegistrationTypeCustomRegistrationFields() {
        return new RegistrationType.CustomRegistrationFields();
    }

    /**
     * Create an instance of {@link PolicyListType }
     * 
     */
    public PolicyListType createPolicyListType() {
        return new PolicyListType();
    }

    /**
     * Create an instance of {@link PolicyListType.PolicyType }
     * 
     */
    public PolicyListType.PolicyType createPolicyListTypePolicyType() {
        return new PolicyListType.PolicyType();
    }

    /**
     * Create an instance of {@link PathologyTestType }
     * 
     */
    public PathologyTestType createPathologyTestType() {
        return new PathologyTestType();
    }

    /**
     * Create an instance of {@link PathologyTestType.RangeInformationList }
     * 
     */
    public PathologyTestType.RangeInformationList createPathologyTestTypeRangeInformationList() {
        return new PathologyTestType.RangeInformationList();
    }

    /**
     * Create an instance of {@link AppointmentType }
     * 
     */
    public AppointmentType createAppointmentType() {
        return new AppointmentType();
    }

    /**
     * Create an instance of {@link StructuredIdentType }
     * 
     */
    public StructuredIdentType createStructuredIdentType() {
        return new StructuredIdentType();
    }

    /**
     * Create an instance of {@link PathologyReportType }
     * 
     */
    public PathologyReportType createPathologyReportType() {
        return new PathologyReportType();
    }

    /**
     * Create an instance of {@link PathologyReportType.Identifiers }
     * 
     */
    public PathologyReportType.Identifiers createPathologyReportTypeIdentifiers() {
        return new PathologyReportType.Identifiers();
    }

    /**
     * Create an instance of {@link EDIOrderType }
     * 
     */
    public EDIOrderType createEDIOrderType() {
        return new EDIOrderType();
    }

    /**
     * Create an instance of {@link ElementListType }
     * 
     */
    public ElementListType createElementListType() {
        return new ElementListType();
    }

    /**
     * Create an instance of {@link MedicalRecordType }
     * 
     */
    public MedicalRecordType createMedicalRecordType() {
        return new MedicalRecordType();
    }

    /**
     * Create an instance of {@link MedicalRecordType.MessageInformation }
     * 
     */
    public MedicalRecordType.MessageInformation createMedicalRecordTypeMessageInformation() {
        return new MedicalRecordType.MessageInformation();
    }

    /**
     * Create an instance of {@link MedicalRecordType.MessageInformation.MessagePurpose }
     * 
     */
    public MedicalRecordType.MessageInformation.MessagePurpose createMedicalRecordTypeMessageInformationMessagePurpose() {
        return new MedicalRecordType.MessageInformation.MessagePurpose();
    }

    /**
     * Create an instance of {@link MedicalRecordType.RegistrationChangeHistory }
     * 
     */
    public MedicalRecordType.RegistrationChangeHistory createMedicalRecordTypeRegistrationChangeHistory() {
        return new MedicalRecordType.RegistrationChangeHistory();
    }

    /**
     * Create an instance of {@link TypeOfLocationType }
     * 
     */
    public TypeOfLocationType createTypeOfLocationType() {
        return new TypeOfLocationType();
    }

    /**
     * Create an instance of {@link EventType }
     * 
     */
    public EventType createEventType() {
        return new EventType();
    }

    /**
     * Create an instance of {@link RoleType }
     * 
     */
    public RoleType createRoleType() {
        return new RoleType();
    }

    /**
     * Create an instance of {@link TestRequestType }
     * 
     */
    public TestRequestType createTestRequestType() {
        return new TestRequestType();
    }

    /**
     * Create an instance of {@link AllergyListType }
     * 
     */
    public AllergyListType createAllergyListType() {
        return new AllergyListType();
    }

    /**
     * Create an instance of {@link NoteListType }
     * 
     */
    public NoteListType createNoteListType() {
        return new NoteListType();
    }

    /**
     * Create an instance of {@link PersonType }
     * 
     */
    public PersonType createPersonType() {
        return new PersonType();
    }

    /**
     * Create an instance of {@link EDIPathologyReportListType }
     * 
     */
    public EDIPathologyReportListType createEDIPathologyReportListType() {
        return new EDIPathologyReportListType();
    }

    /**
     * Create an instance of {@link LocationTypeListType }
     * 
     */
    public LocationTypeListType createLocationTypeListType() {
        return new LocationTypeListType();
    }

    /**
     * Create an instance of {@link ControlledDrugInfoType }
     * 
     */
    public ControlledDrugInfoType createControlledDrugInfoType() {
        return new ControlledDrugInfoType();
    }

    /**
     * Create an instance of {@link MixtureItemType }
     * 
     */
    public MixtureItemType createMixtureItemType() {
        return new MixtureItemType();
    }

    /**
     * Create an instance of {@link LocationListType }
     * 
     */
    public LocationListType createLocationListType() {
        return new LocationListType();
    }

    /**
     * Create an instance of {@link EventListType }
     * 
     */
    public EventListType createEventListType() {
        return new EventListType();
    }

    /**
     * Create an instance of {@link CarePlanListType }
     * 
     */
    public CarePlanListType createCarePlanListType() {
        return new CarePlanListType();
    }

    /**
     * Create an instance of {@link PathologySpecimenType }
     * 
     */
    public PathologySpecimenType createPathologySpecimenType() {
        return new PathologySpecimenType();
    }

    /**
     * Create an instance of {@link PathologyInvestigationType }
     * 
     */
    public PathologyInvestigationType createPathologyInvestigationType() {
        return new PathologyInvestigationType();
    }

    /**
     * Create an instance of {@link CareAimListType }
     * 
     */
    public CareAimListType createCareAimListType() {
        return new CareAimListType();
    }

    /**
     * Create an instance of {@link IdentType }
     * 
     */
    public IdentType createIdentType() {
        return new IdentType();
    }

    /**
     * Create an instance of {@link PersonCategoryType }
     * 
     */
    public PersonCategoryType createPersonCategoryType() {
        return new PersonCategoryType();
    }

    /**
     * Create an instance of {@link NumericValueType }
     * 
     */
    public NumericValueType createNumericValueType() {
        return new NumericValueType();
    }

    /**
     * Create an instance of {@link QualifierType }
     * 
     */
    public QualifierType createQualifierType() {
        return new QualifierType();
    }

    /**
     * Create an instance of {@link PathologyInvestigationListType }
     * 
     */
    public PathologyInvestigationListType createPathologyInvestigationListType() {
        return new PathologyInvestigationListType();
    }

    /**
     * Create an instance of {@link ApplicationType }
     * 
     */
    public ApplicationType createApplicationType() {
        return new ApplicationType();
    }

    /**
     * Create an instance of {@link TeamType }
     * 
     */
    public TeamType createTeamType() {
        return new TeamType();
    }

    /**
     * Create an instance of {@link InvestigationTypeBase }
     * 
     */
    public InvestigationTypeBase createInvestigationTypeBase() {
        return new InvestigationTypeBase();
    }

    /**
     * Create an instance of {@link MedicationLinkType }
     * 
     */
    public MedicationLinkType createMedicationLinkType() {
        return new MedicationLinkType();
    }

    /**
     * Create an instance of {@link CarePlanType }
     * 
     */
    public CarePlanType createCarePlanType() {
        return new CarePlanType();
    }

    /**
     * Create an instance of {@link InvestigationType }
     * 
     */
    public InvestigationType createInvestigationType() {
        return new InvestigationType();
    }

    /**
     * Create an instance of {@link ReferralType }
     * 
     */
    public ReferralType createReferralType() {
        return new ReferralType();
    }

    /**
     * Create an instance of {@link AuthorType }
     * 
     */
    public AuthorType createAuthorType() {
        return new AuthorType();
    }

    /**
     * Create an instance of {@link AlertType }
     * 
     */
    public AlertType createAlertType() {
        return new AlertType();
    }

    /**
     * Create an instance of {@link DrugDeliveryType }
     * 
     */
    public DrugDeliveryType createDrugDeliveryType() {
        return new DrugDeliveryType();
    }

    /**
     * Create an instance of {@link DiaryListType }
     * 
     */
    public DiaryListType createDiaryListType() {
        return new DiaryListType();
    }

    /**
     * Create an instance of {@link IssueListType }
     * 
     */
    public IssueListType createIssueListType() {
        return new IssueListType();
    }

    /**
     * Create an instance of {@link RegistrationHistoryType }
     * 
     */
    public RegistrationHistoryType createRegistrationHistoryType() {
        return new RegistrationHistoryType();
    }

    /**
     * Create an instance of {@link InvestigationListType }
     * 
     */
    public InvestigationListType createInvestigationListType() {
        return new InvestigationListType();
    }

    /**
     * Create an instance of {@link AppointmentListType }
     * 
     */
    public AppointmentListType createAppointmentListType() {
        return new AppointmentListType();
    }

    /**
     * Create an instance of {@link ItemBaseType }
     * 
     */
    public ItemBaseType createItemBaseType() {
        return new ItemBaseType();
    }

    /**
     * Create an instance of {@link ReferralListType }
     * 
     */
    public ReferralListType createReferralListType() {
        return new ReferralListType();
    }

    /**
     * Create an instance of {@link PathologyTestListType }
     * 
     */
    public PathologyTestListType createPathologyTestListType() {
        return new PathologyTestListType();
    }

    /**
     * Create an instance of {@link AttachmentType }
     * 
     */
    public AttachmentType createAttachmentType() {
        return new AttachmentType();
    }

    /**
     * Create an instance of {@link DiaryType }
     * 
     */
    public DiaryType createDiaryType() {
        return new DiaryType();
    }

    /**
     * Create an instance of {@link MedicationListType }
     * 
     */
    public MedicationListType createMedicationListType() {
        return new MedicationListType();
    }

    /**
     * Create an instance of {@link AlertListType }
     * 
     */
    public AlertListType createAlertListType() {
        return new AlertListType();
    }

    /**
     * Create an instance of {@link TestRequestListType }
     * 
     */
    public TestRequestListType createTestRequestListType() {
        return new TestRequestListType();
    }

    /**
     * Create an instance of {@link SpecimenListType }
     * 
     */
    public SpecimenListType createSpecimenListType() {
        return new SpecimenListType();
    }

    /**
     * Create an instance of {@link EDICommentListType }
     * 
     */
    public EDICommentListType createEDICommentListType() {
        return new EDICommentListType();
    }

    /**
     * Create an instance of {@link IssueType }
     * 
     */
    public IssueType createIssueType() {
        return new IssueType();
    }

    /**
     * Create an instance of {@link CareAimType }
     * 
     */
    public CareAimType createCareAimType() {
        return new CareAimType();
    }

    /**
     * Create an instance of {@link AllergyType }
     * 
     */
    public AllergyType createAllergyType() {
        return new AllergyType();
    }

    /**
     * Create an instance of {@link AttachmentListType }
     * 
     */
    public AttachmentListType createAttachmentListType() {
        return new AttachmentListType();
    }

    /**
     * Create an instance of {@link PeopleListType }
     * 
     */
    public PeopleListType createPeopleListType() {
        return new PeopleListType();
    }

    /**
     * Create an instance of {@link IntegerCodeType }
     * 
     */
    public IntegerCodeType createIntegerCodeType() {
        return new IntegerCodeType();
    }

    /**
     * Create an instance of {@link LinkListType }
     * 
     */
    public LinkListType createLinkListType() {
        return new LinkListType();
    }

    /**
     * Create an instance of {@link ConsultationType }
     * 
     */
    public ConsultationType createConsultationType() {
        return new ConsultationType();
    }

    /**
     * Create an instance of {@link ConsultationListType }
     * 
     */
    public ConsultationListType createConsultationListType() {
        return new ConsultationListType();
    }

    /**
     * Create an instance of {@link NoteType }
     * 
     */
    public NoteType createNoteType() {
        return new NoteType();
    }

    /**
     * Create an instance of {@link StatusType }
     * 
     */
    public StatusType createStatusType() {
        return new StatusType();
    }

    /**
     * Create an instance of {@link ProblemType }
     * 
     */
    public ProblemType createProblemType() {
        return new ProblemType();
    }

    /**
     * Create an instance of {@link EDIComment }
     * 
     */
    public EDIComment createEDIComment() {
        return new EDIComment();
    }

    /**
     * Create an instance of {@link MixtureItemListType }
     * 
     */
    public MixtureItemListType createMixtureItemListType() {
        return new MixtureItemListType();
    }

    /**
     * Create an instance of {@link LinkType }
     * 
     */
    public LinkType createLinkType() {
        return new LinkType();
    }

    /**
     * Create an instance of {@link AddressType }
     * 
     */
    public AddressType createAddressType() {
        return new AddressType();
    }

    /**
     * Create an instance of {@link StringCodeType }
     * 
     */
    public StringCodeType createStringCodeType() {
        return new StringCodeType();
    }

    /**
     * Create an instance of {@link LocationType.GPLinkCodeList.GPLinkCode }
     * 
     */
    public LocationType.GPLinkCodeList.GPLinkCode createLocationTypeGPLinkCodeListGPLinkCode() {
        return new LocationType.GPLinkCodeList.GPLinkCode();
    }

    /**
     * Create an instance of {@link MixtureType.Constituents }
     * 
     */
    public MixtureType.Constituents createMixtureTypeConstituents() {
        return new MixtureType.Constituents();
    }

    /**
     * Create an instance of {@link TestRequestHeaderType.EDIOrderList }
     * 
     */
    public TestRequestHeaderType.EDIOrderList createTestRequestHeaderTypeEDIOrderList() {
        return new TestRequestHeaderType.EDIOrderList();
    }

    /**
     * Create an instance of {@link RegistrationStatusType.CurrentStatus }
     * 
     */
    public RegistrationStatusType.CurrentStatus createRegistrationStatusTypeCurrentStatus() {
        return new RegistrationStatusType.CurrentStatus();
    }

    /**
     * Create an instance of {@link RegistrationStatusType.StatusHistoryList }
     * 
     */
    public RegistrationStatusType.StatusHistoryList createRegistrationStatusTypeStatusHistoryList() {
        return new RegistrationStatusType.StatusHistoryList();
    }

    /**
     * Create an instance of {@link RegistrationStatusType.RegistrationHistoryList }
     * 
     */
    public RegistrationStatusType.RegistrationHistoryList createRegistrationStatusTypeRegistrationHistoryList() {
        return new RegistrationStatusType.RegistrationHistoryList();
    }

    /**
     * Create an instance of {@link CodedItemBaseType.QualifierList }
     * 
     */
    public CodedItemBaseType.QualifierList createCodedItemBaseTypeQualifierList() {
        return new CodedItemBaseType.QualifierList();
    }

    /**
     * Create an instance of {@link MedicationType.Drug }
     * 
     */
    public MedicationType.Drug createMedicationTypeDrug() {
        return new MedicationType.Drug();
    }

    /**
     * Create an instance of {@link OriginatorType.System }
     * 
     */
    public OriginatorType.System createOriginatorTypeSystem() {
        return new OriginatorType.System();
    }

    /**
     * Create an instance of {@link RegistrationType.CustomRegistrationFields.CustomRegistrationEntry }
     * 
     */
    public RegistrationType.CustomRegistrationFields.CustomRegistrationEntry createRegistrationTypeCustomRegistrationFieldsCustomRegistrationEntry() {
        return new RegistrationType.CustomRegistrationFields.CustomRegistrationEntry();
    }

    /**
     * Create an instance of {@link PolicyListType.PolicyType.UserList }
     * 
     */
    public PolicyListType.PolicyType.UserList createPolicyListTypePolicyTypeUserList() {
        return new PolicyListType.PolicyType.UserList();
    }

    /**
     * Create an instance of {@link PathologyTestType.RangeInformationList.RangeInformation }
     * 
     */
    public PathologyTestType.RangeInformationList.RangeInformation createPathologyTestTypeRangeInformationListRangeInformation() {
        return new PathologyTestType.RangeInformationList.RangeInformation();
    }

    /**
     * Create an instance of {@link AppointmentType.Site }
     * 
     */
    public AppointmentType.Site createAppointmentTypeSite() {
        return new AppointmentType.Site();
    }

    /**
     * Create an instance of {@link StructuredIdentType.NationalCode }
     * 
     */
    public StructuredIdentType.NationalCode createStructuredIdentTypeNationalCode() {
        return new StructuredIdentType.NationalCode();
    }

    /**
     * Create an instance of {@link PathologyReportType.OriginalRequestor }
     * 
     */
    public PathologyReportType.OriginalRequestor createPathologyReportTypeOriginalRequestor() {
        return new PathologyReportType.OriginalRequestor();
    }

    /**
     * Create an instance of {@link PathologyReportType.ServiceProvider }
     * 
     */
    public PathologyReportType.ServiceProvider createPathologyReportTypeServiceProvider() {
        return new PathologyReportType.ServiceProvider();
    }

    /**
     * Create an instance of {@link PathologyReportType.OriginalMessageDetails }
     * 
     */
    public PathologyReportType.OriginalMessageDetails createPathologyReportTypeOriginalMessageDetails() {
        return new PathologyReportType.OriginalMessageDetails();
    }

    /**
     * Create an instance of {@link PathologyReportType.Identifiers.OriginalDetails }
     * 
     */
    public PathologyReportType.Identifiers.OriginalDetails createPathologyReportTypeIdentifiersOriginalDetails() {
        return new PathologyReportType.Identifiers.OriginalDetails();
    }

    /**
     * Create an instance of {@link PathologyReportType.Identifiers.MatchedDetails }
     * 
     */
    public PathologyReportType.Identifiers.MatchedDetails createPathologyReportTypeIdentifiersMatchedDetails() {
        return new PathologyReportType.Identifiers.MatchedDetails();
    }

    /**
     * Create an instance of {@link EDIOrderType.FormDestination }
     * 
     */
    public EDIOrderType.FormDestination createEDIOrderTypeFormDestination() {
        return new EDIOrderType.FormDestination();
    }

    /**
     * Create an instance of {@link EDIOrderType.TestRequestList }
     * 
     */
    public EDIOrderType.TestRequestList createEDIOrderTypeTestRequestList() {
        return new EDIOrderType.TestRequestList();
    }

    /**
     * Create an instance of {@link ElementListType.ConsultationElement }
     * 
     */
    public ElementListType.ConsultationElement createElementListTypeConsultationElement() {
        return new ElementListType.ConsultationElement();
    }

    /**
     * Create an instance of {@link MedicalRecordType.MiscellaneousData }
     * 
     */
    public MedicalRecordType.MiscellaneousData createMedicalRecordTypeMiscellaneousData() {
        return new MedicalRecordType.MiscellaneousData();
    }

    /**
     * Create an instance of {@link MedicalRecordType.TestRequestHeaderList }
     * 
     */
    public MedicalRecordType.TestRequestHeaderList createMedicalRecordTypeTestRequestHeaderList() {
        return new MedicalRecordType.TestRequestHeaderList();
    }

    /**
     * Create an instance of {@link MedicalRecordType.MessageInformation.MessagingError }
     * 
     */
    public MedicalRecordType.MessageInformation.MessagingError createMedicalRecordTypeMessageInformationMessagingError() {
        return new MedicalRecordType.MessageInformation.MessagingError();
    }

    /**
     * Create an instance of {@link MedicalRecordType.MessageInformation.MessagePurpose.Post }
     * 
     */
    public MedicalRecordType.MessageInformation.MessagePurpose.Post createMedicalRecordTypeMessageInformationMessagePurposePost() {
        return new MedicalRecordType.MessageInformation.MessagePurpose.Post();
    }

    /**
     * Create an instance of {@link MedicalRecordType.MessageInformation.MessagePurpose.Get }
     * 
     */
    public MedicalRecordType.MessageInformation.MessagePurpose.Get createMedicalRecordTypeMessageInformationMessagePurposeGet() {
        return new MedicalRecordType.MessageInformation.MessagePurpose.Get();
    }

    /**
     * Create an instance of {@link MedicalRecordType.RegistrationChangeHistory.RegistrationEntry }
     * 
     */
    public MedicalRecordType.RegistrationChangeHistory.RegistrationEntry createMedicalRecordTypeRegistrationChangeHistoryRegistrationEntry() {
        return new MedicalRecordType.RegistrationChangeHistory.RegistrationEntry();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link MedicalRecordType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.e-mis.com/emisopen/MedicalRecord", name = "MedicalRecord")
    public JAXBElement<MedicalRecordType> createMedicalRecord(MedicalRecordType value) {
        return new JAXBElement<MedicalRecordType>(_MedicalRecord_QNAME, MedicalRecordType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Byte }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.e-mis.com/emisopen/MedicalRecord", name = "Episodicity")
    public JAXBElement<Byte> createEpisodicity(Byte value) {
        return new JAXBElement<Byte>(_Episodicity_QNAME, Byte.class, null, value);
    }

}
