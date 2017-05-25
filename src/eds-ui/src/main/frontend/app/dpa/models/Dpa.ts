import {Documentation} from "../../documention/models/Documentation";
export class Dpa {
    uuid : string;
    name : string;
    description : string;
    derivation : string;
    publisherInformation : string;
    publisherContractInformation : string;
    publisherDataSet : string;
    dsaStatusId : number;
    dataFlow : string;
    returnToSenderPolicy : string;
    startDate : string;
    endDate : string;
    dataFlows : { [key:string]:string; };
    cohorts : { [key:string]:string; };
    dataSets : { [key:string]:string; };
    documentations : Documentation[];

    getDisplayItems() :any[] {
        return [
            {label: 'Description', property: 'description'},
            {label: 'Derivation', property: 'derivation'},
            {label: 'Publisher Information', property: 'publisherInformation'},
            {label: 'Publisher Contract Information', property: 'publisherContractInformation'},
            {label: 'Publisher DataSet', property: 'publisherDataSet'},
            {label: 'DSA Status Id', property: 'dsaStatusId'},
            {label: 'Return To Sender Policy', property: 'returnToSenderPolicy'}
        ];
    }
}
