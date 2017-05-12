export class DataSharingSummary {
    uuid : string;
    name: string;
    description : string;
    purpose : string;
    natureOfInformationId : number;
    schedule2Condition : string;
    benefitToSharing : string;
    overviewOfDataItems : string
    formatTypeId : number;
    dataSubjectTypeId : number;
    natureOfPersonsAccessingData : string;
    reviewCycleId : number;
    reviewDate : Date;
    startDate : Date;
    evidenceOfAgreement : string;

    getDisplayItems() :any[] {
        return [
            {label: 'Description', property: 'description'},
            {label: 'Purpose', property: 'purpose'},
            {label: 'Schedule 2 Condition', property: 'schedule2Condition'},
            {label: 'Overview Of Data Items', property: 'overviewOfDataItems'},
            {label: 'Review Date', property: 'reviewDate'},
            {label: 'Start Date', property: 'startDate'},
            {label: 'Evidence Of Agreement', property: 'evidenceOfAgreement'}
        ];
    }
}
