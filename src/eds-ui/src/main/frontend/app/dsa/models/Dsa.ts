import {DsaPurpose} from "./DsaPurpose";
import {DsaBenefit} from "./DsaBenefit";

export class Dsa {
    uuid : string;
    name : string;
    description : string;
    derivation : string;
    publisherInformation : string;
    publisherContractInformation : string;
    subscriberInformation : string;
    subscriberContractInformation : string;
    dsaStatusId : number;
    consentModel : string;
    dataFlows : { [key:string]:string; };
    regions : { [key:string]:string; };
    publishers : { [key:string]:string; };
    subscribers : { [key:string]:string; };
    purposes : DsaPurpose[];
    benefits : DsaBenefit[];

    getDisplayItems() :any[] {
        return [
            {label: 'Description', property: 'description'},
            {label: 'Derivation', property: 'derivation'},
            {label: 'Publisher Information', property: 'publisherInformation'},
            {label: 'Publisher Contract Information', property: 'publisherContractInformation'},
            {label: 'Subscriber Information', property: 'subscriberInformation'},
            {label: 'Subscriber Contract Information', property: 'subscriberContractInformation'},
        ];
    }
}
